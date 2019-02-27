package com.chocohead.eumj.te;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import ic2.api.item.ITeBlockSpecialItem;

import ic2.core.block.ITeBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.ref.TeBlock.DefaultDrop;
import ic2.core.ref.TeBlock.HarvestTool;
import ic2.core.ref.TeBlock.ITePlaceHandler;
import ic2.core.util.Util;

import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.MagicModelLoader.Engine;

public enum Engine_TEs implements ITeBlock, ITeBlockSpecialItem {
	slow_electric_engine(TileEntitySlowElectricEngine.class, 0, Engine.SLOW_ELECTRIC_ENGINE, EnumRarity.COMMON),
	regular_electric_engine(TileEntityRegularElectricEngine.class, 1, Engine.REGULAR_ELECTRIC_ENGINE, EnumRarity.UNCOMMON),
	fast_electric_engine(TileEntityFastElectricEngine.class, 2, Engine.FAST_ELECTRIC_ENGINE, EnumRarity.RARE),
	quick_electric_engine(TileEntityQuickElectricEngine.class, 3, Engine.QUICK_ELECTRIC_ENGINE, EnumRarity.EPIC),
	adjustable_electric_engine(TileEntityAdjustableElectricEngine.class, 4, Engine.ADJUSTABLE_ELECTRIC_ENGINE, EnumRarity.EPIC);

	private Engine_TEs(Class<? extends TileEntityEngine> teClass, int itemMeta, Engine engine, EnumRarity rarity) {
		this.teClass = teClass;
		this.itemMeta = itemMeta;
		this.engine = engine;
		this.rarity = rarity;

		GameRegistry.registerTileEntity(teClass, EngineMod.MODID + ':' + getName());
	}

	@Override
	public boolean hasItem() {
		return true;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getId() {
		return itemMeta;
	}

	@Override
	public ResourceLocation getIdentifier() {
		return IDENTITY;
	}

	@Override
	public Class<? extends TileEntityBlock> getTeClass() {
		return teClass;
	}

	@Override
	public boolean hasActive() {
		return false;
	}

	@Override
	public float getHardness() {
		return 5F;
	}
	//Coincidentally, both IC2 and Buildcraft uses these as defaults
	@Override
	public float getExplosionResistance() {
		return 10F;
	}

	@Override
	public HarvestTool getHarvestTool() {
		return HarvestTool.Pickaxe;
	}

	@Override
	public DefaultDrop getDefaultDrop() {
		return DefaultDrop.Self;
	}

	@Override
	public boolean allowWrenchRotating() {
		return false;
	}

	@Override
	public Set<EnumFacing> getSupportedFacings() {
		return Util.allFacings;
	}

	@Override
	public EnumRarity getRarity() {
		return rarity;
	}

	@Override
	public Material getMaterial() {
		return Material.IRON;
	}

	@Override
	public boolean isTransparent() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	private void addRenderer() {
		ClientRegistry.bindTileEntitySpecialRenderer(teClass, new EngineRender(engine));
	}

	@Override
	public boolean doesOverrideDefault(ItemStack stack) {
		return true;
	}

	@Override
	public ModelResourceLocation getModelLocation(ItemStack stack) {
		return engine.getItemLocation();
	}

	/** Load dummyTe for each tile entity */
	public static void buildDummies(boolean client) {
		for (Engine_TEs block : Engine_TEs.VALUES) {
			if (block.teClass != null) {
				try {
					block.dummyTe = block.teClass.newInstance();

					if (client) {
						block.addRenderer();
					}
				} catch (Exception e) {
					if (Util.inDev()) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public TileEntityBlock getDummyTe() {
		return dummyTe;
	}

	private final Class<? extends TileEntityEngine> teClass;
	private final int itemMeta;
	private final Engine engine;
	private final EnumRarity rarity;
	private TileEntityBlock dummyTe;

	public static final Engine_TEs[] VALUES = values();
	public static final ResourceLocation IDENTITY = new ResourceLocation(EngineMod.MODID, "engines");
}