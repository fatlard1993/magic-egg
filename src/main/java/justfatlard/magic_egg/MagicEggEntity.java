package justfatlard.magic_egg;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class MagicEggEntity extends ThrowableItemProjectile {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	/** One missed throw in four never breaks at all, and can be picked up and thrown again. */
	private static final int INTACT_ODDS = 4;

	/** Of the ones that do break, one in four hatches something. */
	private static final int HATCH_ODDS = 4;

	/** As many chickens as a miss can produce. */
	private static final int FLOCK_MAX = 99;

	/**
	 * Facts about the mob that was removed, not about the one that comes back.
	 *
	 * <p>Position, heading and momentum would put the new mob back where the old
	 * one stood, however far away the egg is opened. The UUID is worse: the eggs
	 * stack, so two mobs placed from the same stack would claim one identity and
	 * the second would be thrown out. Passengers are written by the save and
	 * never read back by the load, so they are weight in the component and
	 * nothing else. A leash points at a fence somebody has probably walked away
	 * from.
	 */
	private static final List<String> VOLATILE_KEYS =
		List.of("Pos", "Motion", "Rotation", "UUID", "Passengers", "Leash");

	public MagicEggEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
		super(entityType, world);
	}

	public MagicEggEntity(Level world, LivingEntity owner, ItemStack stack) {
		super(Main.MAGIC_EGG_ENTITY_TYPE, owner, world, stack);
	}

	public MagicEggEntity(Level world, double x, double y, double z, ItemStack stack) {
		super(Main.MAGIC_EGG_ENTITY_TYPE, x, y, z, world, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Main.MAGIC_EGG_ITEM;
	}

	/**
	 * Which of the two eggs is in flight.
	 *
	 * <p>The thrown stack is synced to the client already, for the renderer, so
	 * asking it is cheaper than a second entity type and cannot drift out of step
	 * with what the player sees flying.
	 */
	private boolean preservesIdentity() {
		return this.getItem().is(Main.ENDER_EGG_ITEM);
	}

	private void spawnParticles(int count) {
		ItemStack itemStack = this.getItem();

		if (this.level() instanceof ServerLevel serverWorld) {
			for (int i = 0; i < count; i++) {
				serverWorld.sendParticles(
					new ItemParticleOption(ParticleTypes.ITEM, itemStack.getItem()),
					this.getX(),
					this.getY(),
					this.getZ(),
					1,
					((double) this.random.nextFloat() - 0.5) * 0.08,
					((double) this.random.nextFloat() - 0.5) * 0.08,
					((double) this.random.nextFloat() - 0.5) * 0.08,
					0.0
				);
			}
		}
	}

	@Override
	public void handleEntityEvent(byte status) {
		if (status == 3) {
			// Server-side particles are handled in onHit via spawnParticles
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		Entity hitEntity = entityHitResult.getEntity();

		// Don't convert players or other magic egg projectiles
		if (hitEntity instanceof Player || hitEntity instanceof MagicEggEntity) {
			return;
		}

		super.onHitEntity(entityHitResult);

		Level world = this.level();
		if (world instanceof ServerLevel serverWorld) {
			EntityType<?> entityType = hitEntity.getType();

			var spawnEggHolder = SpawnEggItem.byId(entityType);

			if (spawnEggHolder.isPresent()) {
				ItemStack spawnEggStack = new ItemStack(spawnEggHolder.get());

				// Read the mob before it is discarded, never after.
				if (this.preservesIdentity()) {
					preserveIdentity(serverWorld, hitEntity, spawnEggStack);
				}

				hitEntity.spawnAtLocation(serverWorld, spawnEggStack);
				hitEntity.discard();

				if (this.getOwner() instanceof ServerPlayer player) {
					Main.MOB_CAPTURE_CRITERION.trigger(player);
					// Same moment, second audience: a villager asked to be told.
					justfatlard.magic_egg.quest.EggLore.recordCatch(serverWorld, player.getUUID());
				}
			}
		}
	}

	/**
	 * The whole difference between the two eggs, in one method.
	 *
	 * <p>A magic egg hands back the species: throw it at a named, saddled,
	 * particularly coloured horse and what drops is a horse spawn egg, which
	 * places a horse. An ender egg hands back the animal. The mob's own save data
	 * rides along in the spawn egg's entity data, so the variant it was born
	 * with, the name somebody painted on a nametag, the saddle, the tamed-by and
	 * everything else all come back when the egg is placed.
	 *
	 * <p>The name is also written onto the item, so the egg reads as that animal
	 * in the inventory rather than as a generic one.
	 */
	private void preserveIdentity(ServerLevel serverWorld, Entity captured, ItemStack spawnEgg) {
		try (ProblemReporter.ScopedCollector reporter =
				new ProblemReporter.ScopedCollector(captured.problemPath(), LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, serverWorld.registryAccess());
			captured.saveWithoutId(output);
			CompoundTag tag = output.buildResult();

			for (String key : VOLATILE_KEYS) {
				tag.remove(key);
			}

			spawnEgg.set(DataComponents.ENTITY_DATA, TypedEntityData.of(captured.getType(), tag));

			Component customName = captured.getCustomName();
			if (customName != null) {
				spawnEgg.set(DataComponents.CUSTOM_NAME, customName);
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);

		Level world = this.level();
		if (!(world instanceof ServerLevel serverWorld)) {
			return;
		}

		// A missed throw sometimes simply lands. Nothing broke, so nothing hatches
		// and nothing is lost: the egg is lying there to be picked up and thrown
		// again.
		if (hitResult instanceof BlockHitResult && this.random.nextInt(INTACT_ODDS) == 0) {
			ItemStack recovered = new ItemStack(this.getItem().getItem());
			serverWorld.addFreshEntity(new ItemEntity(
				serverWorld, this.getX(), this.getY(), this.getZ(), recovered, 0.0, 0.1, 0.0));
			this.discard();
			return;
		}

		spawnParticles(8);

		// If we missed (hit a block, not an entity), small chance to spawn something
		if (hitResult instanceof BlockHitResult) {
			if (this.random.nextInt(HATCH_ODDS) == 0) {
				int roll = this.random.nextInt(8);
				if (roll == 0) {
					// 1/8 chance for a random mob
					spawnRandomMob(serverWorld);
				} else if (roll == 1) {
					// 1/8 chance for a fire chicken
					spawnChicken(serverWorld, chickenVariants(serverWorld), true);
				} else {
					// 6/8 chance for chickens, however many that turns out to be
					spawnFlock(serverWorld);
				}
			}
		}

		this.discard();
	}

	/**
	 * A flock, not a chicken.
	 *
	 * <p>Mixed species, mixed ages, and anywhere from one bird to ninety-nine of
	 * them. The size is weighted hard toward the small end on purpose: every bird
	 * is a real entity the server has to tick from then on, so the ninety-nine
	 * chicken miss wants to be the story somebody tells and not the weekly
	 * average. Most flocks are a handful; roughly one in sixteen can run away
	 * with itself.
	 */
	private void spawnFlock(ServerLevel serverWorld) {
		int ceiling = switch (this.random.nextInt(16)) {
			case 0 -> FLOCK_MAX;
			case 1, 2 -> 24;
			default -> 5;
		};

		List<Holder.Reference<ChickenVariant>> variants = chickenVariants(serverWorld);
		int count = 1 + this.random.nextInt(ceiling);
		for (int i = 0; i < count; i++) {
			spawnChicken(serverWorld, variants, false);
		}
	}

	/** Every species the server knows about, datapacked ones included. */
	private static List<Holder.Reference<ChickenVariant>> chickenVariants(ServerLevel serverWorld) {
		return serverWorld.registryAccess()
			.lookupOrThrow(Registries.CHICKEN_VARIANT)
			.listElements()
			.toList();
	}

	private void spawnChicken(ServerLevel serverWorld, List<Holder.Reference<ChickenVariant>> variants, boolean onFire) {
		Chicken chicken = EntityTypes.CHICKEN.create(serverWorld, EntitySpawnReason.TRIGGERED);
		if (chicken == null) {
			return;
		}

		// Scattered a little, so a large flock arrives as a crowd rather than as
		// one column of chickens in a single block.
		chicken.snapTo(
			this.getX() + (this.random.nextDouble() - 0.5),
			this.getY(),
			this.getZ() + (this.random.nextDouble() - 0.5),
			this.random.nextFloat() * 360.0F,
			0.0F
		);

		if (!variants.isEmpty()) {
			chicken.setVariant(variants.get(this.random.nextInt(variants.size())));
		}

		// Anywhere from just hatched to fully grown, rolled per bird, so a flock
		// turns up as a family rather than as a batch.
		chicken.setAge(AgeableMob.BABY_START_AGE + this.random.nextInt(2 * -AgeableMob.BABY_START_AGE));

		if (onFire) {
			// ~5 seconds of fire - enough to kill the chicken
			chicken.igniteForTicks(100);
			if (this.getOwner() instanceof ServerPlayer player) {
				Main.LAVA_CHICKEN_CRITERION.trigger(player);
			}
		}

		serverWorld.addFreshEntity(chicken);
	}

	private void spawnRandomMob(ServerLevel serverWorld) {
		List<EntityType<?>> spawnableTypes = BuiltInRegistries.ENTITY_TYPE.stream()
			.filter(type -> SpawnEggItem.byId(type).isPresent())
			.collect(Collectors.toList());

		if (!spawnableTypes.isEmpty()) {
			EntityType<?> randomType = spawnableTypes.get(this.random.nextInt(spawnableTypes.size()));
			Entity entity = randomType.create(serverWorld, EntitySpawnReason.TRIGGERED);
			if (entity != null) {
				entity.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
				serverWorld.addFreshEntity(entity);
			}
		}
	}
}
