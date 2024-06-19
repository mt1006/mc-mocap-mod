package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetEffectColor implements ComparableAction
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

	public SetEffectColor(RecordingFiles.Reader reader)
	{
		color = reader.readInt();
		ambience = reader.readBoolean();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return color != ((SetEffectColor)action).color ||
				ambience != ((SetEffectColor)action).ambience;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_EFFECT_COLOR.id);

		writer.addInt(color);
		writer.addBoolean(ambience);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_EFFECT_PARTICLES.set(ctx.entity,
				List.of(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color)));
		EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.set(ctx.entity, ambience);
		return Result.OK;
	}
}
