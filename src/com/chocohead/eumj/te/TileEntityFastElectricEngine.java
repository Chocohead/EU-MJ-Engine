package com.chocohead.eumj.te;

import buildcraft.api.mj.MjAPI;

public class TileEntityFastElectricEngine extends TileEntityElectricEngine {
	public TileEntityFastElectricEngine() {
		super(3);
	}

	@Override
	protected double getPistonSpeed() {
		return 0.07;
	}

	@Override
	protected long getOutput() {
		return 10 * MjAPI.MJ;
	}
}