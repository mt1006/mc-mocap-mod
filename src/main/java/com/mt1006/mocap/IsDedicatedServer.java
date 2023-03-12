package com.mt1006.mocap;

import net.minecraftforge.fml.loading.FMLEnvironment;

public class IsDedicatedServer
{
	public static boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();
}