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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.mt1006.mocap.command.io.CommandInfo;
import net.mt1006.mocap.command.io.FullCommandInfo;
import net.mt1006.mocap.mocap.files.Files;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class SettingFields
{
	public final Map<String, Field<?>> fieldMap = new HashMap<>();

	//TODO: remove?
	/*public IntegerField add(String name, int val)
	{
		IntegerField field = new IntegerField(name, val);
		addField(field, name);
		return field;
	}*/

	public BooleanField add(String name, boolean val)
	{
		BooleanField field = new BooleanField(name, val);
		addField(field, name);
		return field;
	}

	public DoubleField add(String name, double val)
	{
		DoubleField field = new DoubleField(name, val);
		addField(field, name);
		return field;
	}

	public StringField add(String name, String val, @Nullable Consumer<String> onSet,
						   @Nullable Function<String, Boolean> testCommandInput)
	{
		StringField field = new StringField(name, val, onSet, testCommandInput);
		addField(field, name);
		return field;
	}

	public <T extends Enum<T>> EnumField<T> add(String name, Enum<T> val)
	{
		EnumField<T> field = new EnumField<>(name, val);
		addField(field, name);
		return field;
	}

	private void addField(Field<?> field, String name)
	{
		if (fieldMap.put(name, field) != null) { throw new RuntimeException("Duplicate field names!"); };
	}

	public void save()
	{
		try
		{
			File settingsFile = Files.getSettingsFile();
			if (settingsFile == null) { return; }
			PrintWriter printWriter = new PrintWriter(settingsFile);

			for (Field<?> setting : fieldMap.values())
			{
				printWriter.print(setting.toFileLine());
			}

			printWriter.close();
		}
		catch (Exception ignore) {}
	}

	public void load()
	{
		//TODO: improve loading settings?
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
		private final @Nullable Consumer<T> onSet;
		public final T defVal;
		public T val;

		public Field(String name, T defVal, @Nullable Consumer<T> onSet)
		{
			this.name = name;
			this.defVal = defVal;
			this.val = defVal;
			this.onSet = onSet;

			if (onSet != null) { onSet.accept(val); }
		}

		public void set(T val)
		{
			this.val = val;
			if (onSet != null) { onSet.accept(val); }
		}

		public void reset()
		{
			set(defVal);
		}

		public Component getInfo(CommandInfo commandInfo)
		{
			String key = val.equals(defVal) ? "settings.list.info_def" : "settings.list.info";
			return commandInfo.getTranslatableComponent(key, name, valToString());
		}

		public void printValues(CommandInfo commandInfo)
		{
			commandInfo.sendSuccess("settings.info.current_value", valToString());
			commandInfo.sendSuccess("settings.info.default_value", valToString(defVal));
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

		public final boolean fromCommand(FullCommandInfo commandInfo)
		{
			try
			{
				T newVal = parseFromCommand(commandInfo);
				if (newVal == null)
				{
					commandInfo.sendFailure("settings.set.invalid_value");
					return false;
				}

				set(newVal);
				return true;
			}
			catch (Exception e)
			{
				commandInfo.sendException(e, "settings.set.error");
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
		public abstract @Nullable T parseFromCommand(FullCommandInfo commandInfo);
		public abstract ArgumentType<?> getArgumentType();
	}

	//TODO: remove?
	/*public static class IntegerField extends Field<Integer>
	{
		public IntegerField(String name, Integer val)
		{
			super(name, val, null);
		}

		@Override public Integer parseFromString(String str)
		{
			return Integer.valueOf(str);
		}

		@Override public Integer parseFromCommand(CommandInfo commandInfo)
		{
			return commandInfo.getInteger("new_value");
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return IntegerArgumentType.integer();
		}
	}*/

	public static class BooleanField extends Field<Boolean>
	{
		public BooleanField(String name, Boolean val)
		{
			super(name, val, null);
		}

		@Override public Boolean parseFromString(String str)
		{
			return Boolean.valueOf(str);
		}

		@Override public Boolean parseFromCommand(FullCommandInfo commandInfo)
		{
			return commandInfo.getBool("new_value");
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
			super(name, val, null);
		}

		@Override public Double parseFromString(String str)
		{
			return Double.valueOf(str);
		}

		@Override public Double parseFromCommand(FullCommandInfo commandInfo)
		{
			return commandInfo.getDouble("new_value");
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return DoubleArgumentType.doubleArg();
		}
	}

	public static class StringField extends Field<String>
	{
		private final @Nullable Function<String, Boolean> testCommandInput;

		public StringField(String name, String val, @Nullable Consumer<String> onSet,
						   @Nullable Function<String, Boolean> testCommandInput)
		{
			super(name, val, onSet);
			this.testCommandInput = testCommandInput;
		}

		@Override public String parseFromString(String str)
		{
			return (testCommandInput == null || testCommandInput.apply(str)) ? str : defVal;
		}

		@Override public @Nullable String parseFromCommand(FullCommandInfo commandInfo)
		{
			String newValue = commandInfo.getString("new_value");
			return (testCommandInput == null || testCommandInput.apply(newValue)) ? newValue : null;
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return StringArgumentType.greedyString();
		}

		@Override public void printValues(CommandInfo commandInfo)
		{
			String valStr = val, defValStr = defVal;

			commandInfo.sendSuccess("settings.info.string_value",
					commandInfo.getTranslatableComponent("settings.info.current_value", valStr),
					createButton(commandInfo, valStr));

			commandInfo.sendSuccess("settings.info.string_value",
					commandInfo.getTranslatableComponent("settings.info.default_value", defValStr),
					createButton(commandInfo, defValStr));
		}

		private static Component createButton(CommandInfo commandInfo, String textToCopy)
		{
			ClickEvent clickEvent = new ClickEvent.CopyToClipboard(textToCopy);
			HoverEvent hoverEvent = new HoverEvent.ShowText(
					commandInfo.getTranslatableComponent("settings.info.copy_button_info"));

			return commandInfo.getTranslatableComponent("settings.info.copy_button")
					.setStyle(Style.EMPTY.withClickEvent(clickEvent).withHoverEvent(hoverEvent));
		}
	}

	public static class EnumField<T extends Enum<T>> extends Field<Enum<T>>
	{
		private final Class<T> enumClass;
		private final T[] constants;

		public EnumField(String name, Enum<T> val)
		{
			super(name, val, null);
			enumClass = (Class<T>)val.getClass();
			constants = enumClass.getEnumConstants();
		}

		@Override public Enum<T> parseFromString(String str)
		{
			return Enum.valueOf(enumClass, str.toUpperCase());
		}

		@Override public @Nullable Enum<T> parseFromCommand(FullCommandInfo commandInfo)
		{
			String newValue = commandInfo.getString("new_value");
			try { return Enum.valueOf(enumClass, newValue.toUpperCase()); }
			catch (Exception e) { return null; }
		}

		@Override public ArgumentType<?> getArgumentType()
		{
			return StringArgumentType.word();
		}

		@Override protected String valToString(Enum<T> val)
		{
			return val.toString().toLowerCase();
		}

		public SuggestionProvider<CommandSourceStack> getSuggestionProvider()
		{
			return this::suggestionProvider;
		}

		private CompletableFuture<Suggestions> suggestionProvider(CommandContext<?> ctx, SuggestionsBuilder builder)
		{
			String remaining = builder.getRemaining();
			for (Enum<T> e : constants)
			{
				String str = e.name().toLowerCase();
				if (str.startsWith(remaining)) { builder.suggest(str); }
			}
			return builder.buildFuture();
		}
	}
}
