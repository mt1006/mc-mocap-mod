package net.mt1006.mocap;

import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod(MocapMod.MOD_ID)
public class MocapModNeoForge implements MocapModLoaderInterface
{
	public static final boolean isDedicatedServer = FMLEnvironment.dist.isDedicatedServer();
	private final @Nullable ModContainer modContainer;

	public MocapModNeoForge(IEventBus eventBus)
	{
		ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
		this.modContainer = modContainer.getModId().equals("minecraft") ? null : modContainer;
		MocapMod.init(isDedicatedServer, this);
		MocapMod.postInit();
	}

	@Override public String getLoaderName()
	{
		return "NeoForge";
	}

	@Override public String getModVersion()
	{
		return modContainer != null ? modContainer.getModInfo().getVersion().toString() : "[unknown]";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override public void sendPacketToClient(ServerPlayer player, MocapPacketS2C packet)
	{
		PacketDistributor.sendToPlayer(player, packet);
	}

	@Override public void sendPacketToServer(MocapPacketC2S packet)
	{
		ClientPacketDistributor.sendToServer(packet);
	}
}
