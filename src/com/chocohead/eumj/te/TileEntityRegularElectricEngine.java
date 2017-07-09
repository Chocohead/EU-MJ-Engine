package com.chocohead.eumj.te;

import buildcraft.api.mj.MjAPI;

public class TileEntityRegularElectricEngine extends TileEntityElectricEngine {
	public TileEntityRegularElectricEngine() {
		super(2);
	}

	@Override
	protected double getPistonSpeed() {
		return 0.05;
	}

	@Override
	protected long getOutput() {
		return 5 * MjAPI.MJ;
	}
}