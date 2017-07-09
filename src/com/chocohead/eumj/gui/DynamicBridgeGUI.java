package com.chocohead.eumj.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.gui.pos.GuiRectangle;

import ic2.core.ContainerBase;
import ic2.core.gui.GuiElement;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser.GuiNode;

/**
 * A bridge between IC2's {@link GuiElement}s and BC's {@link IGuiElement}s.
 *
 * @param <B> The inventory the container this GUI has is for
 *
 * @author Chocohead
 */
@SideOnly(Side.CLIENT)
public class DynamicBridgeGUI<B extends IInventory> extends DynamicGui<ContainerBase<B>> {
	@FunctionalInterface
	protected interface Background {
		void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);
	}
	@FunctionalInterface
	protected interface Foreground {
		void drawGuiContainerForegroundLayer(int mouseX, int mouseY);
	}

	protected final List<Consumer<Consumer<IGuiElement>>> BCelements = new ArrayList<>();
	protected final GuiBC8<?> wrappedGUI = new GuiBC8<ContainerBC_Neptune>(null) {
		{
			wrappedBackground = this::drawGuiContainerBackgroundLayer;
			wrappedForeground = this::drawGuiContainerForegroundLayer;

			xSize = DynamicBridgeGUI.this.xSize;
			ySize = DynamicBridgeGUI.this.ySize;
		}

		@Override
		public void initGui() {
			guiLeft = DynamicBridgeGUI.this.guiLeft;
			guiTop = DynamicBridgeGUI.this.guiTop;

			mc = DynamicBridgeGUI.this.mc;
			itemRender = mc.getRenderItem();
			fontRendererObj = mc.fontRendererObj;

			wrappedGUI.guiElements.clear();
		}
	};
	protected Background wrappedBackground;
	protected Foreground wrappedForeground;


	public DynamicBridgeGUI(B base, EntityPlayer player, GuiNode guiNode) {
		this(player, DynamicContainer.create(base, player, guiNode), guiNode);
	}

	protected DynamicBridgeGUI(EntityPlayer player, ContainerBase<B> container, GuiNode guiNode) {
		super(player, container, guiNode);
	}

	/**
	 * Access to the wrapped BC GUI to allow the registering of {@link Ledger_Neptune}s.
	 *
	 * @return The wrapped BC GUI
	 */
	public GuiBC8<?> getWrappedGUI() {
		return wrappedGUI;
	}

	/**
	 * Add a producer of {@link IGuiElement}s that will be added when {@link #initGui()} is called.
	 *
	 * @param producer A producer of {@link IGuiElement}s
	 */
	public void addElementProducer(Consumer<Consumer<IGuiElement>> producer) {
		BCelements.add(producer);
	}

	/**
	 * Get a view of {@link GuiElement}s that the GUI has.
	 *
	 * @return A view of the current {@link GuiElement}s
	 */
	public List<GuiElement<?>> getGuiElements() {
		return Collections.unmodifiableList(elements);
	}


	// Bouncers >>
	@Override
	public void initGui() {
		super.initGui();

		wrappedGUI.setGuiSize(width, height);
		wrappedGUI.initGui();

		BCelements.forEach(element -> element.accept(wrappedGUI.guiElements::add));
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		wrappedGUI.ledgersLeft.update();
		wrappedGUI.ledgersRight.update();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

		wrappedBackground.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	@Override
	protected void drawForegroundLayer(int mouseX, int mouseY) {
		super.drawForegroundLayer(mouseX, mouseY);

		wrappedForeground.drawGuiContainerForegroundLayer(mouseX + guiLeft, mouseY + guiTop);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		wrappedGUI.mouse.setMousePosition(mouseX, mouseY);

		GuiRectangle debugRect = new GuiRectangle(0, 0, 16, 16);
		if (debugRect.contains(wrappedGUI.mouse)) {
			GuiBC8.debugging = !GuiBC8.debugging;
		}

		for (IGuiElement element : wrappedGUI.guiElements) {
			element.onMouseClicked(mouseButton);
		}

		wrappedGUI.ledgersLeft.onMouseClicked(mouseButton);
		wrappedGUI.ledgersRight.onMouseClicked(mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

		wrappedGUI.mouse.setMousePosition(mouseX, mouseY);

		for (IGuiElement element : wrappedGUI.guiElements) {
			element.onMouseDragged(clickedMouseButton, timeSinceLastClick);
		}

		wrappedGUI.ledgersLeft.onMouseDragged(clickedMouseButton, timeSinceLastClick);
		wrappedGUI.ledgersRight.onMouseDragged(clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);

		wrappedGUI.mouse.setMousePosition(mouseX, mouseY);

		for (IGuiElement element : wrappedGUI.guiElements) {
			element.onMouseReleased(state);
		}

		wrappedGUI.ledgersLeft.onMouseReleased(state);
		wrappedGUI.ledgersRight.onMouseReleased(state);
	}
}