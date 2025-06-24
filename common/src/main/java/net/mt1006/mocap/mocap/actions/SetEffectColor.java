package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

import java.util.List;

public class SetEffectColor implements MocapStateAction
{
	private final int color;
	private final boolean ambience;

	public SetEffectColor(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			//TODO: fix it, it only takes color of first effect and ignores new particle types for 1.21 effects
			int color = 0;
			for (ParticleOptions particle : EntityData.LIVING_ENTITY_EFFECT_PARTICLES.valOrDef(entity, List.of()))
			{
				if (particle instanceof ColorParticleOption && particle.getType() == ParticleTypes.ENTITY_EFFECT)
				{
					ColorParticleOption colorParticle = (ColorParticleOption)particle;
					color = FastColor.ARGB32.colorFromFloat(colorParticle.getAlpha(),
							colorParticle.getRed(), colorParticle.getGreen(), colorParticle.getBlue());
					break;
				}
			}
			this.color = color;
			this.ambience = EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.valOrDef(entity, false);
		}
		else
		{
			this.color = 0;
			this.ambience = false;
		}
	}

	public SetEffectColor(Reader reader)
	{
		color = reader.readInt();
		ambience = reader.readBoolean();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return color != ((SetEffectColor)previousAction).color
				|| ambience != ((SetEffectColor)previousAction).ambience;
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addInt(color);
		writer.addBoolean(ambience);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_EFFECT_PARTICLES.set(ctx.getEntity(),
				List.of(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color)));
		EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.set(ctx.getEntity(), ambience);
		return Result.OK;
	}
}
