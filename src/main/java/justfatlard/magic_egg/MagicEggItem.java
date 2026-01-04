package justfatlard.magic_egg;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class MagicEggItem extends Item implements PolymerItem {
	private final Identifier modelId;

	public MagicEggItem(Settings settings) {
		super(settings);
		this.modelId = Identifier.of(Main.MOD_ID, "magic_egg");
	}

	@Override
	public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
		// Appear as ender pearl to vanilla clients (has similar magical feel)
		return Items.ENDER_PEARL;
	}

	@Override
	public Identifier getPolymerItemModel(ItemStack itemStack, PacketContext context) {
		return this.modelId;
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);

		world.playSound(
			null,
			user.getX(),
			user.getY(),
			user.getZ(),
			SoundEvents.ENTITY_ENDER_PEARL_THROW,
			SoundCategory.NEUTRAL,
			0.5F,
			0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
		);

		if (!world.isClient()) {
			MagicEggEntity magicEggEntity = new MagicEggEntity(world, user);
			magicEggEntity.setItem(itemStack);
			magicEggEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
			world.spawnEntity(magicEggEntity);
		}

		user.incrementStat(Stats.USED.getOrCreateStat(this));

		itemStack.decrementUnlessCreative(1, user);

		return ActionResult.SUCCESS;
	}
}
