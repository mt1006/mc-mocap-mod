package net.mt1006.mocap.mocap.files;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.playable.MocapSavedRecording;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.io.CommandOutput;
import net.mt1006.mocap.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecordingFiles
{
	public static final byte VERSION = MocapMod.RECORDING_FORMAT_VERSION;
	private static final int ALT_NAME_MAX_I = 128;
	public static final MocapAction.Reader DUMMY_READER = new DummyReader();

	public static boolean save(CommandOutput commandOutput, File recordingFile, String name, RecordingData data)
	{
		try
		{
			// double-check to make sure it won't override existing file
			if (recordingFile.exists())
			{
				commandOutput.sendFailure("recording.save.already_exists");
				return false;
			}

			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(recordingFile));
			data.save(stream);
			stream.close();
		}
		catch (IOException e)
		{
			commandOutput.sendException(e, "recording.save.error");
			return false;
		}

		CommandSuggestions.inputSet.add(name);
		return true;
	}

	public static boolean copy(CommandOutput commandOutput, String srcName, String destName)
	{
		File srcFile = Files.getRecordingFile(commandOutput, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getRecordingFile(commandOutput, destName);
		if (destFile == null) { return false; }

		try { FileUtils.copyFile(srcFile, destFile); }
		catch (IOException e)
		{
			commandOutput.sendException(e, "recordings.copy.failed");
			return false;
		}

		CommandSuggestions.inputSet.add(destName);
		commandOutput.sendSuccess("recordings.copy.success");
		return true;
	}

	public static boolean rename(CommandOutput commandOutput, String oldName, String newName)
	{
		File oldFile = Files.getRecordingFile(commandOutput, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getRecordingFile(commandOutput, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			commandOutput.sendFailure("recordings.rename.failed");
			return false;
		}

		CommandSuggestions.inputSet.remove(oldName);
		CommandSuggestions.inputSet.add(newName);
		commandOutput.sendSuccess("recordings.rename.success");
		return true;
	}

	public static boolean remove(CommandOutput commandOutput, String name)
	{
		File recordingFile = Files.getRecordingFile(commandOutput, name);
		if (recordingFile == null) { return false; }

		if (!recordingFile.delete())
		{
			commandOutput.sendFailure("recordings.remove.failed");
			return false;
		}

		CommandSuggestions.inputSet.remove(name);
		commandOutput.sendSuccess("recordings.remove.success");
		return true;
	}

	public static boolean info(CommandOutput commandOutput, String name)
	{
		Info info = Info.load(commandOutput, name);
		if (info == null) { return false; }

		commandOutput.sendSuccess("recordings.info.info");
		commandOutput.sendSuccess("file.info.name", name);
		if (!Files.printVersionInfo(commandOutput, VERSION, info.version, info.experimental)) { return true; }

		commandOutput.sendSuccess("recordings.info.length", String.format("%.2f", info.lengthInTicks / 20.0), info.lengthInTicks);
		commandOutput.sendSuccess("recordings.info.size", String.format("%.2f", info.sizeInBytes / 1024.0), info.sizeInOps);

		String xStr = String.format(Locale.US, "%.2f", info.startPos.x);
		String yStr = String.format(Locale.US, "%.2f", info.startPos.y);
		String zStr = String.format(Locale.US, "%.2f", info.startPos.z);
		MutableComponent tpSuggestionComponent = Utils.getEventComponent(ClickEvent.Action.SUGGEST_COMMAND,
				String.format("/tp @p %s %s %s", xStr, yStr, zStr), String.format("%s %s %s", xStr, yStr, zStr));
		tpSuggestionComponent.withStyle(Style.EMPTY.withUnderlined(true));
		commandOutput.sendSuccess("recordings.info.start_pos", tpSuggestionComponent);

		if (info.assignedPlayerName != null) { commandOutput.sendSuccess("recordings.info.player_name_assigned.yes", info.assignedPlayerName); }
		else { commandOutput.sendSuccess("recordings.info.player_name_assigned.no"); }

		commandOutput.sendSuccess(info.legacyEndsWithDeath ? "recordings.info.dies.yes" : "recordings.info.dies.no");
		return true;
	}

	public static @Nullable List<String> list()
	{
		if (!Files.initialized) { return null; }

		String[] fileList = Files.recordingsDirectory.list(Files::isRecordingFile);
		if (fileList == null) { return null; }

		ArrayList<String> recordings = new ArrayList<>(fileList.length);
		for (String filename : fileList)
		{
			recordings.add(filename.substring(0, filename.lastIndexOf('.')));
		}

		Collections.sort(recordings);
		return recordings;
	}

	public static @Nullable String findAlternativeName(String name)
	{
		if (name.isEmpty()) { return null; }

		int firstDigit = name.length();
		int lastDigit = name.length() - 1;
		for (int i = lastDigit; i >= 0; i--)
		{
			char ch = name.charAt(i);
			if (ch >= '0' && ch <= '9') { firstDigit = i; }
			else { break; }
		}

		if (firstDigit > lastDigit) { return null; }
		String prefix = name.substring(0, firstDigit);
		int suffix = Integer.parseInt(name.substring(firstDigit, lastDigit + 1));

		for (int i = suffix + 1; i <= suffix + ALT_NAME_MAX_I ; i++)
		{
			String possibleName = String.format("%s%d", prefix, i);
			if (!CommandSuggestions.inputSet.contains(possibleName)) { return possibleName; }
		}
		return null;
	}

	public record Info(
			int version,
			boolean experimental,
			long lengthInTicks,
			long sizeInBytes,
			long sizeInOps,
			Vec3 startPos,
			@Nullable String assignedPlayerName,
			boolean legacyEndsWithDeath) implements MocapSavedRecording.Info
	{
		public static @Nullable RecordingFiles.Info load(CommandOutput commandOutput, String name)
		{
			RecordingData recording = new RecordingData();
			if (!recording.load(commandOutput, name) && recording.version <= VERSION)
			{
				commandOutput.sendFailure("recordings.info.failed");
				return null;
			}

			return new Info(
					recording.version,
					recording.experimentalVersion,
					recording.tickCount,
					recording.fileSize,
					recording.actions.size(),
					recording.startPos,
					recording.playerName,
					recording.endsWithDeath);
		}
	}

	public static class Writer implements MocapAction.Writer
	{
		private final ArrayList<Byte> recording = new ArrayList<>();

		@Override public void addByte(byte val)
		{
			recording.add(val);
		}

		@Override public void addShort(short val)
		{
			recording.add((byte)(val >> 8));
			recording.add((byte)val);
		}

		@Override public void addInt(int val)
		{
			recording.add((byte)(val >> 24));
			recording.add((byte)(val >> 16));
			recording.add((byte)(val >> 8));
			recording.add((byte)val);
		}

		@Override public void addFloat(float val)
		{
			for (byte b : floatToByteArray(val))
			{
				recording.add(b);
			}
		}

		@Override public void addDouble(double val)
		{
			for (byte b : doubleToByteArray(val))
			{
				recording.add(b);
			}
		}

		@Override public void addBoolean(boolean val)
		{
			recording.add(val ? (byte)1 : (byte)0);
		}

		@Override public void addString(String val)
		{
			byte[] bytes = val.getBytes(StandardCharsets.UTF_8);
			addPackedSize(bytes.length);
			for (byte b : bytes)
			{
				recording.add(b);
			}
		}

		@Override public void addVec3(Vec3 vec)
		{
			addDouble(vec.x);
			addDouble(vec.y);
			addDouble(vec.z);
		}

		@Override public void addBlockPos(BlockPos blockPos)
		{
			addInt(blockPos.getX());
			addInt(blockPos.getY());
			addInt(blockPos.getZ());
		}

		@Override public void addPackedSize(int size)
		{
			if (size < 255)
			{
				addByte((byte)size);
			}
			else
			{
				addByte((byte)255);
				addInt(size);
			}
		}

		public void copyToWriter(MocapAction.Writer writer)
		{
			recording.forEach(writer::addByte);
		}

		public int getSize()
		{
			return recording.size();
		}

		//TODO: remove?
		public List<Byte> getByteList()
		{
			return recording;
		}

		public byte[] toByteArray()
		{
			byte[] array = new byte[recording.size()];
			for (int i = 0; i < recording.size(); i++) { array[i] = recording.get(i); }
			return array;
		}

		private static byte[] floatToByteArray(float val)
		{
			int bits = Float.floatToIntBits(val);
			return new byte[] { (byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
		}

		private static byte[] doubleToByteArray(double val)
		{
			long bits = Double.doubleToLongBits(val);
			return new byte[] { (byte)(bits >> 56), (byte)(bits >> 48), (byte)(bits >> 40), (byte)(bits >> 32),
					(byte)(bits >> 24), (byte)(bits >> 16), (byte)(bits >> 8), (byte)bits };
		}
	}

	public static class FileReader implements MocapAction.Reader
	{
		private final byte[] recording;
		private boolean legacyString;
		public int offset = 0;

		public FileReader(byte[] recording, boolean legacyString)
		{
			this.recording = recording;
			this.legacyString = legacyString;
		}

		@Override public byte readByte()
		{
			return recording[offset++];
		}

		@Override public short readShort()
		{
			short retVal = (short)(((recording[offset] & 0xFF) << 8) | (recording[offset + 1] & 0xFF));
			offset += 2;
			return retVal;
		}

		@Override public int readInt()
		{
			int retVal = ((recording[offset] & 0xFF) << 24) | ((recording[offset + 1] & 0xFF) << 16) |
					((recording[offset + 2] & 0xFF) << 8) | (recording[offset + 3] & 0xFF);
			offset += 4;
			return retVal;
		}

		@Override public float readFloat()
		{
			float retVal = byteArrayToFloat(Arrays.copyOfRange(recording, offset, offset + 4));
			offset += 4;
			return retVal;
		}

		@Override public double readDouble()
		{
			double retVal = byteArrayToDouble(Arrays.copyOfRange(recording, offset, offset + 8));
			offset += 8;
			return retVal;
		}

		@Override public boolean readBoolean()
		{
			return recording[offset++] == 1;
		}

		@Override public String readString()
		{
			int len = legacyString ? readInt() : readPackedSize();
			String str = new String(recording, offset, len, StandardCharsets.UTF_8);
			offset += len;
			return str;
		}

		@Override public Vec3 readVec3()
		{
			return new Vec3(readDouble(), readDouble(), readDouble());
		}

		@Override public BlockPos readBlockPos()
		{
			return new BlockPos(readInt(), readInt(), readInt());
		}

		@Override public int readPackedSize()
		{
			int val = Byte.toUnsignedInt(readByte());
			return (val == 255) ? readInt() : val;
		}

		@Override public void shift(int val)
		{
			offset += val;
		}

		public void setStringMode(boolean legacyString)
		{
			this.legacyString = legacyString;
		}

		public boolean canRead()
		{
			return recording.length > offset;
		}

		public int getSize()
		{
			return recording.length;
		}

		private static float byteArrayToFloat(byte[] bytes)
		{
			int bits = (((int)bytes[0] & 0xFF) << 24) | (((int)bytes[1] & 0xFF) << 16) | (((int)bytes[2] & 0xFF) << 8) | ((int)bytes[3] & 0xFF);
			return Float.intBitsToFloat(bits);
		}

		private static double byteArrayToDouble(byte[] bytes)
		{
			long bits = (((long)bytes[0] & 0xFF) << 56) | (((long)bytes[1] & 0xFF) << 48) | (((long)bytes[2] & 0xFF) << 40) | (((long)bytes[3] & 0xFF) << 32) |
					(((long)bytes[4] & 0xFF) << 24) | (((long)bytes[5] & 0xFF) << 16) | (((long)bytes[6] & 0xFF) << 8) | ((long)bytes[7] & 0xFF);
			return Double.longBitsToDouble(bits);
		}
	}

	private static class DummyReader implements MocapAction.Reader
	{
		@Override public byte readByte() { return 0; }
		@Override public short readShort() { return 0; }
		@Override public int readInt() { return 0; }
		@Override public float readFloat() { return 0.0f; }
		@Override public double readDouble() { return 0.0; }
		@Override public boolean readBoolean() { return false; }
		@Override public String readString() { return ""; }
		@Override public Vec3 readVec3() { return Vec3.ZERO; }
		@Override public BlockPos readBlockPos() { return BlockPos.ZERO; }
		@Override public int readPackedSize() { return 0; }
		@Override public void shift(int val) {}
	}
}
