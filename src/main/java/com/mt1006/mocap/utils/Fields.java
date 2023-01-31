package com.mt1006.mocap.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Fields
{
	public static Field gameProfileProperties = null;

	public static void init()
	{
		try { gameProfileProperties = getField(GameProfile.class, PropertyMap.class); }
		catch (Exception ignore) {}
	}

	private static @Nullable Field getField(Class<?> declaringClass, Class<?> fieldType)
	{
		Field[] fields = declaringClass.getDeclaredFields();

		for (Field field : fields)
		{
			if (Modifier.isStatic(field.getModifiers())) { continue; }
			if (fieldType.isAssignableFrom(field.getType()))
			{
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}
}
