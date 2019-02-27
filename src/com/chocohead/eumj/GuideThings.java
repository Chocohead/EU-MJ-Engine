package com.chocohead.eumj;

import java.util.Collections;

import com.chocohead.eumj.te.Engine_TEs;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;

import ic2.core.util.StackUtil;

class GuideThings {
	@SideOnly(Side.CLIENT)
	public static void addLoaders() {
		//BuildCraft bounces the loading of Markdown into XML anyway, we'll just go straight there thank you.
		GuideManager.PAGE_LOADERS.put("xml", XmlPageLoader.INSTANCE);
	}
	
	@SideOnly(Side.CLIENT)
	public static void addTags() {
		XmlPageLoader.TAG_FACTORIES.put("engineLink", (tag, profiler) -> {
			ItemStack stack = XmlPageLoader.loadItemStack(tag);

			PageLine line;
			if (StackUtil.isEmpty(stack)) {
				line = new PageLine(1, "Missing item: "+tag, false);
			} else {
				ISimpleDrawable icon = new GuiStack(stack);
				line = new PageLine(icon, icon, 1, stack.getDisplayName(), true);
			}

			return Collections.singletonList(gui -> new GuideText(gui, line) {
				@Override
				public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
					if (line.link && (wasHovered || wasIconHovered)) {
						gui.openPage(GuideManager.INSTANCE.getPageFor(stack).createNew(gui));
					}

					return renderLine(current, text, x, y, width, height, -1);
				}
			});
		});

		Object[] engines = {
			EngineMod.engine.getItemStack(Engine_TEs.slow_electric_engine),
			EngineMod.engine.getItemStack(Engine_TEs.regular_electric_engine),
			EngineMod.engine.getItemStack(Engine_TEs.fast_electric_engine),
			EngineMod.engine.getItemStack(Engine_TEs.quick_electric_engine),
			EngineMod.engine.getItemStack(Engine_TEs.adjustable_electric_engine),
		};
        GuideGroupManager.addEntries("buildcraft", "pipe_power_providers", engines);
        GuideGroupManager.addEntries("buildcraft", "full_power_providers", engines);
	}
}