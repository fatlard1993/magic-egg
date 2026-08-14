package justfatlard.magic_egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class MobCaptureCriterion extends SimpleCriterionTrigger<MobCaptureCriterion.Conditions> {
	@Override
	public Codec<Conditions> codec() {
		return Conditions.CODEC;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, conditions -> true);
	}

	public record Conditions(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
			).apply(instance, Conditions::new)
		);

		public static Conditions create() {
			return new Conditions(Optional.empty());
		}
	}
}
