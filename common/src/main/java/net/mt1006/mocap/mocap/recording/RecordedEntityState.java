package net.mt1006.mocap.mocap.recording;

import net.minecraft.world.entity.Entity;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.mocap.actions.ActionType;
import net.mt1006.mocap.mocap.actions.EntityAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecordedEntityState
{
	public final List<@Nullable MocapStateAction> actions;

	public RecordedEntityState(Entity entity)
	{
		actions = new ArrayList<>(ActionType.Registry.allComparableActions.size());
		ActionType.Registry.allComparableActions.forEach((supplier) -> actions.add(supplier.apply(entity)));
	}

	public boolean differs(@Nullable RecordedEntityState previousActions)
	{
		if (previousActions == null) { return false; }
		for (int i = 0; i < actions.size(); i++)
		{
			MocapStateAction action = actions.get(i);
			MocapStateAction previousAction = previousActions.actions.get(i);

			if (action != null && previousAction == null) { throw new RuntimeException("State action isn't null, but previous was!"); }
			if (action != null && action.differs(previousAction)) { return true; }
		}
		return false;
	}

	public void saveDifference(List<MocapAction> actionList, @Nullable RecordedEntityState previousActions)
	{
		if (previousActions == null)
		{
			for (MocapStateAction action : actions)
			{
				if (action != null && action.shouldBeInitialized()) { actionList.add(action); }
			}
			return;
		}

		for (int i = 0; i < actions.size(); i++)
		{
			MocapStateAction action = actions.get(i);
			MocapStateAction previousAction = previousActions.actions.get(i);

			if (action != null && previousAction == null) { throw new RuntimeException("State action isn't null, but previous was!"); }
			if (action != null && action.differs(previousAction)) { actionList.add(action); }
		}
	}

	public void saveTrackedEntityDifference(List<MocapAction> actionList, int id, @Nullable RecordedEntityState previousActions)
	{
		if (previousActions == null)
		{
			for (MocapStateAction action : actions)
			{
				if (action != null && action.shouldBeInitialized()) { actionList.add(new EntityAction(id, action)); }
			}
			return;
		}

		for (int i = 0; i < ActionType.Registry.allComparableActions.size(); i++)
		{
			MocapStateAction action = actions.get(i);
			MocapStateAction previousAction = previousActions.actions.get(i);

			if (action != null && previousAction == null) { throw new RuntimeException("State action isn't null, but previous was!"); }
			if (action != null && action.differs(previousAction)) { actionList.add(new EntityAction(id, action)); }
		}
	}
}
