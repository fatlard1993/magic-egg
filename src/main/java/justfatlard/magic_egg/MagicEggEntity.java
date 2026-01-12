package justfatlard.magic_egg;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import xyz.nucleoid.packettweaker.PacketContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.entity.passive.ChickenVariants;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class MagicEggEntity extends ThrownItemEntity implements PolymerEntity {
	public MagicEggEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	public MagicEggEntity(World world, LivingEntity owner) {
		super(Main.MAGIC_EGG_ENTITY_TYPE, owner, world, new ItemStack(Main.MAGIC_EGG_ITEM));
	}

	public MagicEggEntity(World world, double x, double y, double z) {
		super(Main.MAGIC_EGG_ENTITY_TYPE, x, y, z, world, new ItemStack(Main.MAGIC_EGG_ITEM));
	}

	@Override
	protected Item getDefaultItem() {
		return Main.MAGIC_EGG_ITEM;
	}

	@Override
	public EntityType<?> getPolymerEntityType(PacketContext context) {
		// Appear as a thrown ender pearl to vanilla clients
		return EntityType.ENDER_PEARL;
	}

	private void spawnParticles(int count) {
		ItemStack itemStack = this.getStack();

		if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
			for (int i = 0; i < count; i++) {
				serverWorld.spawnParticles(
					new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack),
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
	public void handleStatus(byte status) {
		if (status == 3) {
			// Server-side particles are handled in onCollision via spawnParticles
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);

		Entity hitEntity = entityHitResult.getEntity();

		// Don't convert players or other magic egg projectiles
		if (hitEntity instanceof PlayerEntity || hitEntity instanceof MagicEggEntity) {
			return;
		}

		World world = this.getEntityWorld();
		if (world instanceof ServerWorld serverWorld) {
			EntityType<?> entityType = hitEntity.getType();

			// Try to get the spawn egg for this entity type
			SpawnEggItem spawnEggItem = SpawnEggItem.forEntity(entityType);

			if (spawnEggItem != null) {
				// Create the spawn egg item stack
				ItemStack spawnEggStack = new ItemStack(spawnEggItem);

				// Drop the spawn egg at the entity's location
				hitEntity.dropStack(serverWorld, spawnEggStack);

				// Remove the entity (discard it from the world)
				hitEntity.discard();

				// Grant advancement to the player who threw the egg
				if (this.getOwner() instanceof ServerPlayerEntity player) {
					Main.MOB_CAPTURE_CRITERION.trigger(player);
				}
			}
			// If no spawn egg exists for this entity, just do nothing special
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);

		World world = this.getEntityWorld();
		if (world instanceof ServerWorld serverWorld) {
			// Spawn particles on collision
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

	private static final net.minecraft.registry.RegistryKey<ChickenVariant>[] CHICKEN_VARIANTS = new net.minecraft.registry.RegistryKey[] {
		ChickenVariants.TEMPERATE,
		ChickenVariants.WARM,
		ChickenVariants.COLD
	};

	private void spawnChicken(ServerWorld serverWorld, boolean onFire) {
		ChickenEntity chicken = EntityType.CHICKEN.create(serverWorld, SpawnReason.TRIGGERED);
		if (chicken != null) {
			chicken.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);

			// Random chicken variant (temperate/warm/cold)
			var variantKey = CHICKEN_VARIANTS[this.random.nextInt(CHICKEN_VARIANTS.length)];
			serverWorld.getRegistryManager()
				.getOptional(RegistryKeys.CHICKEN_VARIANT)
				.flatMap(registry -> registry.getOptional(variantKey))
				.ifPresent(chicken::setVariant);

			if (onFire) {
				// ~5 seconds of fire - enough to kill the chicken
				chicken.setOnFireForTicks(100);
				// Grant achievement to the player who threw the egg
				if (this.getOwner() instanceof ServerPlayerEntity player) {
					Main.LAVA_CHICKEN_CRITERION.trigger(player);
				}
			}
			serverWorld.spawnEntity(chicken);
		}
	}

	private void spawnRandomMob(ServerWorld serverWorld) {
		// Get all entity types that have spawn eggs (meaning they're spawnable mobs)
		List<EntityType<?>> spawnableTypes = Registries.ENTITY_TYPE.stream()
			.filter(type -> SpawnEggItem.forEntity(type) != null)
			.collect(Collectors.toList());

		if (!spawnableTypes.isEmpty()) {
			EntityType<?> randomType = spawnableTypes.get(this.random.nextInt(spawnableTypes.size()));
			Entity entity = randomType.create(serverWorld, SpawnReason.TRIGGERED);
			if (entity != null) {
				entity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
				serverWorld.spawnEntity(entity);
			}
		}
	}
}
