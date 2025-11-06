package net.mt1006.mocap.mixin.fields;

import net.minecraft.world.entity.animal.coppergolem.CopperGolemState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.IntFunction;

@Mixin(CopperGolemState.class)
public interface CopperGolemStateFields
{
	@Accessor static IntFunction<CopperGolemState> getBY_ID() { return null; }
	@Invoker int callId();
}
