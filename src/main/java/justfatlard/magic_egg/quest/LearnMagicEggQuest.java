package justfatlard.magic_egg.quest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import justfatlard.village_quests.quest.VillagerQuest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Stage one: somebody tells you what goes in it.
 *
 * <p>The recipe is four lapis, four ender pearls and an egg, and there is no way
 * to arrive at that by experiment. So a cleric describes it and asks you to
 * bring the pieces, which is the same errand as any fetch quest except that the
 * reward is knowing what they are for.
 *
 * <p>Nothing is taken. You gathered exactly one magic egg's worth of materials
 * and the payoff is being able to make one immediately, so confiscating them at
 * the door would be a joke at the player's expense.
 */
public class LearnMagicEggQuest extends VillagerQuest {
	private static final int LAPIS = 4;
	private static final int PEARLS = 4;

	private static final ResourceKey<Recipe<?>> RECIPE = ResourceKey.create(
		Registries.RECIPE, Identifier.fromNamespaceAndPath("magic-egg-justfatlard", "magic_egg"));

	public LearnMagicEggQuest(String requesterName, UUID villagerUuid) {
		super(VillagerQuest.QuestType.CREATION, requesterName, villagerUuid, 8);
	}

	@Override
	public String getDescription() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String[] lines = {
			this.requesterName + ": \"There's a working I was taught and never had cause to use. "
				+ "Lapis at the corners, pearls between them, an egg in the middle. Bring me those and I'll show you the rest.\"",
			this.requesterName + ": \"Four lapis, four ender pearls, one egg. That is the whole of it, "
				+ "and knowing the order matters more than you would think. Bring them and I will show you the order.\"",
			this.requesterName + ": \"My teacher called it an unmaking. Lapis, pearls, and an egg. "
				+ "Bring them here and I will not send you away not knowing.\""
		};
		return lines[rng.nextInt(lines.length)];
	}

	@Override
	public String getObjective() {
		return "bring " + this.requesterName + " " + LAPIS + " lapis lazuli, " + PEARLS
			+ " ender pearls and an egg - pearls come off endermen, after dark";
	}

	@Override
	public boolean checkCompletion(ServerPlayer player) {
		return count(player, Items.LAPIS_LAZULI) >= LAPIS
			&& count(player, Items.ENDER_PEARL) >= PEARLS
			&& count(player, Items.EGG) >= 1;
	}

	@Override
	public void onComplete(ServerPlayer player) {
		// Shown, not surrendered: the materials stay, so the first magic egg can be
		// made on the walk home.
		player.awardRecipesByKey(java.util.List.of(RECIPE));

		if (player.level() instanceof ServerLevel world) {
			EggLore.teach(world, player.getUUID());
		}
	}

	private static int count(ServerPlayer player, Item item) {
		int found = 0;
		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (stack.is(item)) found += stack.getCount();
		}
		return found;
	}
}
