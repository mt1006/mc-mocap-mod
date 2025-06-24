package net.mt1006.mocap.api.v1.extension.actions;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;

public interface MocapBlockAction extends MocapAction
{
	void preExecute(Entity entity, MocapPositionTransformer transformer);
}
