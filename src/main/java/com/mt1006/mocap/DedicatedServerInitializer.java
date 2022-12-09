package com.mt1006.mocap;

import net.fabricmc.api.DedicatedServerModInitializer;

public class DedicatedServerInitializer implements DedicatedServerModInitializer
{
	public static boolean isDedicatedServer = false;

	@Override
	public void onInitializeServer()
	{
		isDedicatedServer = true;
	}
}
