package justfatlard.magic_egg.integration;

import justfatlard.magic_egg.quest.EggLore;
import justfatlard.magic_egg.quest.LearnMagicEggQuest;
import justfatlard.magic_egg.quest.TryMagicEggQuest;
import justfatlard.village_quests.api.QuestRegistry;
import justfatlard.village_quests.quest.VillagerQuest;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Offers the magic egg in two halves, from the profession that would know.
 *
 * <p>This class names village-quests types directly and must only be loaded
 * behind the isModLoaded guard in Main, the same shape poopsmith uses.
 *
 * <p>The second stage is only offered to someone already taught and not yet
 * shown, so the pair reads as one conversation continued rather than two
 * unrelated errands that happen to involve the same object.
 */
public final class EggQuestRegistration {
	private EggQuestRegistration() {}

	private static final float OFFER_CHANCE = 0.12F;

	/** Nobody teaches a working to somebody they have just met. */
	private static final int MIN_REPUTATION = 20;

	public static void register() {
		QuestRegistry.registerProfessionQuest("cleric", EggQuestRegistration::offer);
	}

	private static VillagerQuest offer(Villager villager, String villagerName, int reputation, Random random) {
		if (!(villager.level() instanceof ServerLevel world)) return null;
		if (reputation < MIN_REPUTATION) return null;
		if (random.nextFloat() > OFFER_CHANCE) return null;

		// A quest generator has no player in hand, so the stage is chosen from the
		// nearest one: it is their lore that decides which half they are owed.
		if (!(world.getNearestPlayer(villager, 16.0) instanceof net.minecraft.server.level.ServerPlayer nearby)) {
			return null;
		}

		if (!EggLore.knowsRecipe(world, nearby.getUUID())) {
			return new LearnMagicEggQuest(villagerName, villager.getUUID());
		}
		if (!EggLore.hasCaught(world, nearby.getUUID())) {
			return new TryMagicEggQuest(villagerName, villager.getUUID());
		}
		return null;
	}
}
