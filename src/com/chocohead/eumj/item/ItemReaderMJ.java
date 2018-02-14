package com.chocohead.eumj.item;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

import ic2.api.item.IBoxable;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.init.BlocksItems;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import com.chocohead.eumj.EngineMod;

public class ItemReaderMJ extends ItemIC2 implements IHandHeldInventory, IBoxable {
	private static final String NAME = "mj_reader";

	public ItemReaderMJ() {
		super(null);

		BlocksItems.registerItem(this, new ResourceLocation(EngineMod.MODID, NAME)).setUnlocalizedName(NAME).setCreativeTab(EngineMod.TAB);

		maxStackSize = 1;
		setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void registerModel(int meta, ItemName name, String extraName) {
		ModelLoader.setCustomModelResourceLocation(this, meta, new ModelResourceLocation(EngineMod.MODID+':'+NAME, null));
	}

	@Override
	public String getUnlocalizedName() {
		return EngineMod.MODID + super.getUnlocalizedName().substring(3);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (world.isRemote) return EnumActionResult.PASS;

		TileEntity te = world.getTileEntity(pos);
		if (te == null || hand == EnumHand.OFF_HAND) return EnumActionResult.FAIL;

		Map<EnumFacing, Section> caps = new EnumMap<>(EnumFacing.class);
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (te.hasCapability(MjAPI.CAP_CONNECTOR, facing)) {
				IMjConnector cap = te.getCapability(MjAPI.CAP_CONNECTOR, facing);

				if (cap instanceof Section) {
					//MJ really doesn't make it easy to work out average power flow
					//But the internal BuildCraft logic renders using averages, so we can just steal it
					//Ideally there should be a better (and more generic) way to do this though
					caps.put(facing, (Section) cap);
				}
			}
		}
		if (caps.isEmpty()) return EnumActionResult.FAIL;

		if (IC2.platform.launchGui(player, new HandHeldReaderMJ(player, StackUtil.get(player, hand), pos, caps)))
			//caps.forEach((facing, section) -> System.out.println(facing + ": " + section.powerAverage.getAverage()));
			return EnumActionResult.SUCCESS;

		return EnumActionResult.PASS;
	}

	@Override
	public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
		return new HandHeldReaderMJ(player, stack);
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack) {
		return true;
	}
}