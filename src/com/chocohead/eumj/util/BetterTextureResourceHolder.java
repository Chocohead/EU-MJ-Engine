package com.chocohead.eumj.util;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.resource.TextureResourceHolder;

/**
 * A version of {@link TextureResourceHolder} that doesn't try translating images.
 *
 * <p> Also features:
 * <ul>
 * 	<li>No big warnings in the log about the translated image being missing.
 * 	<li>Useful constructor to avoid having to make a {@link ResourceLocation} externally.
 * 	<li>Lots of self confidence knowing it's better.
 * </ul>
 *
 * @author Chocohead
 */
public class BetterTextureResourceHolder extends TextureResourceHolder {
	public BetterTextureResourceHolder(String domain, String path) {
		this(new ResourceLocation(domain, path));
	}

	public BetterTextureResourceHolder(ResourceLocation location) {
		super(location);
	}

	public BetterTextureResourceHolder(String domain, String path, int width, int height) {
		this(new ResourceLocation(domain, path), width, height);
	}

	public BetterTextureResourceHolder(ResourceLocation location, int width, int height) {
		super(location, width, height);
	}

	@Override
	public ResourceLocation getLocationForLang(boolean useFallback) {
		return locationBase;
	}
}