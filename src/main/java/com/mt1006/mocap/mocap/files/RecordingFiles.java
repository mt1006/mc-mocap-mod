package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.command.InputArgument;
import com.mt1006.mocap.mocap.playing.RecordingData;
import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
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

	public static boolean save(CommandSourceStack commandSource, String name, RecordingFiles.Writer writer)
	{
		File recordingFile = Files.getRecordingFile(commandSource, name);
		if (recordingFile == null) { return false; }

		try
		{
			if (recordingFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.recording.save.file_already_exist");
				Utils.sendFailure(commandSource, "mocap.recording.save.file_already_exist.tip");
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
			Utils.sendException(exception, commandSource, "mocap.recording.save.exception");
			return false;
		}

		InputArgument.addServerInput(name);
		Utils.sendSuccess(commandSource, "mocap.recording.save.success");
		return true;
	}

	public static boolean copy(CommandSourceStack commandSource, String srcName, String destName)
	{
		File srcFile = Files.getRecordingFile(commandSource, srcName);
		if (srcFile == null) { return false; }

		File destFile = Files.getRecordingFile(commandSource, destName);
		if (destFile == null) { return false; }

		try { FileUtils.copyFile(srcFile, destFile); }
		catch (IOException exception)
		{
			Utils.sendException(exception, commandSource, "mocap.recordings.copy.failed");
			return false;
		}

		InputArgument.addServerInput(destName);
		Utils.sendSuccess(commandSource, "mocap.recordings.copy.success");
		return true;
	}

	public static boolean rename(CommandSourceStack commandSource, String oldName, String newName)
	{
		File oldFile = Files.getRecordingFile(commandSource, oldName);
		if (oldFile == null) { return false; }

		File newFile = Files.getRecordingFile(commandSource, newName);
		if (newFile == null) { return false; }

		if (!oldFile.renameTo(newFile))
		{
			Utils.sendFailure(commandSource, "mocap.recordings.rename.failed");
			return false;
		}

		InputArgument.removeServerInput(oldName);
		InputArgument.addServerInput(newName);
		Utils.sendSuccess(commandSource, "mocap.recordings.rename.success");
		return true;
	}

	public static boolean remove(CommandSourceStack commandSource, String name)
	{
		File recordingFile = Files.getRecordingFile(commandSource, name);
		if (recordingFile == null) { return false; }

		if (!recordingFile.delete())
		{
			Utils.sendFailure(commandSource, "mocap.recordings.remove.failed");
			return false;
		}

		InputArgument.removeServerInput(name);
		Utils.sendSuccess(commandSource, "mocap.recordings.remove.success");
		return true;
	}

	public static boolean info(CommandSourceStack commandSource, String name)
	{
		RecordingData recordingData = new RecordingData();

		if (!recordingData.load(commandSource, name) && recordingData.version <= RECORDING_VERSION)
		{
			Utils.sendFailure(commandSource, "mocap.recordings.info.failed");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.recordings.info.info");
		Utils.sendSuccess(commandSource, "mocap.file.info.name", name);

		if (recordingData.version <= RECORDING_VERSION)
		{
			if (recordingData.version == RECORDING_VERSION)
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.current", recordingData.version);
			}
			else if (recordingData.version > 0)
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.old", recordingData.version);
			}
			else
			{
				Utils.sendSuccess(commandSource, "mocap.file.info.version.unknown", recordingData.version);
			}

			Utils.sendSuccess(commandSource, "mocap.recordings.info.length",
					String.format("%.2f", recordingData.tickCount / 20.0), recordingData.tickCount);

			Utils.sendSuccess(commandSource, "mocap.recordings.info.size",
					String.format("%.2f", recordingData.fileSize / 1024.0), recordingData.actions.size() - recordingData.tickCount);

			Utils.sendSuccess(commandSource, "mocap.recordings.info.start_pos",
					String.format("%.2f", recordingData.startPos[0]),
					String.format("%.2f", recordingData.startPos[1]),
					String.format("%.2f", recordingData.startPos[2]));

			//TODO: add info about rotation and death at the end (1.4)
		}
		else
		{
			Utils.sendSuccess(commandSource, "mocap.file.info.version.not_supported", recordingData.version);
		}
		return true;
	}

	public static @Nullable ArrayList<String> list(MinecraftServer server, @Nullable CommandSourceStack commandSource)
	{
		if (!Files.initDirectories(server, commandSource)) { return null; }
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
			for (byte b : Utils.floatToByteArray(val))
			{
				recording.add(b);
			}
		}

		public void addDouble(double val)
		{
			for (byte b : Utils.doubleToByteArray(val))
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
			addInt(val.length());
			for (byte b : val.getBytes(StandardCharsets.UTF_8))
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
			float retVal = Utils.byteArrayToFloat(Arrays.copyOfRange(recording, offset, offset + 4));
			offset += 4;
			return retVal;
		}

		@Override public double readDouble()
		{
			double retVal = Utils.byteArrayToDouble(Arrays.copyOfRange(recording, offset, offset + 8));
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
