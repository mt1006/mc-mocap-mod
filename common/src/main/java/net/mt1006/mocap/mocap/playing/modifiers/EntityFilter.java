package net.mt1006.mocap.mocap.playing.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.api.v1.modifiers.MocapEntityFilter;
import net.mt1006.mocap.mocap.files.Files;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EntityFilter implements MocapEntityFilter
{
	private static final String EMPTY_GROUP = "none";
	private final String str;
	private final List<Element> elements;

	public static @Nullable EntityFilter fromString(@Nullable String str)
	{
		if (str == null) { return null; }
		try
		{
			List<Element> elements = parse(str);
			return new EntityFilter(str, elements);
		}
		catch (FilterParserException e) { return null; }
	}

	private static List<Element> parse(String str) throws FilterParserException
	{
		List<Element> elements = new ArrayList<>();
		if (str.isEmpty()) { return elements; }
		String[] parts = str.split(";");

		for (String part : parts)
		{
			if (part.isBlank()) { throw new FilterParserException(); }

			boolean exclude = (part.charAt(0) == '-');
			if (exclude && part.length() == 1) { throw new FilterParserException(); }

			char firstChar = part.charAt(exclude ? 1 : 0);

			if (firstChar == '@')
			{
				String groupName = part.substring(exclude ? 2 : 1);
				if (groupName.equals(EMPTY_GROUP)) { continue; } // "@none" is just ignored
				if (!Files.checkIfProperName(CommandOutput.DUMMY, groupName)) { throw new FilterParserException(); }

				GroupElement groupElement = Group.fromString(exclude, groupName);
				if (groupElement == null) { throw new FilterParserException(); }
				elements.add(groupElement);
			}
			else if (firstChar == '$')
			{
				String tagName = part.substring(exclude ? 2 : 1);
				elements.add(new TagElement(exclude, tagName));
			}
			else
			{
				String name = exclude ? part.substring(1) : part;

				if (name.equals("*"))
				{
					elements.add(exclude ? AllEntitiesElement.EXCLUDE_ALL : AllEntitiesElement.INCLUDE_ALL);
					continue;
				}

				if (name.endsWith(":*"))
				{
					if (name.length() == 2) { throw new FilterParserException(); }
					String namespace = name.substring(0, name.length() - 2);
					if (!Identifier.isValidNamespace(namespace)) { throw new FilterParserException(); }

					elements.add(new AllEntitiesElement(exclude, namespace));
					continue;
				}

				Identifier id = parseToId(name);
				Element lastElement = elements.isEmpty() ? null : elements.get(elements.size() - 1);

				boolean reuseEntitySet = (lastElement instanceof EntitySetElement && lastElement.exclude == exclude);
				EntitySetElement entitySetElement = reuseEntitySet ? (EntitySetElement)lastElement : new EntitySetElement(exclude);
				entitySetElement.add(id);
				if (!reuseEntitySet) { elements.add(entitySetElement); }
			}
		}
		return elements;
	}

	private EntityFilter(String str, List<Element> elements)
	{
		this.str = str;
		this.elements = elements;
	}

	@Override public boolean isAllowed(Entity entity)
	{
		boolean allowed = false;
		for (Element element : elements)
		{
			switch (element.isAllowed(entity))
			{
				case ALLOW -> allowed = true;
				case DENY -> allowed = false;
				case IGNORE -> {}
			}
		}
		return allowed;
	}

	@Override public boolean isEmpty()
	{
		return elements.isEmpty();
	}

	@Override public @Nullable String getFilterString()
	{
		return str;
	}

	@Override public @Nullable String save()
	{
		return str;
	}

	@Override public String toString()
	{
		return str;
	}
	
	private static Identifier parseToId(String str) throws FilterParserException
	{
		Identifier id = Identifier.tryParse(str);
		if (id == null) { throw new FilterParserException(); }
		return id;
	}

	private static abstract class Element
	{
		private final boolean exclude;

		protected Element(boolean exclude)
		{
			this.exclude = exclude;
		}

		public FilterElementResults isAllowed(Entity entity)
		{
			return applies(entity)
					? (exclude ? FilterElementResults.DENY : FilterElementResults.ALLOW)
					: FilterElementResults.IGNORE;
		}

		protected abstract boolean applies(Entity entity);
	}

	private static class GroupElement extends Element
	{
		public final String name;
		private final List<Class<?>> parents;

		public GroupElement(boolean exclude, String name, List<Class<?>> parents)
		{
			super(exclude);
			this.name = name;
			this.parents = parents;
		}

		protected boolean applies(Entity entity)
		{
			for (Class<?> parent : parents)
			{
				if (parent.isInstance(entity)) { return true; }
			}
			return false;
		}
	}

	private static class TagElement extends Element
	{
		public final String tag;

		public TagElement(boolean exclude, String tag)
		{
			super(exclude);
			this.tag = tag;
		}

		@Override protected boolean applies(Entity entity)
		{
			return entity.entityTags().contains(tag);
		}
	}

	private static class EntitySetElement extends Element
	{
		public final Set<EntityType<?>> set = new HashSet<>(); //TODO: make it immutable

		public EntitySetElement(boolean exclude)
		{
			super(exclude);
		}

		public void add(Identifier id)
		{
			Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
			entityType.ifPresent(set::add);
		}

		@Override protected boolean applies(Entity entity)
		{
			return set.contains(entity.getType());
		}
	}

	private static class AllEntitiesElement extends Element
	{
		public final static AllEntitiesElement INCLUDE_ALL = new AllEntitiesElement(false, null);
		public final static AllEntitiesElement EXCLUDE_ALL = new AllEntitiesElement(false, null);
		private final @Nullable String fromNamespace;

		public AllEntitiesElement(boolean exclude, @Nullable String fromNamespace)
		{
			super(exclude);
			this.fromNamespace = fromNamespace;
		}

		@Override protected boolean applies(Entity entity)
		{
			if (fromNamespace == null) { return true; }
			return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals(fromNamespace);
		}
	}

	public static class FilterParserException extends Exception {}

	private enum FilterElementResults
	{
		ALLOW, DENY, IGNORE
	}

	private enum Group
	{
		VEHICLES(List.of(PlayerRideable.class, Minecart.class, Boat.class)),
		PROJECTILES(List.of(Projectile.class)),
		ITEMS(List.of(ItemEntity.class)),
		MOBS(List.of(Mob.class)),
		MINECARTS(List.of(AbstractMinecart.class));

		public final String name;
		public final GroupElement include, exclude;

		Group(List<Class<?>> parent)
		{
			this.name = name().toLowerCase(Locale.ROOT);
			this.include = new GroupElement(false, name, parent);
			this.exclude = new GroupElement(true, name, parent);
		}

		public static @Nullable GroupElement fromString(boolean exclude, String str)
		{
			try
			{
				Group group = valueOf(str.toUpperCase(Locale.ROOT));
				return exclude ? group.exclude : group.include;
			}
			catch (IllegalArgumentException e) { return null; }
		}
	}
}
