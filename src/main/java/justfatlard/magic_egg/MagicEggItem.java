package justfatlard.magic_egg;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicEggItem extends Item {

	public MagicEggItem(Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);

		world.playSound(
			null,
			user.getX(),
			user.getY(),
			user.getZ(),
			SoundEvents.ENDER_PEARL_THROW,
			SoundSource.NEUTRAL,
			0.5F,
			0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
		);

		if (!world.isClientSide()) {
			MagicEggEntity magicEggEntity = new MagicEggEntity(world, user, itemStack);
			magicEggEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
			world.addFreshEntity(magicEggEntity);
		}

		user.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, user);

		return InteractionResult.SUCCESS;
	}
}
