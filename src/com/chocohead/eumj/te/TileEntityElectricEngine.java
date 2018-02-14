package com.chocohead.eumj.te;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;

import ic2.core.ContainerBase;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot.Access;
import ic2.core.block.invslot.InvSlot.InvSide;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.GuiParser.GuiNode;
import ic2.core.gui.dynamic.GuiParser.Node;
import ic2.core.gui.dynamic.GuiParser.ParentNode;
import ic2.core.gui.dynamic.GuiParser.SlotNode;
import ic2.core.network.GuiSynced;

import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.EngineMod.Conversion;
import com.chocohead.eumj.gui.DynamicBridgeGUI;
import com.chocohead.eumj.gui.LedgerIEngine;

/**
 * An abstract implementation of an engine that uses EU to produce MJ
 *
 * @author Chocohead
 */
public abstract class TileEntityElectricEngine extends TileEntityEngine implements IUpgradableBlock {
	protected final int tier;
	protected final Energy energy;

	public final InvSlotDischarge slot;
	public final InvSlotUpgrade upgrade;

	@GuiSynced
	protected boolean engineActive = false;

	public TileEntityElectricEngine(int tier) {
		this.tier = tier;
		slot = new InvSlotDischarge(this, Access.IO, tier, false, InvSide.ANY);
		energy = addComponent(new Energy(this, 100_000, Collections.singleton(getFacing().getOpposite()),
				Collections.emptySet(), tier)).addManagedSlot(slot);
		upgrade = new InvSlotUpgrade(this, "upgrade", 1);
	}

	@Override
	protected IMjConnector createConnector() {
		return new EngineConnector(false);
	}
	// << Constructor + fields

	// Energy facing hooks >>
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		energy.setDirections(Collections.singleton(getFacing().getOpposite()), energy.getSinkDirs());
	}

	@Override
	protected void setFacing(EnumFacing facing) {
		assert getFacing() != facing; //This shouldn't happen
		super.setFacing(facing);

		assert facing == getFacing(); //This should be an optimisation that can be made
		energy.setDirections(Collections.singleton(facing.getOpposite()), energy.getSinkDirs());
	}
	// << Energy facing hooks

	// Upgrade Logic >>
	@Override
	protected void onLoaded() {
		super.onLoaded();

		if (!world.isRemote) {
			updateTier();
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();

		if (!world.isRemote) {
			updateTier();
		}
	}

	protected void updateTier() {
		int tier = upgrade.getTier(this.tier);

		energy.setSinkTier(tier);
		slot.setTier(tier);
	}
	// << Upgrade Logic

	// Engine run logic >>
	// Engine state >>>
	@Override
	protected EnumPowerStage computePowerStage() {
		return EnumPowerStage.GREEN;
	}

	@Override
	protected abstract double getPistonSpeed();
	// <<< Engine State

	// Power >>>
	@Override
	protected void burn() {
		long output = getOutput();
		double input = Conversion.MJtoEU(output);

		if (energy.canUseEnergy(input) && redstone.hasRedstoneInput()) {
			engineActive = true;

			addPower(output);
			energy.useEnergy(input);
		} else {
			engineActive = false;
		}
	}

	/**
	 * @return The engine output in microMJ
	 */
	protected abstract long getOutput();

	@Override
	protected long getMaxPower() {
		return 10000 * MjAPI.MJ;
	}

	@Override
	public long maxPowerExtracted() {
		return 1000 * MjAPI.MJ;
	}
	// <<< Power
	// << Engine run logic

	// IHasGUI >>
	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player) {
		return DynamicContainer.create(this, player, GuiParser.parse(teBlock));
	}

	@SideOnly(Side.CLIENT)
	protected DynamicBridgeGUI<TileEntityElectricEngine> makeBridge(EntityPlayer player, GuiNode node, boolean isAdmin) {
		return new DynamicBridgeGUI<>(this, player, node);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
		GuiNode node = GuiParser.parse(teBlock);
		DynamicBridgeGUI<TileEntityElectricEngine> gui = makeBridge(player, node, isAdmin);
		gui.addHelpLedger();
		gui.getWrappedGUI().shownElements.add(new LedgerIEngine(gui.getWrappedGUI(), this));
		gui.addElementProducer(new Consumer<Consumer<IGuiElement>>() {
			private IGuiArea makeArea(SlotNode node) {
				return new GuiRectangle(node.x, node.y, node.style.width, node.style.height).offset(gui.getWrappedGUI().rootElement);
			}

			private ElementHelpInfo makeHelp(String name) {
				//The shift is just for show to avoid them producing similar hashes (thus similar colours)
				return new ElementHelpInfo(EngineMod.MODID+".help."+name+".title", 0xFF_00_00_00 | name.hashCode() >> 4, EngineMod.MODID+".help."+name);
			}

			private void findSlots(Consumer<IGuiElement> list, ParentNode node) {
				for (Node rawNode : node.getNodes()) {
					switch (rawNode.getType()) {
					case slot:
						SlotNode realNode = (SlotNode) rawNode;

						ElementHelpInfo help = null;
						switch (realNode.name) {
						case "upgrade":
							help = makeHelp("upgrade");
							break;

						case "discharge":
							help = makeHelp("battery");
							break;
						}

						if (help != null) {
							list.accept(new DummyHelpElement(makeArea(realNode), help));
						}

						break;

					default:
						break;
					}

					if (rawNode instanceof ParentNode) {
						findSlots(list, (ParentNode) rawNode);
					}
				}
			}

			@Override
			public void accept(Consumer<IGuiElement> list) {
				findSlots(list, node);
			}
		});
		return gui;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public long getActiveOutput() {
		return engineActive ? getOutput() : 0;
	}
	// << IHasGUI

	// IUpgradableBlock >>
	@Override
	public double getEnergy() {
		return energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount) {
		return energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties() {
		return EnumSet.of(UpgradableProperty.Transformer);
	}
}