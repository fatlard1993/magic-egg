package justfatlard.magic_egg;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.stream.Collectors;

public class MagicEggEntity extends ThrowableItemProjectile {
	private static final List<ResourceKey<ChickenVariant>> CHICKEN_VARIANTS = List.of(
		ChickenVariants.TEMPERATE,
		ChickenVariants.WARM,
		ChickenVariants.COLD
	);

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
				hitEntity.spawnAtLocation(serverWorld, spawnEggStack);
				hitEntity.discard();

				if (this.getOwner() instanceof ServerPlayer player) {
					Main.MOB_CAPTURE_CRITERION.trigger(player);
				}
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);

		Level world = this.level();
		if (world instanceof ServerLevel serverWorld) {
			spawnParticles(8);

			// If we missed (hit a block, not an entity), small chance to spawn something
			if (hitResult instanceof BlockHitResult) {
				// 1/4 chance to spawn something (higher than regular egg)
				if (this.random.nextInt(4) == 0) {
					int roll = this.random.nextInt(8);
					if (roll == 0) {
						// 1/8 chance for a random mob
						spawnRandomMob(serverWorld);
					} else if (roll == 1) {
						// 1/8 chance for a fire chicken
						spawnChicken(serverWorld, true);
					} else {
						// 6/8 chance for a normal chicken
						spawnChicken(serverWorld, false);
					}
				}
			}

			this.discard();
		}
	}

	private void spawnChicken(ServerLevel serverWorld, boolean onFire) {
		Chicken chicken = EntityTypes.CHICKEN.create(serverWorld, EntitySpawnReason.TRIGGERED);
		if (chicken != null) {
			chicken.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);

			// Random chicken variant (temperate/warm/cold)
			var variantKey = CHICKEN_VARIANTS.get(this.random.nextInt(CHICKEN_VARIANTS.size()));
			serverWorld.registryAccess()
				.lookupOrThrow(Registries.CHICKEN_VARIANT)
				.get(variantKey)
				.ifPresent(chicken::setVariant);

			if (onFire) {
				// ~5 seconds of fire - enough to kill the chicken
				chicken.igniteForTicks(100);
				if (this.getOwner() instanceof ServerPlayer player) {
					Main.LAVA_CHICKEN_CRITERION.trigger(player);
				}
			}
			serverWorld.addFreshEntity(chicken);
		}
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
