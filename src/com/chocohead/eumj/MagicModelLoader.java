package com.chocohead.eumj;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.energy.BCEnergyModels;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableString;
import buildcraft.lib.misc.data.ModelVariableData;

import com.chocohead.eumj.util.IEngine;


/**
 * As very strongly inspired from {@link BCEnergyModels}
 *
 * @author Chocohead
 */
public class MagicModelLoader {
	static final FunctionContext fnCtx = DefaultContexts.createWithAll();

	static final NodeVariableDouble ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
	static final NodeVariableString ENGINE_STAGE = fnCtx.putVariableString("stage");
	static final NodeVariableString ENGINE_FACING = fnCtx.putVariableString("facing");

	public enum Engine {
		SLOW_ELECTRIC_ENGINE, REGULAR_ELECTRIC_ENGINE, FAST_ELECTRIC_ENGINE, QUICK_ELECTRIC_ENGINE, ADJUSTABLE_ELECTRIC_ENGINE;

		private final ModelHolderVariable model;
		private final ModelResourceLocation item;


		private Engine() {
			model = new ModelHolderVariable(EngineMod.MODID+":models/block/"+name().toLowerCase(Locale.ENGLISH)+".json", fnCtx);
			item = new ModelResourceLocation(new ResourceLocation(EngineMod.MODID, "models/item/"+name().toLowerCase(Locale.ENGLISH)), "inventory");
		}

		@SideOnly(Side.CLIENT)
		public MutableQuad[] getEngineQuads(IEngine tile, float partialTicks) {
			ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
			ENGINE_STAGE.value = tile.getPowerStage().getModelName();
			ENGINE_FACING.value = tile.getFacing().getName();

			ModelVariableData modelData = tile.getModelData();
			if (modelData.hasNoNodes()) {
				modelData.setNodes(model.createTickableNodes());
			}
			modelData.refresh();

			return model.getCutoutQuads();
		}

		public ModelResourceLocation getItemLocation() {
			return item;
		}


		@SideOnly(Side.CLIENT)
		ITickableNode[] createTickableNodes() {
			return model.createTickableNodes();
		}

		@SideOnly(Side.CLIENT)
		MutableQuad[] getCutoutQuads() {
			return model.getCutoutQuads();
		}
	}


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		ENGINE_PROGRESS.value = 0.2;
		ENGINE_STAGE.value = EnumPowerStage.GREEN.getModelName();
		ENGINE_FACING.value = EnumFacing.UP.getName();

		IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		ModelVariableData varData = new ModelVariableData();

		for (Engine engine : Engine.values()) {
			varData.setNodes(engine.createTickableNodes());
			varData.tick();
			varData.refresh();

			List<BakedQuad> quads = new ArrayList<>();
			for (MutableQuad quad : engine.getCutoutQuads()) {
				quads.add(quad.toBakedItem());
			}

			modelRegistry.putObject(engine.getItemLocation(), new ModelItemSimple(quads, ModelItemSimple.TRANSFORM_BLOCK));
		}
	}
}