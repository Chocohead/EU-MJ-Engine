package com.chocohead.eumj.util;

import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable.IViewableGrid;

import ic2.api.recipe.IRecipeInput;

import ic2.core.recipe.AdvRecipe;

/**
 * A version of {@link AdvRecipe} that implements {@link IViewableGrid}
 * (thus can be used in the guide book).
 *
 * @author Chocohead
 */
public class AdvEngineRecipe extends AdvRecipe implements IViewableGrid {
	protected final boolean[][] shape;

	public AdvEngineRecipe(ItemStack result, Object... args) {
		super(result, args);

		Queue<String> shapes = new ArrayDeque<>(3);
		for (Object arg : args) {
			if (arg instanceof String) {
				shapes.add((String) arg);
			} else {
				break;
			}
		}
		assert !shapes.isEmpty() && shapes.size() <= 3;

		shape = new boolean[inputHeight][inputWidth];
		for (int y = 0; y < inputHeight; y++) {
			boolean[] section = shape[y];
			String part = shapes.poll();

			for (int x = 0; x < inputWidth; x++) {
				section[x] = part.charAt(x) != ' ';
			}
		}
	}

	public static ChangingItemStack makeForInput(IRecipeInput input) {
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(input.getInputs());
		return new ChangingItemStack(list);
	}

	@Override
	public ChangingItemStack[] getRecipeInputs() {
		/*ChangingItemStack[] out = new ChangingItemStack[input.length];

		for (int i = 0; i < out.length; i++) {
			out[i] = makeForInput(input[i]);
		}*/// <- Good for solid recipes, bad for ones with holes
		ChangingItemStack[] out = new ChangingItemStack[inputWidth * inputHeight];

		int i = 0, j = 0;
		for (boolean[] section : shape) {
			for (boolean item : section) {
				out[j++] = item ? makeForInput(input[i++]) : ChangingItemStack.create(ItemStack.EMPTY);
			}
		}

		return out;
	}

	@Override
	public ChangingItemStack getRecipeOutputs() {
		return ChangingItemStack.create(output);
	}

	@Override
	public int getRecipeWidth() {
		return inputWidth;
	}

	@Override
	public int getRecipeHeight() {
		return inputHeight;
	}
}