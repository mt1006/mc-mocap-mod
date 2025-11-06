package net.mt1006.mocap.mocap.actions.deprecated;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.utils.EntityData;

import java.util.List;

public class SetEffectColor implements MocapAction
{
	private final int color;
	private final boolean ambience;

	public SetEffectColor(Reader reader)
	{
		color = reader.readInt();
		ambience = reader.readBoolean();
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		//TODO: [CONVERTER] remove
		writer.addInt(color);
		writer.addBoolean(ambience);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity entity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_EFFECT_PARTICLES.set(entity, List.of(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color)));
		EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.set(entity, ambience);
		return Result.OK;
	}
}
