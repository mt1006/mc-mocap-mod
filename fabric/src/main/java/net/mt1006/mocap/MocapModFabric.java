package net.mt1006.mocap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.level.ServerPlayer;
import net.mt1006.mocap.command.commands.MocapCommand;
import net.mt1006.mocap.fabric.PacketHandler;
import net.mt1006.mocap.fabric.events.*;
import net.mt1006.mocap.network.MocapPacketC2S;
import net.mt1006.mocap.network.MocapPacketS2C;

import java.util.Optional;

public class MocapModFabric implements ModInitializer, MocapModLoaderInterface
{
	private static final FabricLoader FABRIC_LOADER = FabricLoader.getInstance();
	public static final boolean isDedicatedServer = FABRIC_LOADER.getEnvironmentType() == EnvType.SERVER;

	@Override public void onInitialize()
	{
		MocapMod.init(isDedicatedServer, this);

		PlayerBlockBreakEvents.BEFORE.register(BlockInteractionFabricEvent::onBlockBreak);
		UseBlockCallback.EVENT.register(BlockInteractionFabricEvent::onRightClickBlock);

		ServerLivingEntityEvents.AFTER_DAMAGE.register(EntityFabricEvent::onEntityHurt);
		ServerPlayerEvents.AFTER_RESPAWN.register(EntityFabricEvent::onPlayerRespawn);
		ServerTickEvents.END_SERVER_TICK.register(ServerTickFabricEvent::onEndTick);
		ServerLifecycleEvents.SERVER_STARTED.register(LifecycleFabricEvent::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPING.register(LifecycleFabricEvent::onServerStop);
		ServerPlayConnectionEvents.JOIN.register(PlayerConnectionFabricEvent::onPlayerJoin);
		ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionFabricEvent::onPlayerLeave);
		ServerMessageEvents.CHAT_MESSAGE.register(ChatFabricEvent::onChatMessage);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MocapCommand.register(dispatcher, registryAccess));
		PacketHandler.register();
		MocapMod.postInit();
	}

	@Override public String getLoaderName()
	{
		return "Fabric";
	}

	@Override public String getModVersion()
	{
		Optional<ModContainer> modContainer = FABRIC_LOADER.getModContainer(MocapMod.MOD_ID);
		return modContainer.isPresent() ? modContainer.get().getMetadata().getVersion().getFriendlyString() : "[unknown]";
	}

	@Override public void sendPacketToClient(ServerPlayer player, MocapPacketS2C packet)
	{
		ServerPlayNetworking.send(player, packet);
	}

	@Override public void sendPacketToServer(MocapPacketC2S packet)
	{
		ClientPlayNetworking.send(packet);
	}
}
