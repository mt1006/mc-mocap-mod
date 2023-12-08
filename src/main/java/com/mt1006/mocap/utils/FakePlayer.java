package com.mt1006.mocap.utils;

import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.INetHandler;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.SocketAddress;
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
		private static final NetworkManager DUMMY_CONNECTION = new DummyConnection(PacketDirection.CLIENTBOUND);

		public FakePlayerNetHandler(MinecraftServer server, ServerPlayerEntity player)
		{
			super(server, DUMMY_CONNECTION, player);
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

	@ParametersAreNonnullByDefault
	private static class DummyConnection extends NetworkManager
	{
		private static final Channel DUMMY_CHANNEL = new DummyChannel();

		public DummyConnection(PacketDirection packetFlow)
		{
			super(packetFlow);
		}
		@Override public void setListener(INetHandler packetListener) {}
		@Override public @NotNull Channel channel() { return DUMMY_CHANNEL; }
	}

	// based on FailedChannel code
	private static class DummyChannel extends AbstractChannel
	{
		private static final ChannelMetadata METADATA = new ChannelMetadata(false);
		private final ChannelConfig config = new DefaultChannelConfig(this);

		DummyChannel() { super(null); }

		@Override protected AbstractUnsafe newUnsafe() { return new FailedChannelUnsafe(); }
		@Override protected boolean isCompatible(EventLoop loop) { return false; }
		@Override protected SocketAddress localAddress0() { return null; }
		@Override protected SocketAddress remoteAddress0() { return null; }
		@Override protected void doBind(SocketAddress localAddress) {}
		@Override protected void doDisconnect() {}
		@Override protected void doClose() {}
		@Override protected void doBeginRead() {}
		@Override protected void doWrite(ChannelOutboundBuffer in) {}
		@Override public ChannelConfig config() { return config; }
		@Override public boolean isOpen() { return false; }
		@Override public boolean isActive() { return false; }
		@Override public ChannelMetadata metadata() { return METADATA; }

		private final class FailedChannelUnsafe extends AbstractUnsafe
		{
			@Override public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {}
		}
	}
}
