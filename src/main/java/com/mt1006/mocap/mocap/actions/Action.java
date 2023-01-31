package com.mt1006.mocap.mocap.actions;

import com.mt1006.mocap.mocap.files.RecordingFile;
import com.mt1006.mocap.utils.FakePlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.vector.Vector3i;

public interface Action
{
	int RET_OK = 1;
	int RET_NEXT_TICK = 0;
	int RET_END = -1;
	int RET_ERROR = -2;

	byte NEXT_TICK = 0;
	byte PLAYER_MOVEMENT = 1;
	byte HEAD_ROTATION = 2;
	byte CHANGE_POSE = 3;
	byte CHANGE_ITEM = 4;
	byte SET_ENTITY_FLAGS = 5;
	byte SET_LIVING_ENTITY_FLAGS = 6;
	byte SET_MAIN_HAND = 7;
	byte SWING = 8;
	byte BREAK_BLOCK = 9;
	byte PLACE_BLOCK = 10;
	byte RIGHT_CLICK_BLOCK = 11;

	int execute(PlayerList packetTargets, FakePlayer fakePlayer, Vector3i blockOffset);

	static Action readAction(RecordingFile.Reader reader)
	{
		switch (reader.readByte())
		{
			case NEXT_TICK: return new NextTick();
			case PLAYER_MOVEMENT: return new PlayerMovement(reader);
			case HEAD_ROTATION: return new HeadRotation(reader);
			case CHANGE_POSE: return new ChangePose(reader);
			case CHANGE_ITEM: return new ChangeItem(reader);
			case SET_ENTITY_FLAGS: return new SetEntityFlags(reader);
			case SET_LIVING_ENTITY_FLAGS: return new SetLivingEntityFlags(reader);
			case SET_MAIN_HAND: return new SetMainHand(reader);
			case SWING: return new Swing(reader);
			case BREAK_BLOCK: return new BreakBlock(reader);
			case PLACE_BLOCK: return new PlaceBlock(reader);
			case RIGHT_CLICK_BLOCK: return new RightClickBlock(reader);
			default: return null;
		}
	}
}
