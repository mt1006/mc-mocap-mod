package com.mt1006.mocap.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Fields
{
	public static Field gameProfileProperties = null;

	public static void init()
	{
		try
		{
			gameProfileProperties = findField(GameProfile.class, PropertyMap.class);
			gameProfileProperties.setAccessible(true);
		}
		catch (Exception ignore) {}
	}

	private static Field findField(Class<?> declaringClass, Class<?> fieldType)
	{
		Field[] fields = declaringClass.getDeclaredFields();

		for (Field field : fields)
		{
			if (Modifier.isStatic(field.getModifiers())) { continue; }
			if (fieldType.isAssignableFrom(field.getType())) { return field; }
		}

		return null;
	}
}
