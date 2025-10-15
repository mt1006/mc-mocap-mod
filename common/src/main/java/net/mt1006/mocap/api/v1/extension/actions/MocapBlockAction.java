package net.mt1006.mocap.api.v1.extension.actions;

public interface MocapBlockAction extends MocapAction
{
	//TODO: merge with normal action

	void preExecute(MocapBasicActionContext ctx);
}
