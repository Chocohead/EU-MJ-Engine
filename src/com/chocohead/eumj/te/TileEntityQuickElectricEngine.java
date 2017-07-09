package com.chocohead.eumj.te;

import buildcraft.api.mj.MjAPI;

public class TileEntityQuickElectricEngine extends TileEntityElectricEngine {
	public TileEntityQuickElectricEngine() {
		super(4);
	}

	@Override
	protected double getPistonSpeed() {
		return 0.09;
	}

	@Override
	protected long getOutput() {
		return 15 * MjAPI.MJ;
	}
}