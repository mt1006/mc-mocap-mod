package net.mt1006.mocap.api.v1.modifiers;

import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;

public class MocapEntityFilterBuilder
{
	private final StringBuilder builder = new StringBuilder();

	public MocapEntityFilterBuilder includeEntity(Identifier id)
	{
		builder.append(id.toString());
		return this;
	}

	public MocapEntityFilterBuilder excludeEntity(Identifier id)
	{
		builder.append("-");
		return includeEntity(id);
	}

	public MocapEntityFilterBuilder includeEntity(EntityType<?> entityType)
	{
		return includeEntity(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
	}

	public MocapEntityFilterBuilder excludeEntity(EntityType<?> entityType)
	{
		return excludeEntity(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
	}

	public MocapEntityFilterBuilder includeAll()
	{
		builder.append("*");
		return this;
	}

	public MocapEntityFilterBuilder excludeAll()
	{
		builder.append("-*");
		return this;
	}

	public MocapEntityFilterBuilder includeNamespace(String namespace)
	{
		if (!Identifier.isValidNamespace(namespace)) { throw new IdentifierException("Invalid namespace!"); }
		builder.append(namespace);
		builder.append(":*");
		return this;
	}

	public MocapEntityFilterBuilder excludeNamespace(String namespace)
	{
		builder.append("-");
		return includeNamespace(namespace);
	}

	public MocapEntityFilterBuilder includeTag(String tag)
	{
		//TODO: check for semicolons
		builder.append("$");
		builder.append(tag);
		return this;
	}

	public MocapEntityFilterBuilder excludeTag(String tag)
	{
		builder.append("-");
		return includeTag(tag);
	}

	public MocapEntityFilterBuilder includeGroup(Group group)
	{
		builder.append("@");
		builder.append(group.name());
		builder.append(";");
		return this;
	}

	public MocapEntityFilterBuilder excludeGroup(Group group)
	{
		builder.append("-");
		return includeGroup(group);
	}

	public MocapEntityFilterBuilder appendFilter(String filter)
	{
		builder.append(filter);
		return this;
	}

	public MocapEntityFilter build()
	{
		if (builder.isEmpty()) { includeGroup(Group.NONE); }
		return EntityFilter.fromString(builder.toString());
	}

	public enum Group
	{
		NONE,
		VEHICLES,
		PROJECTILES,
		ITEMS,
		MOBS,
		MINECARTS
	}
}
