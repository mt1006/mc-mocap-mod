package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Hurt implements Action
{
	public static final byte DUMMY = 0; // for future uses

	public Hurt(RecordingFiles.Reader reader)
	{
		reader.readByte();
	}

	public static void hurtEntity(Entity entity)
	{
		LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity)entity : null;
		entity.setInvulnerable(false);
		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }

		entity.invulnerableTime = 0;
		entity.hurt(new DamageSource(entity.level().damageSources().fellOutOfWorld().typeHolder()), 1.0f);

		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }
		entity.setInvulnerable(true);
	}

	public static void write(RecordingFiles.Writer writer)
	{
		writer.addByte(Type.HURT.id);
		writer.addByte(DUMMY);
	}

	@Override public Result execute(PlayingContext ctx)
	{
		hurtEntity(ctx.entity);
		return Result.OK;
	}
}
