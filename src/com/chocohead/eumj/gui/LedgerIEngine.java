package com.chocohead.eumj.gui;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.BCLibSprites;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;

import com.chocohead.eumj.util.IEngine;

/**
 * A clone of {@link LedgerEngine} that uses {@link IEngine} over {@link TileEngineBase_BC8}.
 *
 * @author Chocohead
 */
@SideOnly(Side.CLIENT)
public class LedgerIEngine extends Ledger_Neptune {
	private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;
	private static final int HEADER_COLOUR = 0xFF_E1_C9_2F;
	private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
	private static final int TEXT_COLOUR = 0xFF_00_00_00;

	protected final IEngine engine;

	public LedgerIEngine(BuildCraftGui gui, IEngine engine) {
		super(gui, OVERLAY_COLOUR, true);

		this.engine = engine;
		title = "gui.power";

		appendText(LocaleUtil.localize("gui.currentOutput") + ':', SUB_HEADER_COLOUR).setDropShadow(true);
		appendText(() -> LocaleUtil.localizeMjFlow(engine.getActiveOutput()), TEXT_COLOUR);
		appendText(LocaleUtil.localize("gui.stored") + ':', SUB_HEADER_COLOUR).setDropShadow(true);
		appendText(() -> LocaleUtil.localizeMj(engine.getPower()), TEXT_COLOUR);
		appendText(LocaleUtil.localize("gui.heat") + ':', SUB_HEADER_COLOUR).setDropShadow(true);
		appendText(() -> LocaleUtil.localizeHeat(engine.getHeat()), TEXT_COLOUR);
		calculateMaxSize();
		
		setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftlib:engine"), "ledger.power.is_open", false));
	}

	@Override
	public int getTitleColour() {
		return HEADER_COLOUR;
	}

	@Override
	protected void drawIcon(double x, double y) {
		ISprite sprite;
		switch (engine.getPowerStage()) {
		case OVERHEAT:
			sprite = BCLibSprites.ENGINE_OVERHEAT;
			break;
		case RED:
		case YELLOW:
			sprite = BCLibSprites.ENGINE_WARM;
			break;
		default:
			sprite = engine.getActive() ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
		}
		GuiIcon.draw(sprite, x, y, x + 16, y + 16);
	}
}