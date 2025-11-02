package net.mt1006.mocap.mocap.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.utils.EntityData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SetEffectParticles implements MocapStateAction
{
	private final Set<String> particleJsonSet = new TreeSet<>();
	private final boolean ambience;

	public SetEffectParticles(Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			this.ambience = false;
			return;
		}

		for (ParticleOptions particle : EntityData.LIVING_ENTITY_EFFECT_PARTICLES.valOrDef(entity, List.of()))
		{
			JsonElement jsonElement = ParticleTypes.CODEC.encodeStart(JsonOps.INSTANCE, particle).result().orElse(null);
			if (jsonElement == null) { continue; }

			particleJsonSet.add(jsonElement.toString());
		}

		this.ambience = EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.valOrDef(entity, false);
	}

	public SetEffectParticles(Reader reader)
	{
		int count = reader.readPackedSize();
		for (int i = 0; i < count; i++)
		{
			String particleJson = reader.readString();
			particleJsonSet.add(particleJson);
		}

		ambience = reader.readBoolean();
	}

	@Override public boolean differs(MocapStateAction previousAction)
	{
		return !particleJsonSet.equals(((SetEffectParticles)previousAction).particleJsonSet);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addPackedSize(particleJsonSet.size());
		particleJsonSet.forEach(writer::addString);

		writer.addBoolean(ambience);
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		if (!(ctx.getEntity() instanceof LivingEntity entity)) { return Result.IGNORED; }

		List<ParticleOptions> particles = new ArrayList<>();
		for (String jsonStr : particleJsonSet)
		{
			try
			{
				particles.add(ParticleTypes.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(jsonStr)).getOrThrow().getFirst());
			}
			catch (Exception ignore) {}
		}

		EntityData.LIVING_ENTITY_EFFECT_PARTICLES.set(entity, particles);
		EntityData.LIVING_ENTITY_EFFECT_AMBIENCE.set(ctx.getEntity(), ambience);
		return Result.OK;
	}
}
