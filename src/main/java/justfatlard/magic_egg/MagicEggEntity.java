package justfatlard.magic_egg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class MagicEggEntity extends ThrownItemEntity {
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

	private void spawnParticles(int count) {
		ItemStack itemStack = this.getStack();
		World world = this.getEntityWorld();

		if (world instanceof ServerWorld serverWorld) {
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
			// This method is called on the client but we don't need client-side particles
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
			this.discard();
		}
	}
}
