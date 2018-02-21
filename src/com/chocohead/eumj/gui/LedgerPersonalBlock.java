package com.chocohead.eumj.gui;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.tile.TileBC_Neptune;

import ic2.core.block.personal.IPersonalBlock;

/**
 * A clone of {@link LedgerOwnership} that uses {@link IPersonalBlock} over {@link TileBC_Neptune}.
 *
 * @author Chocohead
 */
public class LedgerPersonalBlock extends Ledger_Neptune {
	protected final IPersonalBlock tile;

    public LedgerPersonalBlock(BuildCraftGui gui, IPersonalBlock tile, boolean expandPositive) {
        super(gui, 0xFF_E0_F0_FF, expandPositive);
        
        title = "gui.ledger.ownership";
        this.tile = tile;

        appendText(this::getOwnerName, 0);

        calculateMaxSize();
        setOpenProperty(GuiConfigManager.getOrAddBoolean(new ResourceLocation("buildcraftlib:base"), "ledger.owner.is_open", false));
    }

    @Override
    protected void drawIcon(double x, double y) {
        ISprite sprite = SpriteUtil.getFaceSprite(tile.getOwner());
        GuiIcon.draw(sprite, x, y, x + 16, y + 16);
    }

    private String getOwnerName() {
        GameProfile owner = tile.getOwner();
        if (owner == null) {
            return "no-one";
        }
        return owner.getName();
    }
}