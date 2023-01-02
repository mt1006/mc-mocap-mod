package com.mt1006.mocap.mocap.playing;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mt1006.mocap.utils.FileUtils;
import com.mt1006.mocap.utils.RecordingUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerState
{
	public static final int RET_OK = 1;
	public static final int RET_NEXT_TICK = 0;
	public static final int RET_END = -1;
	public static final int RET_ERROR = -2;

	private static final byte NEXT_TICK = 0;
	private static final byte PLAYER_MOVEMENT = 1;
	private static final byte HEAD_ROTATION = 2;
	private static final byte CHANGE_POSE = 3;
	private static final byte CHANGE_ITEM = 4;
	private static final byte SET_ENTITY_FLAGS = 5;
	private static final byte SET_LIVING_ENTITY_FLAGS = 6;
	private static final byte SET_MAIN_HAND = 7;
	private static final byte SWING = 8;

	// PLAYER_MOVEMENT
	private double position[] = new double[3];
	private float rotation[] = new float[2];
	private boolean isOnGround;

	// HEAD_ROTATION
	private float yHeadRotation;

	// CHANGE_POSE
	private Pose pose;

	// CHANGE_ITEM
	private int itemIDs[] = new int[6];

	// SET_ENTITY_FLAGS
	private byte entityFlags;

	// SET_LIVING_ENTITY_FLAGS
	private byte livingEntityFlags;

	// SET_MAIN_HAND
	private byte mainHand;

	// SWING
	private boolean swinging;
	private InteractionHand swingingHand;
	private int swingingTime;

	public PlayerState(Player player)
	{
		Vec3 playerPos = player.position();
		position[0] = playerPos.x;
		position[1] = playerPos.y;
		position[2] = playerPos.z;
		rotation[0] = player.getXRot();
		rotation[1] = player.getYRot();
		isOnGround = player.isOnGround();

		yHeadRotation = player.yHeadRot;

		pose = player.getPose();

		itemIDs[0] = Item.getId(player.getMainHandItem().getItem());
		itemIDs[1] = Item.getId(player.getOffhandItem().getItem());

		int i = 2;
		for (ItemStack itemStack : player.getArmorSlots())
		{
			itemIDs[i] = Item.getId(itemStack.getItem());
			i++;
		}

		entityFlags = 0;
		if (player.isOnFire()) { entityFlags |= 0x01; }
		if (player.isShiftKeyDown()) { entityFlags |= 0x02; }
		if (player.isSprinting()) { entityFlags |= 0x08; }
		if (player.isSwimming()) { entityFlags |= 0x10; }
		if (player.isInvisible()) { entityFlags |= 0x20; }
		if (player.isCurrentlyGlowing()) { entityFlags |= 0x40; }
		if (player.isFallFlying()) { entityFlags |= 0x80; }

		livingEntityFlags = 0;
		if (player.isUsingItem()) { livingEntityFlags |= 0x01; }
		if (player.getUsedItemHand() == InteractionHand.OFF_HAND) { entityFlags |= 0x02; }
		if (player.isAutoSpinAttack()) { entityFlags |= 0x04; }

		if (player.getMainArm() == HumanoidArm.LEFT) { mainHand = 0; }
		else { mainHand = 1; }

		swinging = player.swinging;
		swingingHand = player.swingingArm;
		swingingTime = player.swingTime;
	}

	public boolean compare(PlayerState playerState)
	{
		if (playerState == null) { return true; }

		return Arrays.equals(position, playerState.position) &&
				Arrays.equals(rotation, playerState.rotation) &&
				isOnGround == playerState.isOnGround &&
				yHeadRotation == yHeadRotation &&
				pose == playerState.pose &&
				Arrays.equals(itemIDs, playerState.itemIDs) &&
				entityFlags == playerState.entityFlags &&
				livingEntityFlags == playerState.livingEntityFlags &&
				mainHand == playerState.mainHand &&
				swinging == playerState.swinging;
	}

	public void saveDifference(ArrayList<Byte> recording, @Nullable PlayerState previousState)
	{
		if (previousState == null)
		{
			RecordingUtils.addByte(recording, FileUtils.RECORDINGS_VERSION);
			RecordingUtils.addDouble(recording, position[0]);
			RecordingUtils.addDouble(recording, position[1]);
			RecordingUtils.addDouble(recording, position[2]);
			RecordingUtils.addFloat(recording, rotation[1]);
			RecordingUtils.addFloat(recording, rotation[0]);
		}

		if (previousState == null ||
				!Arrays.equals(position, previousState.position) ||
				!Arrays.equals(rotation, previousState.rotation))
		{
			RecordingUtils.addByte(recording, PLAYER_MOVEMENT);

			if (previousState == null)
			{
				RecordingUtils.addDouble(recording, 0.0);
				RecordingUtils.addDouble(recording, 0.0);
				RecordingUtils.addDouble(recording, 0.0);
			}
			else
			{
				RecordingUtils.addDouble(recording, position[0] - previousState.position[0]);
				RecordingUtils.addDouble(recording, position[1] - previousState.position[1]);
				RecordingUtils.addDouble(recording, position[2] - previousState.position[2]);
			}

			RecordingUtils.addFloat(recording, rotation[0]);
			RecordingUtils.addFloat(recording, rotation[1]);
			RecordingUtils.addBoolean(recording, isOnGround);
		}

		if (previousState == null || yHeadRotation != previousState.yHeadRotation)
		{
			RecordingUtils.addByte(recording, HEAD_ROTATION);
			RecordingUtils.addFloat(recording, yHeadRotation);
		}

		if (previousState == null || pose != previousState.pose)
		{
			RecordingUtils.addByte(recording, CHANGE_POSE);
			RecordingUtils.addPose(recording, pose);
		}

		if (previousState == null || !Arrays.equals(itemIDs, previousState.itemIDs))
		{
			RecordingUtils.addByte(recording, CHANGE_ITEM);

			for (int i = 0; i < itemIDs.length; i++)
			{
				if (previousState == null || itemIDs[i] != previousState.itemIDs[i])
				{
					RecordingUtils.addBoolean(recording, true);
					RecordingUtils.addInt(recording, itemIDs[i]);
				}
				else
				{
					RecordingUtils.addBoolean(recording, false);
				}
			}
		}

		if (previousState == null || entityFlags != previousState.entityFlags)
		{
			RecordingUtils.addByte(recording, SET_ENTITY_FLAGS);
			RecordingUtils.addByte(recording, entityFlags);
		}

		if (previousState == null || livingEntityFlags != previousState.livingEntityFlags)
		{
			RecordingUtils.addByte(recording, SET_LIVING_ENTITY_FLAGS);
			RecordingUtils.addByte(recording, livingEntityFlags);
		}

		if (previousState == null || mainHand != previousState.mainHand)
		{
			RecordingUtils.addByte(recording, SET_MAIN_HAND);
			RecordingUtils.addByte(recording, mainHand);
		}

		if (swinging && (previousState == null || !previousState.swinging || previousState.swingingTime > swingingTime))
		{
			RecordingUtils.addByte(recording, SWING);
			RecordingUtils.addBoolean(recording, (swingingHand == InteractionHand.OFF_HAND));
		}

		RecordingUtils.addByte(recording, NEXT_TICK);
	}

	public static int readState(PlayerList packetTargets, Player player, RecordingUtils.RecordingReader reader)
	{
		// References: https://wiki.vg/index.php?title=Entity_metadata&oldid=17519#Entity

		if (reader.recording.length == reader.offset) { return RET_END; }

		try
		{
			switch (reader.readByte())
			{
				case NEXT_TICK:
					return RET_NEXT_TICK;

				case PLAYER_MOVEMENT:
					double x = reader.readDouble();
					double y = reader.readDouble();
					double z = reader.readDouble();
					float rotX = reader.readFloat();
					float rotY = reader.readFloat();
					boolean onGround = reader.readBoolean();

					player.moveTo(player.getX() + x,
							player.getY() + y,
							player.getZ() + z,
							rotY, rotX);
					player.setOnGround(onGround);

					packetTargets.broadcastAll(new ClientboundTeleportEntityPacket(player));
					return RET_OK;

				case HEAD_ROTATION:
					float headRotY = reader.readFloat();

					packetTargets.broadcastAll(new ClientboundRotateHeadPacket(player,
							(byte)Math.floor(headRotY * 256.0f / 360.0f)));
					return RET_OK;

				case CHANGE_POSE:
					Pose newPose = reader.readPose();
					SynchedEntityData.DataValue<?> pose = new SynchedEntityData.DataValue<>(6, EntityDataSerializers.POSE, newPose);

					packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(player.getId(), ImmutableList.of(pose)));
					return RET_OK;

				case CHANGE_ITEM:
					List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();

					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.MAINHAND, new ItemStack(Item.byId(reader.readInt())))); }
					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.OFFHAND, new ItemStack(Item.byId(reader.readInt())))); }
					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.FEET, new ItemStack(Item.byId(reader.readInt())))); }
					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.LEGS, new ItemStack(Item.byId(reader.readInt())))); }
					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.CHEST, new ItemStack(Item.byId(reader.readInt())))); }
					if (reader.readBoolean()) { list.add(new Pair<>(EquipmentSlot.HEAD, new ItemStack(Item.byId(reader.readInt())))); }

					packetTargets.broadcastAll(new ClientboundSetEquipmentPacket(player.getId(), list));
					return RET_OK;

				case SET_ENTITY_FLAGS:
					Byte newEntityFlags = reader.readByte();
					SynchedEntityData.DataValue<?> entityFlags = new SynchedEntityData.DataValue<>(0, EntityDataSerializers.BYTE, newEntityFlags);

					packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(player.getId(), ImmutableList.of(entityFlags)));
					return RET_OK;

				case SET_LIVING_ENTITY_FLAGS:
					Byte newLivingEntityFlags = reader.readByte();
					SynchedEntityData.DataValue<?> livingEntityFlags = new SynchedEntityData.DataValue<>(8, EntityDataSerializers.BYTE, newLivingEntityFlags);

					packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(player.getId(), ImmutableList.of(livingEntityFlags)));
					return RET_OK;

				case SET_MAIN_HAND:
					Byte newMainHand = reader.readByte();
					SynchedEntityData.DataValue<?> mainHand = new SynchedEntityData.DataValue<>(18, EntityDataSerializers.BYTE, newMainHand);

					packetTargets.broadcastAll(new ClientboundSetEntityDataPacket(player.getId(), ImmutableList.of(mainHand)));
					return RET_OK;

				case SWING:
					int eventID;

					if (reader.readBoolean()) { eventID = 3; }
					else { eventID = 0; }

					packetTargets.broadcastAll(new ClientboundAnimatePacket(player, eventID));
					return RET_OK;
			}
		}
		catch (Exception exception)
		{
			return RET_ERROR;
		}

		return RET_ERROR;
	}

	public static boolean readHeader(Player player, RecordingUtils.RecordingReader reader, Vec3 posOffset)
	{
		try
		{
			byte version = reader.readByte();
			if (version != FileUtils.RECORDINGS_VERSION)
			{
				return false;
			}

			player.moveTo(reader.readDouble() + posOffset.x,
					reader.readDouble() + posOffset.y,
					reader.readDouble() + posOffset.z,
					reader.readFloat(), reader.readFloat());
		}
		catch (Exception exception)
		{
			return false;
		}

		return true;
	}
}
