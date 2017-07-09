package com.chocohead.eumj.item;

import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

import ic2.core.ContainerBase;
import ic2.core.item.tool.HandHeldInventory;

public class HandHeldReaderMJ extends HandHeldInventory {
	protected final Map<EnumFacing, Section> caps;
	protected final BlockPos pos;

	public HandHeldReaderMJ(EntityPlayer player, ItemStack containerStack, BlockPos pos, Map<EnumFacing, Section> caps) {
		super(player, containerStack, 0);

		this.caps = caps;
		this.pos = pos;
	}

	@SideOnly(Side.CLIENT)
	public HandHeldReaderMJ(EntityPlayer player, ItemStack containerStack) {
		super(player, containerStack, 0);

		caps = null;
		pos = null;
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player) {
		return new ContainerMeasurerMJ(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
		return new GuiMeasurerMJ(new ContainerMeasurerMJ(player, this));
	}

	@Override
	public String getName() {
		return containerStack.getDisplayName();
	}

	@Override
	public boolean hasCustomName() {
		return containerStack.hasDisplayName();
	}
}