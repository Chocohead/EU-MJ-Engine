package com.chocohead.eumj.util;

import java.util.Iterator;
import java.util.function.Function;

import buildcraft.lib.misc.collect.OrderedEnumMap;

/**
 * Utility class for iterating {@link OrderedEnumMap}s.
 *
 * @author Chocohead
 */
public class VeryOrderedEnumMap {
	private static <E extends Enum<E>> Iterator<E> makeIterator(E start, Function<E, E> mover) {
		return new Iterator<E>() {
			private boolean first = true;
			private E current = start;

			@Override
			public boolean hasNext() {
				return first || mover.apply(current) != start;
			}

			@Override
			public E next() {
				if (first) {
					first = false;

					return current;
				} else
					return current = mover.apply(current);
			}
		};
	}

	/**
	 * Loops forwards from start back to it.
	 *
	 * @param start The value to start from
	 *
	 * @return An iterator that will loop from the start back to it
	 */
	public static <E extends Enum<E>> Iterable<E> loopFrom(OrderedEnumMap<E> map, E start) {
		return () -> makeIterator(start, map::next);
	}

	/**
	 * Loops backwards from start to it.
	 *
	 * @param start The value to start from
	 *
	 * @return An iterator that will loop backwards from the start to it
	 */
	public static <E extends Enum<E>> Iterable<E> loopBackFrom(OrderedEnumMap<E> map, E start) {
		return () -> makeIterator(start, map::previous);
	}
}