package com.chocohead.eumj.te;

import net.minecraft.client.renderer.BufferBuilder;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;

import ic2.core.IC2;

import com.chocohead.eumj.MagicModelLoader.Engine;

public class EngineRender extends FastTESR<TileEntityEngine> {
	protected final Engine engine;

	public EngineRender(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void renderTileEntityFast(TileEntityEngine engine, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder vb) {
		IC2.platform.profilerStartSection("EU to MJ");
		IC2.platform.profilerStartSection(this.engine.name());

		IC2.platform.profilerStartSection("construction");
		vb.setTranslation(x, y, z);
		MutableQuad[] quads = this.engine.getEngineQuads(engine, partialTicks);

		IC2.platform.profilerEndStartSection("light");
		MutableQuad copy = new MutableQuad(0, null);
		int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
		int light_block = (lightc >> 4) & 15;
		int light_sky = (lightc >> 20) & 15;

		IC2.platform.profilerEndStartSection("render");
		for (MutableQuad q : quads) {
			copy.copyFrom(q);
			copy.maxLighti(light_block, light_sky);
			copy.multShade();
			copy.render(vb);
		}
		vb.setTranslation(0, 0, 0);

		IC2.platform.profilerEndSection();
		IC2.platform.profilerEndSection();
		IC2.platform.profilerEndSection();
	}
}