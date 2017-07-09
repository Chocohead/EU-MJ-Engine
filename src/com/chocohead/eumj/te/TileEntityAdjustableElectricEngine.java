package com.chocohead.eumj.te;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.MjAPI;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;

import ic2.api.network.INetworkClientTileEntityEventListener;

import ic2.core.IC2;
import ic2.core.gui.dynamic.GuiParser.ButtonNode;
import ic2.core.gui.dynamic.GuiParser.GuiNode;
import ic2.core.gui.dynamic.GuiParser.Node;
import ic2.core.gui.dynamic.GuiParser.ParentNode;
import ic2.core.init.Localization;
import ic2.core.network.GuiSynced;

import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.EngineMod.Conversion;
import com.chocohead.eumj.gui.DynamicBridgeGUI;
import com.chocohead.eumj.gui.TransparentDynamicBridgeGUI;

public class TileEntityAdjustableElectricEngine extends TileEntityElectricEngine implements INetworkClientTileEntityEventListener {
	private static final Field X, Y, WIDTH, HEIGHT, EVENT;

	static {
		Field x = null, y = null, width = null, height = null, event = null;
		for (Field field : ButtonNode.class.getDeclaredFields()) {
			field.setAccessible(true);

			switch (field.getName()) {
			case "x":
				x = field;
				break;

			case "y":
				y = field;
				break;

			case "width":
				width = field;
				break;

			case "height":
				height = field;
				break;

			case "eventID":
				event = field;
				break;
			}
		}
		X = x;
		Y = y;
		WIDTH = width;
		HEIGHT = height;
		EVENT = event;

		assert X != null;
		assert Y != null;
		assert WIDTH != null;
		assert HEIGHT != null;
		assert EVENT != null;
	}

	@GuiSynced
	protected long output = 10;
	protected double pistonSpeed = 0.07;

	public TileEntityAdjustableElectricEngine() {
		super(4);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("output", output);
		nbt.setDouble("piston", pistonSpeed);

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		output = nbt.getLong("output");
		pistonSpeed = nbt.getDouble("piston");
	}

	@Override
	public List<String> getNetworkedFields() {
		List<String> out = super.getNetworkedFields();

		out.add("output");
		out.add("pistonSpeed");

		return out;
	}

	@Override
	protected double getPistonSpeed() {
		return pistonSpeed;
	}

	@Override
	protected long getOutput() {
		return output * MjAPI.MJ;
	}

	@Override
	public long maxPowerExtracted() {
		return 3000 * MjAPI.MJ; //TODO: Configurable
	}


	@Override
	@SideOnly(Side.CLIENT)
	protected DynamicBridgeGUI<TileEntityElectricEngine> makeBridge(EntityPlayer player, GuiNode node, boolean isAdmin) {
		DynamicBridgeGUI<TileEntityElectricEngine> gui = new TransparentDynamicBridgeGUI<>(this, player, node);
		gui.addElementProducer(new Consumer<Consumer<IGuiElement>>() {
			private IGuiArea makeArea(ButtonNode node) {
				try {
					return new GuiRectangle(X.getInt(node), Y.getInt(node), WIDTH.getInt(node), HEIGHT.getInt(node)).offset(gui.getWrappedGUI().rootElement);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Error reflecting button node", e);
				}
			}

			private ElementHelpInfo makeHelp(int change) {
				return new ElementHelpInfo(EngineMod.MODID+".help.power_button.title", 0xFF_00_00_00 | 150 * change, Localization.translate(EngineMod.MODID+".help.power_button", change));
			}

			private void findButtons(Consumer<IGuiElement> list, ParentNode node) {
				for (Node rawNode : node.getNodes()) {
					switch (rawNode.getType()) {
					case button:
						ButtonNode realNode = (ButtonNode) rawNode;

						ElementHelpInfo help = null;
						try {
							switch (EVENT.getInt(realNode) % 10) {
							case 0:
								help = makeHelp(-100);
								break;
							case 1:
								help = makeHelp(-10);
								break;
							case 2:
								help = makeHelp(-1);
								break;
							case 3:
								help = makeHelp(1);
								break;
							case 4:
								help = makeHelp(10);
								break;
							case 5:
								help = makeHelp(100);
								break;
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new RuntimeException("Error reflecting button node", e);
						}

						if (help != null) {
							list.accept(new DummyHelpElement(makeArea(realNode), help));
						}

						break;

					default:
						break;
					}

					if (rawNode instanceof ParentNode) {
						findButtons(list, (ParentNode) rawNode);
					}
				}
			}

			@Override
			public void accept(Consumer<IGuiElement> list) {
				findButtons(list, node);
			}
		});
		return gui;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event) {
		switch (event / 10) {
		case 0:
			switch (event % 10) {
			case 0:
				changeProduction(-100);
				break;
			case 1:
				changeProduction(-10);
				break;
			case 2:
				changeProduction(-1);
				break;
			case 3:
				changeProduction(1);
				break;
			case 4:
				changeProduction(10);
				break;
			case 5:
				changeProduction(100);
				break;
			}
			break;
		}
	}

	protected void changeProduction(int value) {
		output = Math.max(output + value, 1);
		pistonSpeed = Math.max(0.1F, 1.0F / (3000 / output - 2.0F));

		IC2.network.get(true).updateTileEntityField(this, "pistonSpeed");
	}

	@SideOnly(Side.CLIENT)
	public String getCurrentOutput() {
		return Localization.translate("eu-mj_engine.engines.adjustable_electric_engine.info", Conversion.MJtoEU(getOutput()), output);
	}
}