package net.mt1006.mocap.command.converter;

import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.mocap.files.RecordingData;
import net.mt1006.mocap.mocap.files.RecordingFiles;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts 1.4-alpha-1 - 1.4-alpha-8 recording format to 1.4-alpha-9.
 * Will be removed in stable 1.4!
 */
public class AlphaConverter
{
	//TODO: [CONVERTER] remove
	public final double[] mainEntityPos;
	public final Map<Integer, double[]> posByEntity = new HashMap<>();

	public static boolean command(CommandOutput out, String name)
	{
		RecordingFile srcFile = RecordingFile.get(out, name);
		if (srcFile == null) { return false; }

		String backupName = name + "_backup";
		RecordingFile backupFile = RecordingFile.get(out, backupName);
		if (backupFile == null) { return false; }

		RecordingData data = new RecordingData();
		if (!data.load(out, srcFile, true)) { return out.sendFailure("misc.converter.failure"); }
		data.setCurrentVersion();

		if (srcFile.rename(out, backupFile) != null) { out.sendSuccess("misc.converter.renamed", name, backupName); }
		else { return out.sendFailure("misc.converter.failed_to_rename", name, backupName); }

		return RecordingFiles.save(out, srcFile.getFile(), name, data)
				? out.sendSuccess("misc.converter.success")
				: out.sendFailure("misc.converter.failed_to_save");
	}

	public AlphaConverter(Vec3 startPos)
	{
		mainEntityPos = new double[3];
		mainEntityPos[0] = startPos.x;
		mainEntityPos[1] = startPos.y;
		mainEntityPos[2] = startPos.z;
	}

	public double @Nullable[] getPosArray(@Nullable Integer entityId)
	{
		return entityId != null ? posByEntity.getOrDefault(entityId, null) : mainEntityPos;
	}
}
