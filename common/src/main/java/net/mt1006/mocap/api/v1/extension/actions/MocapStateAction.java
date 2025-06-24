package net.mt1006.mocap.api.v1.extension.actions;

public interface MocapStateAction extends MocapAction
{
	boolean differs(MocapStateAction previousAction);

	default boolean shouldBeInitialized()
	{
		return true;
	}
}
