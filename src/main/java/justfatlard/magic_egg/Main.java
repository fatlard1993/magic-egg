package justfatlard.magic_egg;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
	public static final String MOD_ID = "magic-egg-justfatlard";

	public static final Identifier MAGIC_EGG_ID = Identifier.of(MOD_ID, "magic_egg");

	public static final RegistryKey<Item> MAGIC_EGG_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, MAGIC_EGG_ID);

	public static final RegistryKey<EntityType<?>> MAGIC_EGG_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, MAGIC_EGG_ID);

	public static final EntityType<MagicEggEntity> MAGIC_EGG_ENTITY_TYPE = EntityType.Builder
		.<MagicEggEntity>create(MagicEggEntity::new, SpawnGroup.MISC)
		.dimensions(0.25F, 0.25F)
		.maxTrackingRange(4)
		.trackingTickInterval(10)
		.build(MAGIC_EGG_ENTITY_KEY);

	public static final MagicEggItem MAGIC_EGG_ITEM = new MagicEggItem(
		new Item.Settings()
			.registryKey(MAGIC_EGG_ITEM_KEY)
			.maxCount(16)
	);

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENTITY_TYPE, MAGIC_EGG_ID, MAGIC_EGG_ENTITY_TYPE);
		Registry.register(Registries.ITEM, MAGIC_EGG_ID, MAGIC_EGG_ITEM);

		// Add to spawn eggs creative tab
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
			content.add(MAGIC_EGG_ITEM);
		});

		System.out.println("[magic-egg] Loaded Magic Egg mod");
	}
}
