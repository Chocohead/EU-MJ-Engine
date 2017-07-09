package com.chocohead.eumj.item;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.mutable.MutableDouble;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

import ic2.core.ContainerFullInv;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.util.StackUtil;

public class ContainerMeasurerMJ extends ContainerFullInv<HandHeldReaderMJ> implements IWorldTickCallback {
	static final short HEIGHT = 211;

	protected final Map<EnumFacing, MutableDouble> energy = new EnumMap<>(EnumFacing.class);
	protected final double[] averageEnergy = new double[EnumFacing.VALUES.length];

	protected ItemStack watchedStack;
	protected long totalTicks = 1;
	protected int ticks = 0;

	public ContainerMeasurerMJ(EntityPlayer player, HandHeldReaderMJ base) {
		super(player, base, HEIGHT);

		if (!player.world.isRemote) {
			IC2.tickHandler.requestContinuousWorldTick(player.world, this);

			for (Entry<EnumFacing, Section> entry : base.caps.entrySet()) {
				energy.put(entry.getKey(), new MutableDouble(entry.getValue().powerAverage.getAverage()));
			}

			watchedStack = StackUtil.getPickStack(player.world, base.pos, player.world.getBlockState(base.pos), player);
			IC2.tickHandler.requestSingleWorldTick(player.world, (world) -> IC2.network.get(true).sendContainerField(this, "watchedStack"));
			//Ensure the client has the container open, before trying to set the picked stack ^
		}
	}

	@Override
	public void onTick(World world) {
		assert !world.isRemote;
		assert base.caps != null;

		boolean sending = false;
		if (++ticks >= 20) {
			ticks = 0;

			sending = true;
		}

		totalTicks++;
		for (Entry<EnumFacing, Section> entry : base.caps.entrySet()) {
			MutableDouble value = energy.get(entry.getKey());
			value.add(entry.getValue().powerAverage.getAverage());

			if (sending) {
				averageEnergy[entry.getKey().getIndex()] = value.doubleValue() / totalTicks;
			}
		}

		if (sending) {
			IC2.network.get(true).sendContainerFields(this, "averageEnergy", "totalTicks");
		}
	}

	public double getAverageEnergy(EnumFacing facing) {
		return averageEnergy[facing.getIndex()];
	}

	public long getTime() {
		return totalTicks / 20;
	}

	public ItemStack getStack() {
		return watchedStack;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		if (!player.world.isRemote) {
			IC2.tickHandler.removeContinuousWorldTick(player.world, this);
		}
	}
}