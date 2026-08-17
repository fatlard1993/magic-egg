package justfatlard.magic_egg.quest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import justfatlard.village_quests.quest.VillagerQuest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Stage two: go and see what it does.
 *
 * <p>Knowing a recipe is not knowing a thing. The first stage ends with a
 * player who can craft a magic egg and has no particular reason to throw one,
 * and this is the reason: somebody wants to hear what happened.
 *
 * <p>Completion is the capture itself, recorded by the projectile, so there is
 * nothing to hand over and nothing to fake. You either watched a cow become the
 * idea of a cow or you did not.
 */
public class TryMagicEggQuest extends VillagerQuest {

	public TryMagicEggQuest(String requesterName, UUID villagerUuid) {
		super(VillagerQuest.QuestType.MYSTERY, requesterName, villagerUuid, 6);
	}

	@Override
	public String getDescription() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String[] lines = {
			this.requesterName + ": \"So you can make one now. I have never seen one used and I would very much like to. "
				+ "Throw it at something and come back and tell me.\"",
			this.requesterName + ": \"You have the working. I have only ever had the words. "
				+ "Use one, on anything, and then come and describe it to me.\"",
			this.requesterName + ": \"They say the animal simply becomes the idea of itself. "
				+ "I would like to know whether that is true or whether my teacher was being poetic.\""
		};
		return lines[rng.nextInt(lines.length)];
	}

	@Override
	public String getObjective() {
		return "throw a magic egg at something living, then tell " + this.requesterName + " about it";
	}

	@Override
	public boolean checkCompletion(ServerPlayer player) {
		return player.level() instanceof ServerLevel world
			&& EggLore.hasCaught(world, player.getUUID());
	}

	@Override
	public void onComplete(ServerPlayer player) {
		// Nothing to take and nothing to undo. The catch already happened.
	}
}
