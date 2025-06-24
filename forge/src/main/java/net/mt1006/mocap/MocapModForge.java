package net.mt1006.mocap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
import net.mt1006.mocap.forge.PacketHandler;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;

@Mod(MocapMod.MOD_ID)
public class MocapModForge implements MocapModLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();
	private final FMLModContainer modContainer;

	public MocapModForge(FMLJavaModLoadingContext ctx)
	{
		modContainer = ctx.getContainer();
		MocapMod.init(isDedicatedServer, this);

		PacketHandler.register();
		MocapMod.postInit();
	}

	@Override public String getLoaderName()
	{
		return "Forge";
	}

	@Override public String getModVersion()
	{
		return modContainer.getModInfo().getVersion().toString();
	}

	@Override public void sendPacketToClient(ServerPlayer player, MocapPacketS2C packet)
	{
		PacketHandler.INSTANCE.send(packet, PacketDistributor.PLAYER.with(player));
	}

	@Override public void sendPacketToServer(MocapPacketC2S packet)
	{
		PacketHandler.INSTANCE.send(packet, PacketDistributor.SERVER.with(null));
	}
}
