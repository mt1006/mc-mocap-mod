package com.mt1006.mocap.utils;

import com.mojang.authlib.GameProfile;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.Stat;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;
import java.util.UUID;

// FakePlayer class from Forge
@ParametersAreNonnullByDefault
public class FakePlayer extends ServerPlayerEntity
{
	public FakePlayer(ServerWorld level, GameProfile name)
	{
		super(level.getServer(), level, name, new PlayerInteractionManager(level));
		this.connection = new FakePlayerNetHandler(level.getServer(), this);
	}

	@Override public Entity changeDimension(ServerWorld level) { return null; }

	@Override public void displayClientMessage(ITextComponent chatComponent, boolean actionBar) { }
	@Override public void sendMessage(ITextComponent component, UUID senderUUID) { }
	@Override public void awardStat(Stat par1StatBase, int par2) { }
	@Override public void die(DamageSource source) { }
	@Override public void tick() { }
	@Override public void updateOptions(CClientSettingsPacket pkt) { }

	@ParametersAreNonnullByDefault
	private static class FakePlayerNetHandler extends ServerPlayNetHandler
	{
		private static final NetworkManager DUMMY_NETWORK_MANAGER = new NetworkManager(PacketDirection.CLIENTBOUND);

		public FakePlayerNetHandler(MinecraftServer server, ServerPlayerEntity player) {
			super(server, DUMMY_NETWORK_MANAGER, player);
		}

		@Override public void tick() { }
		@Override public void resetPosition() { }
		@Override public void disconnect(ITextComponent message) { }
		@Override public void handlePlayerInput(CInputPacket packet) { }
		@Override public void handleMoveVehicle(CMoveVehiclePacket packet) { }
		@Override public void handleAcceptTeleportPacket(CConfirmTeleportPacket packet) { }
		@Override public void handleRecipeBookSeenRecipePacket(CMarkRecipeSeenPacket packet) { }
		@Override public void handleRecipeBookChangeSettingsPacket(CUpdateRecipeBookStatusPacket packet) { }
		@Override public void handleSeenAdvancements(CSeenAdvancementsPacket packet) { }
		@Override public void handleCustomCommandSuggestions(CTabCompletePacket packet) { }
		@Override public void handleSetCommandBlock(CUpdateCommandBlockPacket packet) { }
		@Override public void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket packet) { }
		@Override public void handlePickItem(CPickItemPacket packet) { }
		@Override public void handleRenameItem(CRenameItemPacket packet) { }
		@Override public void handleSetBeaconPacket(CUpdateBeaconPacket packet) { }
		@Override public void handleSetStructureBlock(CUpdateStructureBlockPacket packet) { }
		@Override public void handleSetJigsawBlock(CUpdateJigsawBlockPacket packet) { }
		@Override public void handleJigsawGenerate(CJigsawBlockGeneratePacket packet) { }
		@Override public void handleSelectTrade(CSelectTradePacket packet) { }
		@Override public void handleEditBook(CEditBookPacket packet) { }
		@Override public void handleEntityTagQuery(CQueryEntityNBTPacket packet) { }
		@Override public void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket packet) { }
		@Override public void handleMovePlayer(CPlayerPacket packet) { }
		@Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
		@Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<SPlayerPositionLookPacket.Flags> flags) { }
		@Override public void handlePlayerAction(CPlayerDiggingPacket packet) { }
		@Override public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket packet) { }
		@Override public void handleUseItem(CPlayerTryUseItemPacket packet) { }
		@Override public void handleTeleportToEntityPacket(CSpectatePacket packet) { }
		@Override public void handleResourcePackResponse(CResourcePackStatusPacket packet) { }
		@Override public void handlePaddleBoat(CSteerBoatPacket packet) { }
		@Override public void onDisconnect(ITextComponent message) { }
		@Override public void send(IPacket<?> packet) { }
		@Override public void send(IPacket<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> p_211148_2_) { }
		@Override public void handleSetCarriedItem(CHeldItemChangePacket packet) { }
		@Override public void handleChat(CChatMessagePacket packet) { }
		@Override public void handleAnimate(CAnimateHandPacket packet) { }
		@Override public void handlePlayerCommand(CEntityActionPacket packet) { }
		@Override public void handleInteract(CUseEntityPacket packet) { }
		@Override public void handleClientCommand(CClientStatusPacket packet) { }
		@Override public void handleContainerClose(CCloseWindowPacket packet) { }
		@Override public void handleContainerClick(CClickWindowPacket packet) { }
		@Override public void handlePlaceRecipe(CPlaceRecipePacket packet) { }
		@Override public void handleContainerButtonClick(CEnchantItemPacket packet) { }
		@Override public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket packet) { }
		@Override public void handleContainerAck(CConfirmTransactionPacket packet) { }
		@Override public void handleSignUpdate(CUpdateSignPacket packet) { }
		@Override public void handleKeepAlive(CKeepAlivePacket packet) { }
		@Override public void handlePlayerAbilities(CPlayerAbilitiesPacket packet) { }
		@Override public void handleClientInformation(CClientSettingsPacket packet) { }
		@Override public void handleCustomPayload(CCustomPayloadPacket packet) { }
		@Override public void handleChangeDifficulty(CSetDifficultyPacket packet) { }
		@Override public void handleLockDifficulty(CLockDifficultyPacket packet) { }
	}
}
