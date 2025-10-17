package net.mt1006.mocap.mocap.files;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.v1.controller.config.MocapDimensionSource;
import net.mt1006.mocap.api.v1.controller.playable.MocapRecordingFile;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import net.mt1006.mocap.mocap.settings.Settings;
import net.mt1006.mocap.utils.Utils;
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

	public static boolean save(CommandOutput out, File recordingFile, String name, RecordingData data)
	{
		try
		{
			// double-check to make sure it won't override existing file
			if (recordingFile.exists())
			{
				out.sendFailure("recording.save.already_exists");
				return false;
			}

			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(recordingFile));
			data.save(stream);
			stream.close();
		}
		catch (IOException e)
		{
			out.sendException(e, "recording.save.error");
			return false;
		}

		CommandSuggestions.inputSet.add(name);
		return true;
	}

	public static boolean info(CommandInfo out, @Nullable RecordingFile file) //TODO: rename out
	{
		MocapRecordingFile.Info info = RecordingFile.Info.load(out, file);
		if (file == null || info == null) { return false; }

		out.sendSuccess("recordings.info.info");
		out.sendSuccess("file.info.name", file.getName());
		if (!Files.printVersionInfo(out, VERSION, info.version(), info.experimental(), info.experimentalSubversion())) { return true; }

		out.sendSuccess("recordings.info.length", String.format("%.2f", info.lengthInTicks() / 20.0), info.lengthInTicks());
		out.sendSuccess("recordings.info.size", String.format("%.2f", info.sizeInBytes() / 1024.0), info.sizeInOps());

		printPosInfo(out, info);

		if (info.assignedDimensionId() != null) { out.sendSuccess("recordings.info.dimension", info.assignedDimensionId().toString()); }
		else { out.sendSuccess("recordings.info.dimension.not_assigned"); }

		if (info.assignedPlayerName() != null) { out.sendSuccess("recordings.info.player_name_assigned.yes", info.assignedPlayerName()); }
		else { out.sendSuccess("recordings.info.player_name_assigned.no"); }

		out.sendSuccess(info.legacyEndsWithDeath() ? "recordings.info.dies.yes" : "recordings.info.dies.no");
		return true;
	}

	private static void printPosInfo(CommandInfo out, MocapRecordingFile.Info info) //TODO: rename out
	{
		ResourceLocation dimensionId = info.assignedDimensionId();
		boolean anotherDimension = (dimensionId != null && out.getLevel().dimension() != ResourceKey.create(Registries.DIMENSION, dimensionId)
				&& (Settings.DIMENSION_SOURCE.val == MocapDimensionSource.ASSIGNED_OR_CURRENT
				|| Settings.DIMENSION_SOURCE.val == MocapDimensionSource.ASSIGNED_OR_OVERWORLD));

		String xStr = String.format(Locale.US, "%.2f", info.startPos().x);
		String yStr = String.format(Locale.US, "%.2f", info.startPos().y);
		String zStr = String.format(Locale.US, "%.2f", info.startPos().z);
		String command = anotherDimension
				? String.format("/execute in %s run tp @p %s %s %s", dimensionId, xStr, yStr, zStr)
				: String.format("/tp @p %s %s %s", xStr, yStr, zStr);

		String text = String.format("%s %s %s", xStr, yStr, zStr);
		if (anotherDimension)
		{
			String dimensionIdStr = dimensionId.getNamespace().equals("minecraft") ? dimensionId.getPath() : dimensionId.toString();
			text += String.format(" (%s)", dimensionIdStr);
		}

		MutableComponent tpSuggestionComponent = Utils.getSuggestCommandComponent(command,
				Component.literal(text)).withStyle(Style.EMPTY.withUnderlined(true));
		out.sendSuccess("recordings.info.start_pos", tpSuggestionComponent);
	}

	public static @Nullable List<String> list()
	{
		if (!Files.initialized) { return null; }

		String[] fileList = Files.recordingsDirectory.list(Files::isRecordingFile);
		if (fileList == null) { return null; }

		List<String> recordings = new ArrayList<>(fileList.length);
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
