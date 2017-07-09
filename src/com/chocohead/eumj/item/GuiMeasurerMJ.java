package com.chocohead.eumj.item;

import com.google.common.base.Supplier;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.MjAPI;

import ic2.core.GuiIC2;
import ic2.core.gui.ItemStackImage;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;

import com.chocohead.eumj.EngineMod;

@SideOnly(Side.CLIENT)
public class GuiMeasurerMJ extends GuiIC2<ContainerMeasurerMJ> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(EngineMod.MODID, "textures/gui/GUI_MJ_Measurer.png");

	public GuiMeasurerMJ(ContainerMeasurerMJ container) {
		super(container, ContainerMeasurerMJ.HEIGHT);

		addElement(Text.create(this, 97, 43, TextProvider.of(() -> Localization.translate("eu-mj_engine.mj_reader.time", getContainer().getTime())), 0x20EB3E, false));
		for (EnumFacing facing : EnumFacing.VALUES) {
			addElement(Text.create(this, 15, 43 + facing.getIndex() * 11, TextProvider.of(new Supplier<String>() {
				private final String side = Localization.translate(getNameForFacing(facing));

				private String getNameForFacing(EnumFacing facing) {
					switch (facing) {
					case WEST:  return "ic2.dir.West";
					case EAST:  return "ic2.dir.East";
					case DOWN:  return "ic2.dir.Bottom";
					case UP:    return "ic2.dir.Top";
					case NORTH: return "ic2.dir.North";
					case SOUTH: return "ic2.dir.South";
					default: throw new IllegalStateException("Unexpected direction: "+facing);
					}
				}

				@Override
				public String get() {
					//return side + ": " + Util.toSiString(getContainer().getAverageEnergy(facing) / MjAPI.MJ, 2) + "MJ";
					return Localization.translate("eu-mj_engine.mj_reader.direction", side, MjAPI.formatMj((long) getContainer().getAverageEnergy(facing)));
				}
			}), 0x20EB3E, false));
		}
		addElement(new ItemStackImage(this, 115, 75, container::getStack));
	}

	@Override
	protected ResourceLocation getTexture() {
		return TEXTURE;
	}
}