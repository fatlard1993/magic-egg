package justfatlard.magic_egg.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Who has been shown the recipe, and who has since thrown one.
 *
 * <p>Two facts, because the lesson is in two halves: a villager can tell you
 * what goes in a magic egg, and that is a different thing from having watched
 * one work. The second quest only exists for someone who has been told and has
 * not yet seen.
 *
 * <p>Persisted, unlike the quests themselves. Being taught something is not
 * undone by a server restart, and offering to teach it again would make the
 * village look like it had forgotten the afternoon.
 */
public final class EggLore {
	private EggLore() {}

	private static final SavedDataType<EggLore.Data> TYPE = new SavedDataType<>(
		Identifier.parse("magic_egg_lore"), EggLore.Data::new, EggLore.Data.CODEC, DataFixTypes.LEVEL);

	private static EggLore.Data data(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public static boolean knowsRecipe(ServerLevel level, UUID player) {
		return data(level).taught.contains(player);
	}

	public static void teach(ServerLevel level, UUID player) {
		if (data(level).taught.add(player)) data(level).setDirty();
	}

	public static boolean hasCaught(ServerLevel level, UUID player) {
		return data(level).caught.contains(player);
	}

	/** Called from the projectile when a capture actually lands. */
	public static void recordCatch(ServerLevel level, UUID player) {
		if (data(level).caught.add(player)) data(level).setDirty();
	}

	private static class Data extends SavedData {
		public static final Codec<EggLore.Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.listOf().fieldOf("taught").forGetter(d -> List.copyOf(d.taught)),
			UUIDUtil.CODEC.listOf().fieldOf("caught").forGetter(d -> List.copyOf(d.caught))
		).apply(instance, EggLore.Data::of));

		private final Set<UUID> taught = new HashSet<>();
		private final Set<UUID> caught = new HashSet<>();

		private static EggLore.Data of(List<UUID> taught, List<UUID> caught) {
			EggLore.Data data = new EggLore.Data();
			data.taught.addAll(taught);
			data.caught.addAll(caught);
			return data;
		}
	}
}
