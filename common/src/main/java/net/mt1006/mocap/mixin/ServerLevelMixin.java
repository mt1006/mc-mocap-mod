package net.mt1006.mocap.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.mt1006.mocap.mocap.actions.BreakBlockProgress;
import net.mt1006.mocap.mocap.recording.RecordingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin
{
	@Inject(method = "destroyBlockProgress", at = @At(value = "HEAD"))
	public void destroyBlockProgress(int id, BlockPos blockPos, int progress, CallbackInfo ci)
	{
		if (RecordingManager.isActive())
		{
			RecordingManager.byRecordedPlayer(id).forEach((ctx) -> ctx.addAction(new BreakBlockProgress(blockPos, progress)));
		}
	}
}
