package com.mt1006.mocap.mixin;

import com.mt1006.mocap.mocap.actions.BreakBlockProgress;
import com.mt1006.mocap.mocap.recording.Recording;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerLevelMixin
{
	@Inject(method = "destroyBlockProgress", at = @At(value = "HEAD"))
	public void destroyBlockProgress(int id, BlockPos blockPos, int progress, CallbackInfo callbackInfo)
	{
		if (Recording.state == Recording.State.RECORDING && Recording.player.getId() == id)
		{
			new BreakBlockProgress(blockPos, progress).write(Recording.writer);
		}
	}
}
