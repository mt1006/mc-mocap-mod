package net.mt1006.mocap.api.v1.controller.playable;

import net.mt1006.mocap.api.v1.controller.MocapFile;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapModifiers;
import net.mt1006.mocap.mocap.files.SceneData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MocapSceneFile extends MocapPlayable, MocapFile<MocapSceneFile>
{
	/**
	 * Add element to a scene.
	 * @param out log output
	 * @param playable element to be added
	 * @param modifiers element playback modifiers
	 * @return true if succeeded, false otherwise
	 * @see MocapSceneFile#add(CommandOutput, MocapSceneElement)
	 */
	default boolean add(CommandOutput out, MocapPlayable playable, MocapModifiers modifiers)
	{
		return add(out, new SceneData.Element(playable.getName(), modifiers));
	}

	/**
	 * Add element to a scene.
	 * @param out log output
	 * @param element element to be added
	 * @return true if succeeded, false otherwise
	 * @see MocapSceneFile#add(CommandOutput, MocapPlayable, MocapModifiers)
	 */
	boolean add(CommandOutput out, MocapSceneElement element);

	/**
	 * Returns immutable list of scene elements.
	 * List has proper order of elements, that means it corresponds to element IDs.
	 * Note that first element has ID 1, so to get list index you should
	 * subtract 1 from element ID.
	 * @param out log output
	 * @return immutable list of scene elements
	 */
	@Nullable List<? extends MocapSceneElement> getAll(CommandOutput out);

	/**
	 * Replace all scene elements with those from given list.
	 * List elements are copied, so modifying it after calling this method
	 * is safe, and won't affect scene.
	 * @param out log output
	 * @param elements new list of elements
	 * @return true if succeeded, false otherwise
	 */
	boolean replaceAll(CommandOutput out, List<? extends MocapSceneElement> elements);

	/**
	 * Remove all elements from a scene.
	 * @param out log output
	 * @return true if succeeded, false otherwise
	 */
	boolean clear(CommandOutput out);
}
