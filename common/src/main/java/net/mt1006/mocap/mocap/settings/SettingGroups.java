package net.mt1006.mocap.mocap.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingGroups
{
	final public Map<String, Group> groupMap = new HashMap<>();

	public Group add(String name)
	{
		Group group = new Group(name);
		if (groupMap.put(name, group) != null) { throw new RuntimeException("Duplicate group names!"); }
		return group;
	}

	public static class Group
	{
		public final String name;
		public final List<SettingFields.Field<?>> fields = new ArrayList<>();

		private Group(String name)
		{
			this.name = name;
		}

		public <T extends SettingFields.Field<?>> T add(T field)
		{
			fields.add(field);
			return field;
		}
	}
}
