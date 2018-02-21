package com.chocohead.eumj;

import static com.chocohead.eumj.EngineMod.MODID;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.BCBlocks.Core;
import buildcraft.api.BCItems;
import buildcraft.api.BCModules;
import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.transport.BCTransportItems;

import ic2.api.event.TeBlockFinalCallEvent;
import ic2.api.item.IC2Items;
import ic2.api.recipe.Recipes;

import ic2.core.block.BlockTileEntity;
import ic2.core.block.TeBlockRegistry;
import ic2.core.item.ItemIC2;
import ic2.core.util.StackUtil;

import com.chocohead.eumj.item.ItemReaderMJ;
import com.chocohead.eumj.te.Engine_TEs;
import com.chocohead.eumj.te.TileEntityEngine;

@Mod(modid=MODID, name="EU-MJ Engine", dependencies="required-after:ic2;required-after:buildcraftenergy@[7.99.15];after:buildcrafttransport", version="@VERSION@")
public final class EngineMod {
	public static final String MODID = "eu-mj_engine";
	public static final CreativeTabs TAB = new CreativeTabs("EU-MJ Engine") {
		private ItemStack[] items;
		private int ticker;

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getIconItemStack() {
			if (++ticker >= 500) {
				ticker = 0;
			}

			if (items == null) {
				items = new ItemStack[5];

				for (int i = 0; i < Engine_TEs.VALUES.length; i++) {
					items[i] = engine.getItemStack(Engine_TEs.VALUES[i]);
				}
			}

			assert ticker / 100 < items.length;
			return items[ticker / 100];
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() {
			return null; //Only normally called from getIconItemStack
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getTranslatedTabLabel() {
			return MODID + ".creative_tab";
		}
	};

	public static BlockTileEntity engine;
	public static ItemIC2 readerMJ;

	@EventHandler
	public void construction(FMLConstructionEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void register(TeBlockFinalCallEvent event) {
		TeBlockRegistry.addAll(Engine_TEs.class, Engine_TEs.IDENTITY);
		TeBlockRegistry.addCreativeRegisterer((list, block, item, tab) -> {
			if (tab == TAB || tab == CreativeTabs.SEARCH) Arrays.stream(Engine_TEs.VALUES).filter(Engine_TEs::hasItem).forEach(type -> list.add(block.getItemStack(type)));
		}, Engine_TEs.IDENTITY);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		loadConfig(event.getSuggestedConfigurationFile());
		event.getModLog().info("Running with "+Conversion.MJperEU / MjAPI.MJ+" MJ per EU or "+MjAPI.MJ / Conversion.MJperEU+" EU per MJ");

		//Blocks
		engine = TeBlockRegistry.get(Engine_TEs.IDENTITY);
		engine.setCreativeTab(TAB);
		//Items
		if (BCModules.TRANSPORT.isLoaded()) {
			readerMJ = new ItemReaderMJ();
		}

		if (event.getSide().isClient()) {
			if (readerMJ != null) readerMJ.registerModels(null);
		}
	}

	private void loadConfig(File file) {
		Configuration config = new Configuration(file);

		try {
			config.load();

			Conversion.MJperEU = MjAPI.MJ * config.getFloat("MJperEU", "balance", 2F / 5F, 1F / 100, 100F, "The number of MJ per EU");
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception loading config!", e);
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Engine_TEs.buildDummies(event.getSide().isClient());

		if (Core.ENGINE != null) {
			Recipes.advRecipes.addRecipe(engine.getItemStack(Engine_TEs.slow_electric_engine),
					"B", "E", "C",
					'B', anyCharge(IC2Items.getItem("re_battery")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.STONE.ordinal()),
					'C', IC2Items.getItem("crafting", "circuit"));

			Recipes.advRecipes.addRecipe(engine.getItemStack(Engine_TEs.regular_electric_engine),
					"B", "E", "C",
					'B', anyCharge(IC2Items.getItem("re_battery")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'C', IC2Items.getItem("crafting", "circuit"));

			Recipes.advRecipes.addRecipe(engine.getItemStack(Engine_TEs.fast_electric_engine),
					"BBB", "EPE", "CPC",
					'B', anyCharge(IC2Items.getItem("advanced_re_battery")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'P', IC2Items.getItem("crafting", "alloy"),
					'C', IC2Items.getItem("crafting", "circuit"));

			Recipes.advRecipes.addRecipe(engine.getItemStack(Engine_TEs.quick_electric_engine),
					"BPB", "EEE", "CPC",
					'B', anyCharge(IC2Items.getItem("energy_crystal")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'P', IC2Items.getItem("crafting", "alloy"),
					'C', IC2Items.getItem("crafting", "advanced_circuit"));

			Recipes.advRecipes.addRecipe(engine.getItemStack(Engine_TEs.adjustable_electric_engine),
					"BCB", "EEE", "MTM",
					'B', anyCharge(IC2Items.getItem("lapotron_crystal")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'C', IC2Items.getItem("crafting", "advanced_circuit"),
					'M', IC2Items.getItem("resource", "advanced_machine"),
					'T', IC2Items.getItem("te", "hv_transformer"));
		}

		if (readerMJ != null && BCItems.Core.GEAR_GOLD != null && BCTransportItems.pipePowerWood != null) {
			Collection<ItemStack> pipes = new HashSet<>();

			for (Item pipe : new Item[] {BCTransportItems.pipePowerCobble, BCTransportItems.pipePowerStone,
					BCTransportItems.pipePowerQuartz, BCTransportItems.pipePowerGold, BCTransportItems.pipePowerSandstone}) {
				if (pipe != null) {
					pipes.add(new ItemStack(pipe));
				}
			}

			if (!pipes.isEmpty()) {
				Recipes.advRecipes.addRecipe(new ItemStack(readerMJ),
						" D ", "PGP", "p p",
						'D', Items.GLOWSTONE_DUST,
						'G', BCItems.Core.GEAR_GOLD,
						'P', pipes,
						'p', BCTransportItems.pipePowerWood);
			}
		}

		//BuildCraft bounces the loading of Markdown into XML anyway, we'll just go straight there thank you.
		GuideManager.PAGE_LOADERS.put("xml", XmlPageLoader.INSTANCE);

		//BuildCraft Lib loads pages in post-init, but it also loads first, so we do this here
		XmlPageLoader.TAG_FACTORIES.put("engineLink", tag -> {
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
	}
	
	private static ItemStack anyCharge(ItemStack stack) {
		return StackUtil.copyWithWildCard(stack);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		CustomRotationHelper.INSTANCE.registerHandler(engine, (world, pos, state, side) -> {
			TileEntity te = world.getTileEntity(pos);

			return te instanceof TileEntityEngine && ((TileEntityEngine) te).trySpin(side.getOpposite()) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
		});
	}


	public static class Conversion {
		static double MJperEU = MjAPI.MJ * 2 / 5;

		public static double MJtoEU(long microjoules) {
			return microjoules / MJperEU;
		}

		public static long EUtoMJ(double EU) {
			return (long) (EU * MJperEU);
		}
	}
}