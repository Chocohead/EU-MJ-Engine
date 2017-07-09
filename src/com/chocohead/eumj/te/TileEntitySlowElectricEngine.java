package com.chocohead.eumj.te;

import buildcraft.api.mj.MjAPI;

public class TileEntitySlowElectricEngine extends TileEntityElectricEngine {
	public TileEntitySlowElectricEngine() {
		super(1);
	}

	@Override
	protected double getPistonSpeed() {
		return 0.02;
	}

	@Override
	protected long getOutput() {
		return 1 * MjAPI.MJ;
	}
}