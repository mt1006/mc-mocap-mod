package net.mt1006.mocap.mocap.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBasicActionContext;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;

public class RightClickBlock implements MocapBlockAction
{
	private final BlockHitResult blockHitResult;
	private final boolean offHand;

	public RightClickBlock(BlockHitResult blockHitResult, boolean offHand)
	{
		this.blockHitResult = blockHitResult;
		this.offHand = offHand;
	}

	public RightClickBlock(Reader reader)
	{
		Vec3 pos = reader.readVec3();
		BlockPos blockPos = reader.readBlockPos();
		Direction direction = directionFromByte(reader.readByte());
		boolean inside = reader.readBoolean();

		blockHitResult = new BlockHitResult(pos, direction, blockPos, inside);
		offHand = reader.readBoolean();
	}

	private Direction directionFromByte(byte val)
	{
		return switch (val)
		{
			case 1 -> Direction.UP;
			case 2 -> Direction.NORTH;
			case 3 -> Direction.SOUTH;
			case 4 -> Direction.WEST;
			case 5 -> Direction.EAST;
			default -> Direction.DOWN;
		};
	}

	private byte directionToByte(Direction direction)
	{
		return switch (direction)
		{
			case UP -> 1;
			case NORTH -> 2;
			case SOUTH -> 3;
			case WEST -> 4;
			case EAST -> 5;
			default -> 0;
		};
	}

	@Override public void write(Writer writer, MocapRecordingData data)
	{
		writer.addVec3(blockHitResult.getLocation());
		writer.addBlockPos(blockHitResult.getBlockPos());

		writer.addByte(directionToByte(blockHitResult.getDirection()));
		writer.addBoolean(blockHitResult.isInside());

		writer.addBoolean(offHand);
	}

	@Override public void initBlocks(MocapBasicActionContext ctx) {}

	@Override public Result execute(MocapActionContext ctx)
	{
		Player player = ctx.getRealOrDummyPlayer();
		if (player == null) { return Result.IGNORED; }

		InteractionHand interactionHand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack itemStack = player.getItemInHand(interactionHand);

		boolean allowScaled = ctx.getConfig().getBlockAllowScaled();
		for (BlockPos blockPos : ctx.getTransformer().transformBlockPos(blockHitResult.getBlockPos(), allowScaled))
		{
			BlockState blockState = ctx.getLevel().getBlockState(blockPos);
			if (blockState.getBlock() instanceof BedBlock) { continue; }

			InteractionResult result = blockState.useItemOn(itemStack, ctx.getLevel(), player, interactionHand, blockHitResult);
			if (result == InteractionResult.TRY_WITH_EMPTY_HAND)
			{
				blockState.useWithoutItem(ctx.getLevel(), player, blockHitResult);
			}
		}
		return Result.OK;
	}
}
