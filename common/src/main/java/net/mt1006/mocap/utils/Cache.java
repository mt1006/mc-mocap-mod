package net.mt1006.mocap.utils;

import net.mt1006.mocap.MocapMod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K, V>
{
	private final Map<K, V> map = new ConcurrentHashMap<>();
	private final String description;
	private final int maxSize;

	public Cache(String description, int maxSize)
	{
		this.description = description;
		this.maxSize = maxSize;
	}

	public @Nullable V get(K key)
	{
		return map.get(key);
	}

	public void put(K key, V val)
	{
		if (map.size() >= maxSize)
		{
			MocapMod.LOGGER.warn("Cache \"{}\" reached size limit ({}) - clearing!", description, maxSize);
			clear();
		}
		map.put(key, val);
	}

	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	public void clear()
	{
		map.clear();
	}
}
