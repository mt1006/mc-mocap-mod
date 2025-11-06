package net.mt1006.mocap.mocap.files;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.mt1006.mocap.MocapMod;
import net.mt1006.mocap.api.impl.extenstion.Extensions;
import net.mt1006.mocap.api.v1.controller.config.MocapPlaybackConfig;
import net.mt1006.mocap.api.v1.extension.MocapExtension;
import net.mt1006.mocap.api.v1.extension.MocapPositionTransformer;
import net.mt1006.mocap.api.v1.extension.MocapRecordingData;
import net.mt1006.mocap.api.v1.extension.actions.MocapAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapBlockAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapStateAction;
import net.mt1006.mocap.api.v1.extension.actions.MocapTickAction;
import net.mt1006.mocap.api.v1.io.CommandOutput;
import net.mt1006.mocap.command.converter.AlphaConverter;
import net.mt1006.mocap.mocap.actions.*;
import net.mt1006.mocap.mocap.playing.playable.RecordingFile;
import net.mt1006.mocap.mocap.playing.playback.ActionContext;
import net.mt1006.mocap.mocap.playing.playback.PreExecuteContext;
import net.mt1006.mocap.mocap.recording.PositionTracker;
import net.mt1006.mocap.utils.EntityData;
import net.mt1006.mocap.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;

public class RecordingData implements MocapRecordingData
{
	public static final RecordingData DUMMY = new RecordingData();

	private static final byte FLAGS1_ENDS_WITH_DEATH =         0b00000001;
	private static final byte FLAGS1_PACKED_SIZE_STRINGS =     0b00000010;
	private static final byte FLAGS1_HAS_ID_MAPS =             0b00000100;
	private static final byte FLAGS1_DIMENSION_SPECIFIED =     0b00001000;
	private static final byte FLAGS1_PLAYER_NAME_SPECIFIED =   0b00010000;
	private static final byte FLAGS1_HAS_EXTENSIONS =          0b00100000;
	private static final byte FLAGS1_EXPERIMENTAL_SUBVERSION = 0b01000000;
	private static final byte FLAGS1_HAS_FLAGS2 =        (byte)0b10000000;

	public long fileSize = 0;
	public byte version = 0;
	public boolean experimentalVersion = false;
	public Vec3 startPos = Vec3.ZERO;
	public final float[] startRot = new float[2];
	public boolean endsWithDeath = false; // deprecated
	private boolean usesIdMaps = true;
	private final ItemIdMap itemIdMap = new ItemIdMap(this);
	private final BlockStateIdMap blockStateIdMap = new BlockStateIdMap(this);
	public @Nullable ResourceLocation dimensionId = null; //TODO: use it
	public @Nullable String playerName = null;
	private final SortedMap<Integer, MocapExtension> extensionById = new TreeMap<>();
	private final Map<MocapExtension, Byte> extensionToId = new HashMap<>();
	private final Map<MocapExtension, ExtensionHeader> extensionHeaders = new HashMap<>();
	public byte experimentalSubversion = 0;
	public final List<MocapAction> actions = new ArrayList<>();
	public final List<MocapBlockAction> blockActions = new ArrayList<>();
	public long tickCount = 0;

	public static RecordingData forWriting()
	{
		RecordingData data = new RecordingData();
		data.setCurrentVersion();
		return data;
	}

	public void save(BufferedOutputStream stream) throws IOException
	{
		if (version != RecordingFiles.VERSION) { throw new RuntimeException("Trying to save recording with read-only version."); }
		actions.forEach((a) -> ActionType.prepareToWriteAction(this, a));

		RecordingFiles.Writer writer = new RecordingFiles.Writer();

		writer.addByte((byte)(experimentalVersion ? (-version) : version));
		saveHeader(writer);
		actions.forEach((a) -> ActionType.writeAction(writer, this, a));

		stream.write(writer.toByteArray());
	}

	//TODO: [CONVERTER] remove last arg
	public boolean load(CommandOutput out, @Nullable RecordingFile file, boolean useConverter)
	{
		if (file == null) { return false; }
		byte[] data = Files.loadFile(file.getFile());
		return data != null && load(out, new RecordingFiles.FileReader(data, true), useConverter);
	}

	//TODO: [CONVERTER] remove last arg
	private boolean load(CommandOutput out, RecordingFiles.FileReader reader, boolean useConverter)
	{
		fileSize = reader.getSize();

		byte versionByte = reader.readByte();
		version = (byte)Math.abs(versionByte);
		experimentalVersion = (versionByte < 0);

		if (version > RecordingFiles.VERSION)
		{
			out.sendFailure("playback.start.error.load_header");
			return false;
		}

		if (!loadHeader(out, reader, version == 1 || version == 2, useConverter)) { return false; } //TODO: test old recordings

		//TODO: [CONVERTER] remove
		AlphaConverter converter = (experimentalVersion && version == 5 && experimentalSubversion == 0 && useConverter)
				? new AlphaConverter(startPos)
				: null;

		while (reader.canRead())
		{
			MocapAction action = ActionType.readAction(reader, this, converter, null);
			if (action == null) { return false; }

			actions.add(action);
			if (action instanceof MocapBlockAction) { blockActions.add((MocapBlockAction)action); }
			else if (action instanceof MocapTickAction tickAction) { tickCount += tickAction.getTickCount(); }
		}
		return true;
	}

	private void saveHeader(RecordingFiles.Writer writer)
	{
		boolean hasIdMaps = usesIdMaps && (itemIdMap.size() != 0 || blockStateIdMap.size() != 0);

		writer.addVec3(startPos);
		writer.addFloat(startRot[0]);
		writer.addFloat(startRot[1]);

		byte flags1 = 0;
		flags1 |= endsWithDeath ? FLAGS1_ENDS_WITH_DEATH : 0;
		flags1 |= FLAGS1_PACKED_SIZE_STRINGS;
		flags1 |= hasIdMaps ? FLAGS1_HAS_ID_MAPS : 0;
		flags1 |= dimensionId != null ? FLAGS1_DIMENSION_SPECIFIED : 0;
		flags1 |= playerName != null ? FLAGS1_PLAYER_NAME_SPECIFIED : 0;
		flags1 |= !extensionById.isEmpty() ? FLAGS1_HAS_EXTENSIONS : 0;
		flags1 |= experimentalSubversion != 0 ? FLAGS1_EXPERIMENTAL_SUBVERSION : 0;
		writer.addByte(flags1);

		if (hasIdMaps)
		{
			itemIdMap.save(writer);
			blockStateIdMap.save(writer);
		}

		if (dimensionId != null) { writer.addString(dimensionId.toString()); }
		if (playerName != null) { writer.addString(playerName); }
		if (!extensionById.isEmpty()) { saveExtensionHeaders(writer); }
		if (experimentalSubversion != 0) { writer.addByte(experimentalSubversion); }
	}

	private void saveExtensionHeaders(RecordingFiles.Writer writer)
	{
		writer.addByte((byte)extensionById.size());

		int expectedId = 0;
		for (Map.Entry<Integer, MocapExtension> entry : extensionById.entrySet())
		{
			if (entry.getKey() != expectedId) { throw new RuntimeException("Extensions in wrong order! Trying to save loaded recording?"); }
			expectedId++;

			MocapExtension extension = entry.getValue();
			writer.addString(extension.getId());
			writer.addShort(extension.getVersion());
			writer.addByte(extension.isRequired() ? Extensions.FLAGS_IS_REQUIRED : 0);

			RecordingFiles.Writer headerWriter = new RecordingFiles.Writer();
			extensionHeaders.get(extension).save(headerWriter);
			writer.addPackedInt(headerWriter.getSize());
			headerWriter.copyToWriter(writer);
		}
	}

	//TODO: [CONVERTER] remove last arg
	private boolean loadHeader(CommandOutput out, RecordingFiles.FileReader reader, boolean legacyHeader, boolean useConverter)
	{
		startPos = reader.readVec3();
		startRot[0] = reader.readFloat();
		startRot[1] = reader.readFloat();
		if (legacyHeader) { return true; }

		byte flags1 = reader.readByte();
		endsWithDeath = (flags1 & FLAGS1_ENDS_WITH_DEATH) != 0;
		reader.setStringMode((flags1 & FLAGS1_PACKED_SIZE_STRINGS) == 0);
		usesIdMaps = (flags1 & FLAGS1_HAS_ID_MAPS) != 0;
		boolean startDimensionSpecified = (flags1 & FLAGS1_DIMENSION_SPECIFIED) != 0;
		boolean playerNameSpecified = (flags1 & FLAGS1_PLAYER_NAME_SPECIFIED) != 0;
		boolean hasExtensions = (flags1 & FLAGS1_HAS_EXTENSIONS) != 0;
		boolean hasExperimentalSubversion = (flags1 & FLAGS1_EXPERIMENTAL_SUBVERSION) != 0;

		if (experimentalVersion && version == 5 && !hasExperimentalSubversion)
		{
			// recorded between 1.4-alpha-1 and 1.4-alpha-8
			// not compatible with current recording file format

			//TODO: [CONVERTER] do not remove! maybe move it somewhere else and replace conversion mode with failure message
			if (useConverter)
			{
				//TODO: [CONVERTER] this should be removed
				reader.convertStrings = true;
			}
			else
			{
				out.sendFailure("failure.not_supported_experimental_format");

				//TODO: [CONVERTER] remove suggestions (including language keys)
				out.sendFailure("failure.not_supported_experimental_format.suggest_converter.1");
				out.sendFailure("failure.not_supported_experimental_format.suggest_converter.2");
				return false;
			}
		}
		else if (useConverter)
		{
			return out.sendFailure("misc.converter.not_convertible");
		}

		if (usesIdMaps)
		{
			itemIdMap.load(reader);
			blockStateIdMap.load(reader);
		}

		if (startDimensionSpecified) { dimensionId = ResourceLocation.parse(reader.readString()); }
		if (playerNameSpecified) { playerName = reader.readString(); }
		if (hasExtensions && !loadExtensionHeaders(out, reader)) { return false; }
		if (hasExperimentalSubversion) { experimentalSubversion = reader.readByte(); }
		return true;
	}

	private boolean loadExtensionHeaders(CommandOutput out, RecordingFiles.FileReader reader)
	{
		int extensionCount = Byte.toUnsignedInt(reader.readByte());
		for (int i = 0; i < extensionCount; i++)
		{
			//TODO: add extension version to error str
			String extensionId = reader.readString();
			short minVersion = reader.readShort();
			boolean isRequiredFlag = (reader.readByte() & Extensions.FLAGS_IS_REQUIRED) != 0;
			int headerSize = reader.readPackedInt();

			MocapExtension extension = Extensions.getExtension(extensionId, minVersion);
			if (extension == null)
			{
				if (Extensions.isRequired(isRequiredFlag))
				{
					out.sendFailure("playback.start.error.extension.not_present", extensionId);
					return false;
				}
				else
				{
					reader.shift(headerSize);
					continue;
				}
			}

			ExtensionHeader extensionHeader = extension.createHeader();
			if (!extensionHeader.load(reader))
			{
				out.sendFailure("playback.start.error.extension.load_header", extensionId);
				return false;
			}

			addExtension(i, extension, extensionHeader);
		}
		return true;
	}

	private void addExtension(int id, MocapExtension extension, ExtensionHeader extensionHeader)
	{
		extensionById.put(id, extension);
		extensionToId.put(extension, (byte)id);
		extensionHeaders.put(extension, extensionHeader);
	}

	public void initAndAddExtension(MocapExtension extension)
	{
		addExtension(extensionById.size(), extension, extension.createHeader());
	}

	public void setCurrentVersion()
	{
		version = RecordingFiles.VERSION;
		experimentalVersion = MocapMod.EXPERIMENTAL;
		experimentalSubversion = MocapMod.EXPERIMENTAL ? MocapMod.RECORDING_FORMAT_EXP_SUBVERSION : 0;
	}

	public void initEntityPosition(Entity entity, MocapPositionTransformer transformer, boolean teleportFarAway)
	{
		Vec3 pos = teleportFarAway ? PositionTracker.FAR_AWAY : transformer.transformPos(startPos);
		float rotY = transformer.transformRotation(startRot[0]);
		entity.snapTo(pos, rotY, startRot[1]);
		entity.setYHeadRot(rotY);
	}

	public void preExecute(PreExecuteContext ctx)
	{
		if (ctx.getConfig().getBlockInitialization())
		{
			for (int i = blockActions.size() - 1; i >= 0; i--)
			{
				blockActions.get(i).initBlocks(ctx);
			}
		}
	}

	public MocapAction.Result executeAction(ActionContext ctx, MocapPlaybackConfig config, boolean initialAction, int pos)
	{
		if (pos >= actions.size()) { return MocapAction.Result.END; }

		try
		{
			MocapAction nextAction = actions.get(pos);
			if (!config.getBlockActionsPlayback() && nextAction instanceof BlockStateData) { return MocapAction.Result.OK; }
			if (initialAction)
			{
				if (nextAction instanceof MocapTickAction tickAction && tickAction.endsTick()) { return MocapAction.Result.NEXT_TICK; }
				if ((!(nextAction instanceof MocapStateAction stateAction) || !stateAction.shouldBeInitialized())) { return MocapAction.Result.IGNORED; }
			}

			return nextAction.execute(ctx);
		}
		catch (Exception e)
		{
			Utils.exception(e, "Exception occurred while executing action!");
			return MocapAction.Result.ERROR;
		}
	}

	public void firstExecute(Entity entity)
	{
		if (entity instanceof Player)
		{
			//TODO: recording skin parts
			EntityData.PLAYER_SKIN_PARTS.set(entity, (byte)0b01111111);
		}
	}

	@Override public Vec3 getStartPos()
	{
		return startPos;
	}

	@Override public Item itemFromId(int id)
	{
		return itemIdMap.getObject(id);
	}

	@Override public int provideItemId(Item item)
	{
		return itemIdMap.provideMappedId(item);
	}

	@Override public BlockState blockStateFromId(int id)
	{
		return blockStateIdMap.getMappedObject(id);
	}

	@Override public int provideBlockStateId(BlockState blockState)
	{
		return blockStateIdMap.provideMappedId(blockState);
	}

	@Override public @Nullable MocapExtension getExtension(byte idFromRecording)
	{
		return extensionById.get(Byte.toUnsignedInt(idFromRecording));
	}

	@Override public @Nullable Byte getExtensionId(MocapExtension extension)
	{
		return extensionToId.get(extension);
	}

	public static abstract class RefIdMap<T>
	{
		protected final RecordingData parent;
		protected final Reference2IntMap<T> refToId = new Reference2IntOpenHashMap<>();
		protected final List<T> idToRef = new ArrayList<>();

		public RefIdMap(RecordingData parent)
		{
			this.parent = parent;
			init();
		}

		public int size()
		{
			return idToRef.size() - 1;
		}

		protected int provideMappedId(T ref)
		{
			int id = refToId.getOrDefault(ref, -1);
			return id != -1 ? id : put(ref);
		}

		protected T getMappedObject(int id)
		{
			return idToRef.get(id);
		}

		protected int put(T ref)
		{
			int pos = idToRef.size();
			idToRef.add(ref);
			refToId.put(ref, pos);
			return pos;
		}

		protected String resLocToStr(ResourceLocation resLoc)
		{
			//TODO: test with mods
			return resLoc.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
					? resLoc.getPath()
					: resLoc.toString();
		}

		protected abstract void init();
		public abstract int provideId(T ref);
		public abstract T getObject(int id);
		protected abstract void save(MocapAction.Writer writer);
		protected abstract void load(MocapAction.Reader reader);
	}

	public static class ItemIdMap extends RefIdMap<Item>
	{
		public ItemIdMap(RecordingData parent) { super(parent); }

		@Override protected void init() { put(Items.AIR); }

		@Override public int provideId(Item item)
		{
			return parent.usesIdMaps ? provideMappedId(item) : Item.getId(item);
		}

		@Override public Item getObject(int id)
		{
			return parent.usesIdMaps ? getMappedObject(id) : Item.byId(id);
		}

		@Override protected void save(MocapAction.Writer writer)
		{
			writer.addInt(size());
			idToRef.subList(1, idToRef.size()).forEach((item) -> writer.addString(resLocToStr(BuiltInRegistries.ITEM.getKey(item))));
		}

		@Override protected void load(MocapAction.Reader reader)
		{
			int size = reader.readInt();

			for (int i = 1; i <= size; i++)
			{
				Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.get(ResourceLocation.parse(reader.readString()));
				Item item = itemHolder.get().value();
				refToId.put(item, i);
				idToRef.add(item);
			}
		}
	}

	public static class BlockStateIdMap extends RefIdMap<BlockState>
	{
		public BlockStateIdMap(RecordingData parent) { super(parent); }

		@Override protected void init() { put(Blocks.AIR.defaultBlockState()); }

		@Override public int provideId(BlockState blockState)
		{
			return parent.usesIdMaps ? provideMappedId(blockState) : Block.getId(blockState);
		}

		@Override public BlockState getObject(int id)
		{
			return parent.usesIdMaps ? getMappedObject(id) : Block.stateById(id);
		}

		@Override protected void save(MocapAction.Writer writer)
		{
			writer.addInt(size());

			for (BlockState blockState : idToRef.subList(1, idToRef.size()))
			{
				Collection<Property<?>> properties = blockState.getProperties();
				if (properties.size() > Short.MAX_VALUE)
				{
					MocapMod.LOGGER.warn("BlockState properties count limit reached ({})!", properties.size());
					properties = List.of();
				}

				writer.addString(resLocToStr(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));
				writer.addShort((short)properties.size());

				for (Property<?> property : properties)
				{
					writer.addString(property.getName());
					writer.addString(propertyValueToStr(blockState, property));
				}
			}
		}

		@Override protected void load(MocapAction.Reader reader)
		{
			int size = reader.readInt();

			for (int i = 1; i <= size; i++)
			{
				Optional<Holder.Reference<Block>> blockHolder = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(reader.readString()));
				Block block = blockHolder.get().value();
				StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
				BlockState blockState = block.defaultBlockState();

				short propertyCount = reader.readShort();
				for (int j = 0; j < propertyCount; j++)
				{
					Property<?> property = stateDefinition.getProperty(reader.readString());
					blockState = updateBlockState(blockState, property, reader.readString());
				}

				refToId.put(blockState, i);
				idToRef.add(blockState);
			}
		}

		private static <T extends Comparable<T>> String propertyValueToStr(BlockState blockState, Property<T> property)
		{
			return property.getName(blockState.getValue(property));
		}

		private static <T extends Comparable<T>> BlockState updateBlockState(BlockState blockState, @Nullable Property<T> property, String str)
		{
			if (property == null) { return blockState; }
			Optional<T> value = property.getValue(str);
			return value.map((val) -> blockState.setValue(property, val)).orElse(blockState);
		}
	}
}
