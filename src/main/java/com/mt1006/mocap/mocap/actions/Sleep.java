package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFiles;
import com.mt1006.mocap.mocap.playing.PlayingContext;
import com.mt1006.mocap.utils.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Sleep implements ComparableAction
{
	private final @Nullable BlockPos bedPostion;

	public Sleep(Entity entity)
	{
		if (entity instanceof LivingEntity) { bedPostion = ((LivingEntity)entity).getSleepingPos().orElse(null); }
		else { bedPostion = null; }
	}

	public Sleep(RecordingFiles.Reader reader)
	{
		if (reader.readBoolean()) { bedPostion = new BlockPos(reader.readInt(), reader.readInt(), reader.readInt()); }
		else { bedPostion = null; }
	}

	@Override public boolean differs(ComparableAction action)
	{
		if (bedPostion == null && ((Sleep)action).bedPostion == null) { return false; }
		if ((bedPostion == null) != (((Sleep)action).bedPostion == null)) { return true; }
		return bedPostion != null && !bedPostion.equals(((Sleep)action).bedPostion);
	}

	@Override public void write(RecordingFiles.Writer writer, @Nullable ComparableAction action)
	{
		if (action != null && !differs(action)) { return; }
		writer.addByte(Type.SLEEP.id);

		if (bedPostion != null)
		{
			writer.addBoolean(true);
			writer.addInt(bedPostion.getX());
			writer.addInt(bedPostion.getY());
			writer.addInt(bedPostion.getZ());
		}
		else
		{
			writer.addBoolean(false);
		}
	}

	@Override public Result execute(PlayingContext ctx)
	{
		if (!(ctx.entity instanceof LivingEntity)) { return Result.IGNORED; }
		new EntityData(ctx.entity, EntityData.LIVING_ENTITY_BED_POS, Optional.ofNullable(bedPostion)).broadcast(ctx);
		return Result.OK;
	}
}
