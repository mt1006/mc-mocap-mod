package com.mt1006.mocap.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// FakePlayer class from Forge
public class FakePlayer extends ServerPlayer
{
	public FakePlayer(ServerLevel level, GameProfile name)
	{
		super(level.getServer(), level, name, null);
		this.connection = new FakePlayerNetHandler(level.getServer(), this);
	}

	@Override public void checkInsideBlocks() { super.checkInsideBlocks(); }
	@Override public Entity changeDimension(@NotNull ServerLevel p_20118_) { return null; }

	@Override public void displayClientMessage(@NotNull Component chatComponent, boolean actionBar) { }
	@Override public void awardStat(@NotNull Stat stat, int amount) { }
	@Override public boolean isInvulnerableTo(@NotNull DamageSource source) { return true; }
	@Override public boolean canHarmPlayer(@NotNull Player player) { return false; }
	@Override public void die(@NotNull DamageSource source) { }
	@Override public void tick() { }
	@Override public void updateOptions(@NotNull ServerboundClientInformationPacket packet) { }

	private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl
	{
		private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

		public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
			super(server, DUMMY_CONNECTION, player);
		}

		@Override public void tick() { }
		@Override public void resetPosition() { }
		@Override public void disconnect(Component message) { }
		@Override public void handlePlayerInput(ServerboundPlayerInputPacket packet) { }
		@Override public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) { }
		@Override public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) { }
		@Override public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) { }
		@Override public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) { }
		@Override public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) { }
		@Override public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) { }
		@Override public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) { }
		@Override public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) { }
		@Override public void handlePickItem(ServerboundPickItemPacket packet) { }
		@Override public void handleRenameItem(ServerboundRenameItemPacket packet) { }
		@Override public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) { }
		@Override public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) { }
		@Override public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) { }
		@Override public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) { }
		@Override public void handleSelectTrade(ServerboundSelectTradePacket packet) { }
		@Override public void handleEditBook(ServerboundEditBookPacket packet) { }
		@Override public void handleEntityTagQuery(ServerboundEntityTagQuery packet) { }
		@Override public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) { }
		@Override public void handleMovePlayer(ServerboundMovePlayerPacket packet) { }
		@Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
		@Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<ClientboundPlayerPositionPacket.RelativeArgument> flags) { }
		@Override public void handlePlayerAction(ServerboundPlayerActionPacket packet) { }
		@Override public void handleUseItemOn(ServerboundUseItemOnPacket packet) { }
		@Override public void handleUseItem(ServerboundUseItemPacket packet) { }
		@Override public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) { }
		@Override public void handleResourcePackResponse(ServerboundResourcePackPacket packet) { }
		@Override public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) { }
		@Override public void onDisconnect(Component message) { }
		@Override public void send(Packet<?> packet) { }
		@Override public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) { }
		@Override public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) { }
		@Override public void handleChat(ServerboundChatPacket packet) { }
		@Override public void handleAnimate(ServerboundSwingPacket packet) { }
		@Override public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) { }
		@Override public void handleInteract(ServerboundInteractPacket packet) { }
		@Override public void handleClientCommand(ServerboundClientCommandPacket packet) { }
		@Override public void handleContainerClose(ServerboundContainerClosePacket packet) { }
		@Override public void handleContainerClick(ServerboundContainerClickPacket packet) { }
		@Override public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) { }
		@Override public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) { }
		@Override public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) { }
		@Override public void handleSignUpdate(ServerboundSignUpdatePacket packet) { }
		@Override public void handleKeepAlive(ServerboundKeepAlivePacket packet) { }
		@Override public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) { }
		@Override public void handleClientInformation(ServerboundClientInformationPacket packet) { }
		@Override public void handleCustomPayload(ServerboundCustomPayloadPacket packet) { }
		@Override public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) { }
		@Override public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) { }
		@Override public void dismount(double x, double y, double z, float yaw, float pitch) { }
		@Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeSet, boolean dismountVehicle) { }
		@Override public void ackBlockChangesUpTo(int sequence) { }
		@Override public void handleChatCommand(ServerboundChatCommandPacket packet) { }
		@Override public void handleChatAck(ServerboundChatAckPacket packet) { }
		@Override public void addPendingMessage(PlayerChatMessage message) { }
	}
}
