package net.mt1006.mocap.mocap.settings;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.mt1006.mocap.api.v1.io.CommandInfo;
import net.mt1006.mocap.command.CommandSuggestions;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.mocap.playing.modifiers.EntityFilter;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class SettingFields
{
	public final Map<String, Field<?>> fieldMap = new HashMap<>();

	public BooleanField add(String name, boolean val)
	{
		return addField(name, new BooleanField(name, val));
	}

	public DoubleField add(String name, double val)
	{
		return addField(name, new DoubleField(name, val));
	}

	public <T extends Enum<T>> EnumField<T> add(String name, T val)
	{
		return addField(name, new EnumField<>(name, val));
	}

	public EntityFilterField add(String name, EntityFilter val)
	{
		return addField(name, new EntityFilterField(name, val));
	}

	private <T extends Field<?>> T addField(String name, T field)
	{
		if (fieldMap.put(name, field) != null) { throw new RuntimeException("Duplicate field names!"); }
		return field;
	}

	public void save()
	{
		try
		{
			File settingsFile = Files.getSettingsFile();
			if (settingsFile == null) { return; }
			PrintWriter printWriter = new PrintWriter(settingsFile);

			fieldMap.values().forEach((f) -> printWriter.print(f.toFileLine()));
			printWriter.close();
		}
		catch (Exception ignore) {}
	}

	public void load()
	{
		try
		{
			File settingsFile = Files.getSettingsFile();
			if (settingsFile == null) { return; }

			Scanner fileScanner = new Scanner(settingsFile);
			while (fileScanner.hasNextLine())
			{
				String line = fileScanner.nextLine();
				if (line.isEmpty()) { continue; }
				String[] parts = line.split("=");

				if (parts.length != 2)
				{
					fileScanner.close();
					return;
				}

				Field<?> field = fieldMap.get(parts[0]);
				if (field == null) { continue; }

				field.setFromString(parts[1]);
			}
			fileScanner.close();
		}
		catch (Exception ignore) {}
	}

	public void unload()
	{
		fieldMap.values().forEach(Field::reset);
	}

	public abstract static class Field<T>
	{
		public final String name;
		public final T defVal;
		public T val;

		public Field(String name, T defVal)
		{
			this.name = name;
			this.defVal = defVal;
			this.val = defVal;
		}

		public void set(T val)
		{
			this.val = val;
		}

		public void reset()
		{
			set(defVal);
		}

		public Component getInfo(CommandInfo info)
		{
			String key = val.equals(defVal) ? "settings.list.info_def" : "settings.list.info";
			return info.getTranslatableComponent(key, name, valToString());
		}

		public void printValues(CommandInfo info)
		{
			info.sendSuccess("settings.info.current_value", valToString());
			info.sendSuccess("settings.info.default_value", valToString(defVal));
		}

		public final String toFileLine()
		{
			return name + "=" + valToString() + "\n";
		}

		public final boolean setFromString(String str)
		{
			try
			{
				set(parseFromString(str));
				return true;
			}
			catch (Exception e)
			{
				Utils.exception(e, "Failed to load settings from string");
				return false;
			}
		}

		public final boolean fromCommand(FullCommandInfo info)
		{
			try
			{
				T newVal = parseFromCommand(info);
				if (newVal == null)
				{
					info.sendFailure("settings.set.invalid_value");
					return false;
				}

				set(newVal);
				return true;
			}
			catch (Exception e)
			{
				info.sendException(e, "settings.set.error");
				reset();
				return false;
			}
		}

		public final String valToString()
		{
			return valToString(val);
		}

		protected String valToString(T val)
		{
			return val.toString();
		}

		public @Nullable SuggestionProvider<CommandSourceStack> getSuggestionProvider()
		{
			return null;
		}

		public abstract T parseFromString(String str);
		public abstract @Nullable T parseFromCommand(FullCommandInfo info);
		public abstract ArgumentType<?> getArgumentType();
	}

	public static class BooleanField extends Field<Boolean>
	{
		public BooleanField(String name, Boolean val)
		{
			super(name, val);
		}

		@Override public Boolean parseFromString(String str)
		{
			return Boolean.valueOf(str);
		}

		@Override public Boolean parseFromCommand(FullCommandInfo info)
		{
			return info.getBool("new_value");
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return BoolArgumentType.bool();
		}
	}

	public static class DoubleField extends Field<Double>
	{
		public DoubleField(String name, Double val)
		{
			super(name, val);
		}

		@Override public Double parseFromString(String str)
		{
			return Double.valueOf(str);
		}

		@Override public Double parseFromCommand(FullCommandInfo info)
		{
			return info.getDouble("new_value");
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return DoubleArgumentType.doubleArg();
		}
	}

	public static class EntityFilterField extends Field<EntityFilter>
	{
		public EntityFilterField(String name, EntityFilter val)
		{
			super(name, val);
		}

		@Override public EntityFilter parseFromString(String str)
		{
			EntityFilter filter = EntityFilter.fromString(str);
			return filter != null ? filter : defVal;
		}

		@Override public @Nullable EntityFilter parseFromCommand(FullCommandInfo info)
		{
			return EntityFilter.fromString(info.getString("new_value"));
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return StringArgumentType.greedyString();
		}

		@Override public void printValues(CommandInfo info)
		{
			String valStr = val.getFilterString();
			String defValStr = defVal.getFilterString();

			info.sendSuccess("settings.info.string_value",
					info.getTranslatableComponent("settings.info.current_value", valStr),
					info.createCopyButton(valStr));

			info.sendSuccess("settings.info.string_value",
					info.getTranslatableComponent("settings.info.default_value", defValStr),
					info.createCopyButton(defValStr));
		}

		@Override public SuggestionProvider<CommandSourceStack> getSuggestionProvider()
		{
			return CommandSuggestions::entityFilter;
		}
	}

	public static class EnumField<T extends Enum<T>> extends Field<T>
	{
		private final Class<T> enumClass;
		private final T[] constants;

		public EnumField(String name, T val)
		{
			super(name, val);
			enumClass = (Class<T>)val.getClass();
			constants = enumClass.getEnumConstants();
		}

		@Override public T parseFromString(String str)
		{
			return Enum.valueOf(enumClass, str.toUpperCase(Locale.ROOT));
		}

		@Override public @Nullable T parseFromCommand(FullCommandInfo info)
		{
			String newValue = info.getString("new_value");
			try { return Enum.valueOf(enumClass, newValue.toUpperCase(Locale.ROOT)); }
			catch (Exception e) { return null; }
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return StringArgumentType.word();
		}

		@Override protected String valToString(T val)
		{
			return val.toString().toLowerCase(Locale.ROOT);
		}

		@Override public SuggestionProvider<CommandSourceStack> getSuggestionProvider()
		{
			return this::suggestionProvider;
		}

		private CompletableFuture<Suggestions> suggestionProvider(CommandContext<?> ctx, SuggestionsBuilder builder)
		{
			String remaining = builder.getRemaining();
			for (Enum<T> e : constants)
			{
				String str = e.name().toLowerCase(Locale.ROOT);
				if (str.startsWith(remaining)) { builder.suggest(str); }
			}
			return builder.buildFuture();
		}
	}
}
