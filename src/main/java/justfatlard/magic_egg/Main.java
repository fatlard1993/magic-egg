package justfatlard.magic_egg;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
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

	public static final MobCaptureCriterion MOB_CAPTURE_CRITERION = Criteria.register(
		MOD_ID + "/mob_capture",
		new MobCaptureCriterion()
	);

	public static final MobCaptureCriterion LAVA_CHICKEN_CRITERION = Criteria.register(
		MOD_ID + "/lava_chicken",
		new MobCaptureCriterion()
	);

	@Override
	public void onInitialize() {
		// Register mod assets with Polymer resource pack system
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		// Register entity and item
		Registry.register(Registries.ENTITY_TYPE, MAGIC_EGG_ID, MAGIC_EGG_ENTITY_TYPE);
		Registry.register(Registries.ITEM, MAGIC_EGG_ID, MAGIC_EGG_ITEM);

		// Register entity type with Polymer for vanilla client compatibility
		PolymerEntityUtils.registerType(MAGIC_EGG_ENTITY_TYPE);

		// Create Polymer item group (access via /polymer creative)
		ItemGroup magicEggGroup = PolymerItemGroupUtils.builder()
			.displayName(Text.literal("Magic Egg"))
			.icon(() -> new ItemStack(MAGIC_EGG_ITEM))
			.entries((context, entries) -> {
				entries.add(new ItemStack(MAGIC_EGG_ITEM));
			})
			.build();
		PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "magic_egg"), magicEggGroup);

		System.out.println("[magic-egg] Loaded Magic Egg mod (server-side with Polymer)");
	}
}
