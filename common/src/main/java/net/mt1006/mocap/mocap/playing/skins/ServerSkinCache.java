package net.mt1006.mocap.mocap.playing.skins;

import net.mt1006.mocap.MocapMod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSkinCache
{
	private static final long MAX_CUMULATIVE_ARRAY_SIZE = 128L * (1 << 20);
	private static final int MAX_ELEMENT_COUNT = 4096;
	private final Map<String, byte[]> map = new ConcurrentHashMap<>();
	private long sizeSum = 0L;

	public byte @Nullable[] get(String key)
	{
		return map.get(key);
	}

	public void put(String key, byte[] val)
	{
		if (sizeSum + val.length > MAX_CUMULATIVE_ARRAY_SIZE || map.size() + 1 > MAX_ELEMENT_COUNT)
		{
			MocapMod.LOGGER.warn("Server skin cache reached its limits (s={}/{} B) (n={}/{}) - clearing!",
					sizeSum + val.length, MAX_CUMULATIVE_ARRAY_SIZE, map.size() + 1, MAX_ELEMENT_COUNT);
			clear();
		}
		map.put(key, val);
		sizeSum += val.length;
	}

	public void clear()
	{
		map.clear();
		sizeSum = 0L;
	}
}
