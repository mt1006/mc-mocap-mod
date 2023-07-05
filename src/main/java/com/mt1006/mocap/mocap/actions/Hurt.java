package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Hurt implements Action
{
	public static final byte DUMMY = 0; // for future uses

	public Hurt() {}

	public Hurt(RecordingFiles.Reader reader)
	{
		reader.readByte();
	}

	public static void hurtEntity(Entity entity)
	{
		LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity)entity : null;
		entity.setInvulnerable(false);
		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }

		// "out_of_world" damage type is used as it bypasses spawn invulnerability
		entity.hurt(DamageSource.OUT_OF_WORLD, 1.0f);

		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }
		entity.setInvulnerable(true);
	}

	public void write(RecordingFiles.Writer writer)
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
