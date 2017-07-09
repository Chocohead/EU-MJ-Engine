package com.chocohead.eumj.te;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.data.ModelVariableData;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Redstone;
import ic2.core.network.GuiSynced;

import com.chocohead.eumj.util.IEngine;
import com.chocohead.eumj.util.VeryOrderedEnumMap;

/**
 * A version of {@link TileEngineBase_BC8} but extending {@link TileEntityBlock}
 *
 * @author Chocohead
 */
public abstract class TileEntityEngine extends TileEntityInventory implements IHasGui, IEngine {
	public enum Progress {
		START, IN, OUT;

		public boolean isMoving() {
			return this != START;
		}

		static final Progress[] VALUES = values();
	}

	private static final Map<EnumFacing, List<AxisAlignedBB>> AABBs = makeAABBmap();

	protected final Redstone redstone;
	protected final IMjConnector mjConnector = createConnector();
	protected final MjCapabilityHelper mjCaps = new MjCapabilityHelper(mjConnector);

	@SideOnly(Side.CLIENT)
	protected final ModelVariableData modelData = new ModelVariableData();


	@GuiSynced
	protected double heat = TileEngineBase_BC8.MIN_HEAT;
	protected EnumPowerStage powerStage = EnumPowerStage.BLUE;

	@GuiSynced
	protected long power;
	protected Progress movement = Progress.START;
	protected float progress;
	@SideOnly(Side.CLIENT)
	protected float lastProgress;


	public TileEntityEngine() {
		redstone = addComponent(new Redstone(this));
	}

	private static Map<EnumFacing, List<AxisAlignedBB>> makeAABBmap() {
		Map<EnumFacing, List<AxisAlignedBB>> out = new EnumMap<>(EnumFacing.class);

		out.put(EnumFacing.DOWN,  Arrays.asList(new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.0,  0.25, 0.75, 0.5,  0.75)));
		out.put(EnumFacing.UP,    Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.25, 0.5,  0.25, 0.75, 1.0,  0.75)));
		out.put(EnumFacing.NORTH, Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.25, 0.0,  0.75, 0.75, 0.5)));
		out.put(EnumFacing.SOUTH, Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), new AxisAlignedBB(0.25, 0.25, 0.5,  0.75, 0.75, 1.0)));
		out.put(EnumFacing.WEST,  Arrays.asList(new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.0,  0.25, 0.25, 0.5,  0.75, 0.75)));
		out.put(EnumFacing.EAST,  Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), new AxisAlignedBB(0.5,  0.25, 0.25, 1.0,  0.75, 0.75)));

		return out;
	}

	protected abstract IMjConnector createConnector();
	// << Constructor + Fields

	// NBT + Loading >>
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setDouble("heat", heat);
		nbt.setLong("power", power);
		nbt.setFloat("progress", progress);
		nbt.setByte("movement", (byte) movement.ordinal());

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		heat = nbt.getDouble("heat");
		power = nbt.getLong("power");
		progress = nbt.getFloat("progress");
		movement = Progress.VALUES[nbt.getByte("movement") % Progress.VALUES.length];
	}

	private boolean isFacingMJ(EnumFacing dir) {
		TileEntity neighbour = world.getTileEntity(pos.offset(dir));
		if (neighbour == null) return false;

		IMjConnector other = neighbour.getCapability(MjAPI.CAP_CONNECTOR, dir.getOpposite());
		if (other == null) return false;

		return mjConnector.canConnect(other) && other.canConnect(mjConnector);
	}

	protected EnumFacing spin(EnumFacing start) {
		assert start != null; //Don't pass null, it's not cool.

		for (EnumFacing facing : VeryOrderedEnumMap.loopFrom(VanillaRotationHandlers.ROTATE_FACING, start)) {
			if (isFacingMJ(facing)) return facing;
		}

		return null;
	}

	public boolean trySpin(EnumFacing start) {
		EnumFacing spun = spin(start);

		if (spun != null && getFacing() != spun) {
			setFacing(spun);

			return true;
		} else
			return false;
	}

	@Override
	protected EnumFacing getPlacementFacing(EntityLivingBase placer, EnumFacing placerFacing) {
		/*EnumFacing natural = super.getPlacementFacing(placer, placerFacing).getOpposite();
		if (isFacingMJ(natural)) return natural;

		for (EnumFacing facing : getSupportedFacings().stream().filter(facing -> facing != natural).collect(Collectors.toSet())) {
			if (isFacingMJ(natural)) return facing;
		}*/

		EnumFacing spun = spin(super.getPlacementFacing(placer, placerFacing).getOpposite());
		return spun != null ? spun : EnumFacing.UP;
	}

	@Override
	public List<String> getNetworkedFields() {
		List<String> out = super.getNetworkedFields();

		out.add("progress");
		out.add("powerStage");

		return out;
	}
	// << NBT + Loading

	// Engine Logic >>
	// Power >>>
	protected double getPowerLevel() {
		return power / (double) getMaxPower();
	}

	protected abstract long getMaxPower();
	// <<< Power
	// Heat >>>
	protected double getHeatLevel() {
		return (heat - TileEngineBase_BC8.MIN_HEAT) / (TileEngineBase_BC8.MAX_HEAT - TileEngineBase_BC8.MIN_HEAT);
	}

	protected void updateHeatLevel() {
		heat = ((TileEngineBase_BC8.MAX_HEAT - TileEngineBase_BC8.MIN_HEAT) * getPowerLevel()) + TileEngineBase_BC8.MIN_HEAT;
	}
	// <<< Heat
	// Power Stage + Speed >>>
	protected EnumPowerStage computePowerStage() {
		double heatLevel = getHeatLevel();

		if (heatLevel < 0.25F) return EnumPowerStage.BLUE;
		else if (heatLevel < 0.5F) return EnumPowerStage.GREEN;
		else if (heatLevel < 0.75F) return EnumPowerStage.YELLOW;
		else if (heatLevel < 0.85F) return EnumPowerStage.RED;
		else return EnumPowerStage.OVERHEAT;
	}

	@Override
	public EnumPowerStage getPowerStage() {
		if (!world.isRemote) {
			EnumPowerStage newStage = computePowerStage();

			if (powerStage != newStage) {
				powerStage = newStage;
				IC2.network.get(true).updateTileEntityField(this, "powerStage");
			}
		}

		return powerStage;
	}

	protected double getPistonSpeed() {
		if (!world.isRemote)
			return Math.max(0.16 * getHeatLevel(), 0.01);
		else {
			switch (getPowerStage()) {
			case BLUE:
				return 0.02;
			case GREEN:
				return 0.04;
			case YELLOW:
				return 0.08;
			case RED:
				return 0.16;
			default:
				return 0;
			}
		}
	}
	// <<< Power Stage + Speed
	// Engine Activity >>>
	protected void engineUpdate() {
		if (!redstone.hasRedstoneInput() && power > 0) {
			power = Math.max(power - 1, 0);
		}
	}

	protected boolean canMove() {
		return redstone.hasRedstoneInput();
	}
	// <<< Engine Activity
	// Energy >>>
	public abstract long maxPowerExtracted();

	public IMjReceiver getReceiverToPower(TileEntity tile, EnumFacing side) {
		if (tile == null) return null;

		IMjReceiver rec = tile.getCapability(MjAPI.CAP_RECEIVER, side.getOpposite());
		return rec != null && rec.canConnect(mjConnector) ? rec : null;
	}

	public void addPower(long microJoules) {
		power = Math.min(power + microJoules, getMaxPower());

		if (getPowerStage() == EnumPowerStage.OVERHEAT) {
			// TODO: turn engine off
		}
	}

	public long extractPower(long min, long max, boolean doExtract) {
		if (power < min)
			return 0;

		long actualMax = Math.min(maxPowerExtracted(), max);
		if (actualMax < min)
			return 0;

		long extracted;
		if (power >= actualMax) {
			extracted = actualMax;

			if (doExtract) {
				power -= actualMax;
			}
		} else {
			extracted = power;

			if (doExtract) {
				power = 0;
			}
		}

		return extracted;
	}

	protected long getPowerToExtract(boolean doExtract) {
		TileEntity tile = world.getTileEntity(pos.offset(getFacing()));
		if (tile == null) return 0;

		if (tile.getClass() == getClass()) {
			TileEntityEngine other = (TileEntityEngine) tile;
			return other.getMaxPower() - power;
		} else {
			IMjReceiver receiver = getReceiverToPower(tile, getFacing());
			if (receiver == null)
				return 0;

			return extractPower(0, receiver.getPowerRequested(), doExtract);
			//return extractPower(receiver.getMinPowerReceived(), receiver.getMaxPowerReceived(), false); //TODO: This one
		}
	}

	protected void sendPower() {
		TileEntity tile = world.getTileEntity(pos.offset(getFacing()));
		if (tile == null)
			return;

		if (getClass() == tile.getClass()) {
			TileEntityEngine other = (TileEntityEngine) tile;

			if (getFacing() == other.getFacing()) {
				other.power += extractPower(0, power, true);
			}
		} else {
			IMjReceiver receiver = getReceiverToPower(tile, getFacing());

			if (receiver != null) {
				long extracted = getPowerToExtract(true);

				if (extracted > 0) {
					long excess = receiver.receivePower(extracted, false);
					extractPower(extracted - excess, extracted - excess, true);
				}
			}
		}
	}

	// <<< Energy
	// Updating >>>
	@Override
	protected void updateEntityServer() {
		super.updateEntityServer();

		if (!redstone.hasRedstoneInput() && power > 0) {
			power = Math.max(power - MjAPI.MJ, 0);
		}

		updateHeatLevel();
		boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;
		engineUpdate();

		if (movement.isMoving()) {
			progress += getPistonSpeed();

			if (progress > 0.5 && movement == Progress.IN) {
				movement = Progress.OUT;
				sendPower();
			} else if (progress >= 1) {
				progress = 0;
				movement = Progress.START;
			}
		} else if (canMove() && getPowerToExtract(false) > 0) {
			movement = Progress.IN;
			setActive(true);
		} else {
			setActive(false);
		}

		if (!overheat) {
			burn();
		}
	}

	protected abstract void burn();
	// <<< Updating
	// << Engine Logic

	// Capabilities >>
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (facing == getFacing()) return mjCaps.hasCapability(capability, facing) ||
				(capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && super.hasCapability(capability, facing));
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (facing == getFacing()) {
			T out = mjCaps.getCapability(capability, facing);
			if (out != null) return out;
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return null;
		}

		return super.getCapability(capability, facing);
	}
	// << Capabilities

	// Client >>
	@Override
	@SideOnly(Side.CLIENT)
	public ModelVariableData getModelData() {
		return modelData;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public long getPower() {
		return power;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getHeat() {
		return heat;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getProgressClient(float partialTicks) {
		float last = lastProgress;
		float now = progress;

		if (last > 0.5 && now < 0.5) {
			now += 1;
		}

		return (last * (1 - partialTicks) + now * partialTicks) % 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void updateEntityClient() {
		super.updateEntityClient();

		lastProgress = progress;

		if (getActive()) {
			progress += getPistonSpeed();

			if (progress >= 1) {
				progress = 0;
			}
		} else if (progress > 0) {
			progress -= 0.01f;
		}

		modelData.tick();
	}

	@Override //Not actually only client side, but affects rendering
	protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
		return AABBs.get(getFacing());
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected boolean shouldSideBeRendered(EnumFacing side, BlockPos otherPos) {
		return false;
	}

	@Override
	public boolean canRenderBreaking() {
		return true; //Without this, trying to break an engine will cause a rendering crash
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasFastRenderer() {
		return true;
	}

	@Override //Not only client side either, but only the client has sounds
	protected SoundType getBlockSound(Entity entity) {
		return SoundType.METAL;
	}
	// << Client

	// IHasGUI >>
	@Override
	public void onGuiClosed(EntityPlayer player) {
	}
}