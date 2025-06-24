package net.mt1006.mocap.mocap.actions;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.mocap.settings.Settings;

public class Hurt implements MocapAction
{
	public static final Hurt INSTANCE = new Hurt();

	private Hurt() {}

	public static Hurt fromReader(Reader ignore)
	{
		return INSTANCE;
	}

	public static void hurtEntity(Entity entity)
	{
		LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity)entity : null;
		entity.setInvulnerable(false);
		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }

		entity.invulnerableTime = 0;
		entity.hurt(new DamageSource(entity.level().damageSources().fellOutOfWorld().typeHolder()), 1.0f);

		if (livingEntity != null) { livingEntity.setHealth(livingEntity.getMaxHealth()); }
		entity.setInvulnerable(Settings.INVULNERABLE_PLAYBACK.val);
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addByte((byte)0); // dummy value - for future uses
	}

	@Override public Result execute(MocapActionContext ctx)
	{
		hurtEntity(ctx.getEntity());
		return Result.OK;
	}
}
