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

import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.Ledger_Neptune;

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
	protected final List<Consumer<Consumer<IGuiElement>>> BCelements = new ArrayList<>();
	protected final BuildCraftGui wrappedGUI = new BuildCraftGui(this, BuildCraftGui.createWindowedArea(this));


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
	public BuildCraftGui getWrappedGUI() {
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
	 * Add the default help element to the wrapped GUI
	 */
	public void addHelpLedger() {
		wrappedGUI.shownElements.add(new LedgerHelp(wrappedGUI, false));
	}

	/**
	 * Get a view of {@link GuiElement}s that the GUI has.
	 *
	 * @return A view of the current {@link GuiElement}s
	 */
	public List<GuiElement<?>> getGuiElements() {
		return Collections.unmodifiableList(elements);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		BCelements.forEach(element -> element.accept(wrappedGUI.shownElements::add)); 
	}


	// Bouncers >>
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (wrappedGUI.currentMenu == null || wrappedGUI.currentMenu.shouldFullyOverride()) {
			//BuildCraft would call renderHoveredToolTip(mouseX, mouseY);
			//But we've already done that as part of IC2
			//A sort of fix for IC2 GuiElements is below, but it won't catch everything
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		wrappedGUI.tick();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		wrappedGUI.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> super.drawBackgroundAndTitle(partialTicks, mouseX - guiLeft, mouseY - guiTop));
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		wrappedGUI.drawElementBackgrounds();
	}

	@Override
	protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY) {
	}

	@Override
	protected void drawForegroundLayer(int mouseX, int mouseY) {
		super.drawForegroundLayer(mouseX, mouseY);
		
		wrappedGUI.preDrawForeground(); //Will GL-shift everything, don't put anything else within block
		wrappedGUI.drawElementForegrounds(() -> super.drawBackgroundAndTitle(wrappedGUI.getLastPartialTicks(), mouseX - guiLeft, mouseY - guiTop));
		wrappedGUI.postDrawForeground(); //Pops everything back
	}
	
	
	@Override
	protected void flushTooltips() {
		noopTooltips = wrappedGUI.currentMenu != null && wrappedGUI.currentMenu.shouldFullyOverride();
		super.flushTooltips();
		noopTooltips = false;
	}
	
	//Amazing fix to suppress tooltips best we can
	private boolean noopTooltips = false;
	
	@Override
	public void drawHoveringText(List<String> textLines, int x, int y) {
		if (!noopTooltips) super.drawHoveringText(textLines, x, y);
	}

	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		wrappedGUI.onMouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

		wrappedGUI.onMouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);

		wrappedGUI.onMouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!wrappedGUI.onKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}
}