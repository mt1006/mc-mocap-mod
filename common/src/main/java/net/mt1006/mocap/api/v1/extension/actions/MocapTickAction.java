package net.mt1006.mocap.api.v1.extension.actions;

public interface MocapTickAction extends MocapAction
{
	int getTickCount();

	boolean endsTick();
}
