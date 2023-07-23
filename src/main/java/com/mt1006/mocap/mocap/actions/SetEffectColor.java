package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SetEffectColor implements ComparableAction
{
	private final int potionEffectColor;
	private final boolean effectAmbience;

	public SetEffectColor(Entity entity)
	{
		if (entity instanceof LivingEntity)
		{
			potionEffectColor = EntityData.LIVING_ENTITY_EFFECT_COLOR.valOrDef(entity, 0);
			effectAmbience = EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.valOrDef(entity, false);
		}
		else
		{
			potionEffectColor = 0;
			effectAmbience = false;
		}
	}

	public SetEffectColor(RecordingFiles.Reader reader)
	{
		potionEffectColor = reader.readInt();
		effectAmbience = reader.readBoolean();
	}

	@Override public boolean differs(ComparableAction action)
	{
		return potionEffectColor != ((SetEffectColor)action).potionEffectColor ||
				effectAmbience != ((SetEffectColor)action).effectAmbience;
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SET_EFFECT_COLOR.id);

		writer.addInt(potionEffectColor);
		writer.addBoolean(effectAmbience);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		EntityData.LIVING_ENTITY_EFFECT_COLOR.set(ctx.entity, potionEffectColor);
		EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.set(ctx.entity, effectAmbience);
		return Result.OK;
	}
}
