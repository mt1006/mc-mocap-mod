package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.command.CommandInfo;
import com.mt1006.mocap.command.CommandOutput;
import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.playing.RecordingData;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordingFiles
{
	public static final byte RECORDING_VERSION = 3;

	public static boolean save(CommandInfo commandInfo, String name, RecordingFiles.Writer writer)
	{
		File recordingFile = Files.getRecordingFile(commandInfo, name);
		if (recordingFile == null) { return false; }

		try
		{
			if (recordingFile.exists())
			{
				commandInfo.sendFailure("mocap.recording.save.file_already_exist");
				commandInfo.sendFailure("mocap.recording.save.file_already_exist.tip");
				return false;
			}

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(recordingFile));
			for (Byte val : writer.getByteList())
			{
				bufferedOutputStream.write(new byte[] {val});
			}
			bufferedOutputStream.close();
			writer.clear();
		}
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.recording.save.exception");
			return false;
		}

		InputArgument.addServerInput(name);
		commandInfo.sendSuccess("mocap.recording.save.success");
		return true;
	}

	public static boolean copy(CommandInfo commandInfo, String srcName, String destName)
	{
		File srcFile = Files.getRecordingFile(commandInfo, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getRecordingFile(commandInfo, destName);
		if (destFile == null) { return false; }

		try { FileUtils.copyFile(srcFile, destFile); }
		catch (IOException exception)
		{
			commandInfo.sendException(exception, "mocap.recordings.copy.failed");
			return false;
		}

		InputArgument.addServerInput(destName);
		commandInfo.sendSuccess("mocap.recordings.copy.success");
		return true;
	}

	public static boolean rename(CommandInfo commandInfo, String oldName, String newName)
	{
		File oldFile = Files.getRecordingFile(commandInfo, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getRecordingFile(commandInfo, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			commandInfo.sendFailure("mocap.recordings.rename.failed");
			return false;
		}

		InputArgument.removeServerInput(oldName);
		InputArgument.addServerInput(newName);
		commandInfo.sendSuccess("mocap.recordings.rename.success");
		return true;
	}

	public static boolean remove(CommandInfo commandInfo, String name)
	{
		File recordingFile = Files.getRecordingFile(commandInfo, name);
		if (recordingFile == null) { return false; }

		if (!recordingFile.delete())
		{
			commandInfo.sendFailure("mocap.recordings.remove.failed");
			return false;
		}

		InputArgument.removeServerInput(name);
		commandInfo.sendSuccess("mocap.recordings.remove.success");
		return true;
	}

	public static boolean info(CommandInfo commandInfo, String name)
	{
		RecordingData recordingData = new RecordingData();

		if (!recordingData.load(commandInfo, name) && recordingData.version <= RECORDING_VERSION)
		{
			commandInfo.sendFailure("mocap.recordings.info.failed");
			return false;
		}

		commandInfo.sendSuccess("mocap.recordings.info.info");
		commandInfo.sendSuccess("mocap.file.info.name", name);

		if (recordingData.version <= RECORDING_VERSION)
		{
			if (recordingData.version == RECORDING_VERSION)
			{
				commandInfo.sendSuccess("mocap.file.info.version.current", recordingData.version);
			}
			else if (recordingData.version > 0)
			{
				commandInfo.sendSuccess("mocap.file.info.version.old", recordingData.version);
			}
			else
			{
				commandInfo.sendSuccess("mocap.file.info.version.unknown", recordingData.version);
			}

			commandInfo.sendSuccess("mocap.recordings.info.length",
					String.format("%.2f", recordingData.tickCount / 20.0), recordingData.tickCount);

			commandInfo.sendSuccess("mocap.recordings.info.size",
					String.format("%.2f", recordingData.fileSize / 1024.0), recordingData.actions.size() - recordingData.tickCount);

			commandInfo.sendSuccess("mocap.recordings.info.start_pos",
					String.format("%.2f", recordingData.startPos[0]),
					String.format("%.2f", recordingData.startPos[1]),
					String.format("%.2f", recordingData.startPos[2]));

			//TODO: add info about rotation and death at the end
		}
		else
		{
			commandInfo.sendSuccess("mocap.file.info.version.not_supported", recordingData.version);
		}
		return true;
	}

	public static @Nullable ArrayList<String> list(MinecraftServer server, CommandOutput commandOutput)
	{
		if (!Files.initDirectories(server, commandOutput)) { return null; }
		ArrayList<String> recordings = new ArrayList<>();

		String[] filesList = Files.recordingsDirectory.list();
		if (filesList == null) { return null; }

		for (String filename : filesList)
		{
			if (Files.isRecordingFile(filename))
			{
				recordings.add(filename.substring(0, filename.lastIndexOf('.')));
			}
		}
		return recordings;
	}

	public static class Writer
	{
		private final List<Byte> recording = new ArrayList<>();

		public void addByte(byte val)
		{
			recording.add(val);
		}

		public void addInt(int val)
		{
			recording.add((byte)(val >> 24));
			recording.add((byte)(val >> 16));
			recording.add((byte)(val >> 8));
			recording.add((byte)val);
		}

		public void addFloat(float val)
		{
			for (byte b : floatToByteArray(val))
			{
				recording.add(b);
			}
		}

		public void addDouble(double val)
		{
			for (byte b : doubleToByteArray(val))
			{
				recording.add(b);
			}
		}

		public void addBoolean(boolean val)
		{
			recording.add(val ? (byte)1 : (byte)0);
		}

		public void addString(String val)
		{
			byte[] bytes = val.getBytes(StandardCharsets.UTF_8);
			addInt(bytes.length);
			for (byte b : bytes)
			{
				recording.add(b);
			}
		}

		public void addWriter(RecordingFiles.Writer writer)
		{
			recording.addAll(writer.recording);
		}

		public int addMutableBoolean(boolean val)
		{
			recording.add(val ? (byte)1 : (byte)0);
			return recording.size() - 1;
		}

		public void modifyBoolean(int pos, boolean newVal)
		{
			if (pos < 0) { return; }
			recording.set(pos, newVal ? (byte)1 : (byte)0);
		}

		public void clear()
		{
			recording.clear();
		}

		public List<Byte> getByteList()
		{
			return recording;
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

	public interface Reader
	{
		byte readByte();

		int readInt();

		float readFloat();

		double readDouble();

		boolean readBoolean();

		String readString();
	}

	public static class FileReader implements Reader
	{
		private final byte[] recording;
		private int offset = 0;

		public FileReader(byte[] recording)
		{
			this.recording = recording;
		}

		@Override public byte readByte()
		{
			return recording[offset++];
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
			int length = readInt();
			String retVal = new String(recording, offset, length, StandardCharsets.UTF_8);
			offset += length;
			return retVal;
		}

		public boolean canRead()
		{
			return recording.length != offset;
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

	public static class DummyReader implements Reader
	{
		public static final RecordingFiles.Reader READER = new DummyReader();

		@Override public byte readByte()
		{
			return 0;
		}

		@Override public int readInt()
		{
			return 0;
		}

		@Override public float readFloat()
		{
			return 0.0f;
		}

		@Override public double readDouble()
		{
			return 0.0;
		}

		@Override public boolean readBoolean()
		{
			return false;
		}

		@Override public String readString()
		{
			return "";
		}
	}
}
