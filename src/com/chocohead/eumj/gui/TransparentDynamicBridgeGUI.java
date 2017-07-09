package com.chocohead.eumj.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import ic2.core.ContainerBase;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser.GuiNode;

@SideOnly(Side.CLIENT)
public class TransparentDynamicBridgeGUI<B extends IInventory> extends DynamicBridgeGUI<B> {
	public TransparentDynamicBridgeGUI(B base, EntityPlayer player, GuiNode guiNode) {
		this(player, DynamicContainer.create(base, player, guiNode), guiNode);
	}

	protected TransparentDynamicBridgeGUI(EntityPlayer player, ContainerBase<B> container, GuiNode guiNode) {
		super(player, container, guiNode);
	}

	@Override
	protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
	}
}