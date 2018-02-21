package com.chocohead.eumj.util;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.data.ModelVariableData;

/**
 * <p>Pull outs of methods from {@link TileEngineBase_BC8} to allow more abstract implementations</p>
 * 
 * TODO: Integrate support for {@link buildcraft.api.power.IEngine} once BuildCraft actually uses it
 *
 * @author Chocohead
 */
public interface IEngine {
	/**
	 * Used in engine and model code
	 *
	 * @return The direction the engine is facing (pointing towards the MJ acceptor)
	 */
	EnumFacing getFacing();

	/**
	 * Used in model and GUI code
	 *
	 * @return The current power stage of the engine
	 */
	EnumPowerStage getPowerStage();

	/**
	 * Used in model code
	 *
	 * @param partialTicks The current render partial ticks
	 *
	 * @return The current progress of the engine
	 */
	@SideOnly(Side.CLIENT)
	float getProgressClient(float partialTicks);

	/**
	 * Used in model code
	 *
	 * @return Get the current model data
	 */
	@SideOnly(Side.CLIENT)
	ModelVariableData getModelData();


	/**
	 * Used in GUI code
	 *
	 * @return Get the current MJ output
	 */
	@SideOnly(Side.CLIENT)
	long getActiveOutput();

	/**
	 * Used in GUI code
	 *
	 * @return Get the current engine power
	 */
	@SideOnly(Side.CLIENT)
	long getPower();

	/**
	 * Used in GUI code
	 *
	 * @return Get the current engine temperature
	 */
	@SideOnly(Side.CLIENT)
	double getHeat();

	/**
	 * Used in model updating and GUI code
	 *
	 * @return The current activity of the engine
	 */
	boolean getActive();
}