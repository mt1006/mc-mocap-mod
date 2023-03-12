package com.mt1006.mocap;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class IsDedicatedServer
{
	public static boolean isDedicatedServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
}
