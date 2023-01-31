package com.mt1006.mocap.mocap.files;

import com.mt1006.mocap.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordingFile
{
	public static final byte RECORDING_VERSION = 2;

	public static boolean save(CommandSourceStack commandSource, String name, RecordingFile.Writer writer)
	{
		File recordingFile = Files.getRecordingFile(commandSource, name);
		if (recordingFile == null) { return false; }

		try
		{
			if (recordingFile.exists())
			{
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.file_already_exist");
				Utils.sendFailure(commandSource, "mocap.commands.recording.save.file_already_exist.tip");
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
			Utils.sendFailure(commandSource, "mocap.commands.recording.save.exception");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.recording.save.success");
		return true;
	}

	public static boolean remove(CommandSourceStack commandSource, String name)
	{
		File recordingFile = Files.getRecordingFile(commandSource, name);
		if (recordingFile == null) { return false; }

		if (!recordingFile.delete())
		{
			Utils.sendFailure(commandSource, "mocap.commands.recording.remove.failed");
			return false;
		}

		Utils.sendSuccess(commandSource, "mocap.commands.recording.remove.success");
		return true;
	}

	public static @Nullable ArrayList<String> list(CommandSourceStack commandSource)
	{
		if (!Files.initDirectories(commandSource)) { return null; }
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
			if (val) { recording.add((byte)1); }
			else { recording.add((byte)0); }
		}

		public void addString(String val)
		{
			addInt(val.length());
			for (byte b : val.getBytes(StandardCharsets.UTF_8))
			{
				recording.add(b);
			}
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

	public static class Reader
	{
		private final byte[] recording;
		private int offset = 0;

		public Reader(byte[] recording)
		{
			this.recording = recording;
		}

		public boolean canRead()
		{
			return recording.length != offset;
		}

		public byte readByte()
		{
			return recording[offset++];
		}

		public int readInt()
		{
			int retVal = ((recording[offset] & 0xFF) << 24) | ((recording[offset + 1] & 0xFF) << 16) |
					((recording[offset + 2] & 0xFF) << 8) | (recording[offset + 3] & 0xFF);
			offset += 4;
			return retVal;
		}
		public float readFloat()
		{
			float retVal = Utils.byteArrayToFloat(Arrays.copyOfRange(recording, offset, offset + 4));
			offset += 4;
			return retVal;
		}

		public double readDouble()
		{
			double retVal = Utils.byteArrayToDouble(Arrays.copyOfRange(recording, offset, offset + 8));
			offset += 8;
			return retVal;
		}

		public boolean readBoolean()
		{
			return recording[offset++] == 1;
		}

		public String readString()
		{
			int length = readInt();
			String retVal = new String(recording, offset, length, StandardCharsets.UTF_8);
			offset += length;
			return retVal;
		}
	}
}
