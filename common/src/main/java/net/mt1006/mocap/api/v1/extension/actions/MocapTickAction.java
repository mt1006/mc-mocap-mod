package net.mt1006.mocap.api.v1.extension.actions;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface MocapTickAction extends MocapAction
{
	@ApiStatus.Internal
	int getTickCount();

	@ApiStatus.Internal
	boolean endsTick();
}
