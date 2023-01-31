package com.mt1006.mocap.mocap.playing;

import com.mt1006.mocap.mocap.actions.*;
import com.mt1006.mocap.mocap.files.RecordingFile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PlayerActions
{
	public final PlayerMovement playerMovement;
	public final HeadRotation headRotation;
	public final ChangePose changePose;
	public final ChangeItem changeItem;
	public final SetEntityFlags setEntityFlags;
	public final SetLivingEntityFlags setLivingEntityFlags;
	public final SetMainHand setMainHand;
	public final Swing swing;

	public PlayerActions(Player player)
	{
		playerMovement = new PlayerMovement(player);
		headRotation = new HeadRotation(player);
		changePose = new ChangePose(player);
		changeItem = new ChangeItem(player);
		setEntityFlags = new SetEntityFlags(player);
		setLivingEntityFlags = new SetLivingEntityFlags(player);
		setMainHand = new SetMainHand(player);
		swing = new Swing(player);
	}

	public boolean differs(@Nullable PlayerActions previousActions)
	{
		if (previousActions == null) { return false; }
		return playerMovement.differs(previousActions) ||
				headRotation.differs(previousActions) ||
				changePose.differs(previousActions) ||
				changeItem.differs(previousActions) ||
				setEntityFlags.differs(previousActions) ||
				setLivingEntityFlags.differs(previousActions) ||
				setMainHand.differs(previousActions) ||
				swing.differs(previousActions);
	}

	public void saveDifference(RecordingFile.Writer writer, @Nullable PlayerActions previousActions)
	{
		if (previousActions == null)
		{
			writer.addByte(RecordingFile.RECORDING_VERSION);
			playerMovement.writeAsHeader(writer);
		}

		playerMovement.write(writer, previousActions);
		headRotation.write(writer, previousActions);
		changePose.write(writer, previousActions);
		changeItem.write(writer, previousActions);
		setEntityFlags.write(writer, previousActions);
		setLivingEntityFlags.write(writer, previousActions);
		setMainHand.write(writer, previousActions);
		swing.write(writer, previousActions);

		new NextTick().write(writer);
	}

	public static void initFakePlayer(Player fakePlayer, RecordingData recordingData, Vec3 posOffset)
	{
		fakePlayer.moveTo(
				recordingData.startPos[0] + posOffset.x,
				recordingData.startPos[1] + posOffset.y,
				recordingData.startPos[2] + posOffset.z,
				recordingData.startRot[0],
				recordingData.startRot[1]);
	}
}
