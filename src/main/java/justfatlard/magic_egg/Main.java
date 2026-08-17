package justfatlard.magic_egg;

import justfatlard.pandorical.api.ItemRegistration;
import justfatlard.pandorical.api.PandoricalApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MOD_ID = "magic-egg-justfatlard";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier MAGIC_EGG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "magic_egg");

	// These IDs must match the "trigger" fields in the advancement JSON files
	public static final String MOB_CAPTURE_TRIGGER_ID = MOD_ID + "/mob_capture";
	public static final String LAVA_CHICKEN_TRIGGER_ID = MOD_ID + "/lava_chicken";

	private static ResourceKey<Item> itemKeyOf(String name) {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static ResourceKey<EntityType<?>> entityKeyOf(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	public static final ResourceKey<Item> MAGIC_EGG_ITEM_KEY = itemKeyOf("magic_egg");

	public static final ResourceKey<EntityType<?>> MAGIC_EGG_ENTITY_KEY = entityKeyOf("magic_egg");

	public static final EntityType<MagicEggEntity> MAGIC_EGG_ENTITY_TYPE = EntityType.Builder
		.<MagicEggEntity>of(MagicEggEntity::new, MobCategory.MISC)
		.sized(0.25F, 0.25F)
		.clientTrackingRange(4)
		.updateInterval(10)
		.build(MAGIC_EGG_ENTITY_KEY);

	public static final MagicEggItem MAGIC_EGG_ITEM = new MagicEggItem(
		new Item.Properties()
			.setId(MAGIC_EGG_ITEM_KEY)
			.stacksTo(16)
	);

	public static final MobCaptureCriterion MOB_CAPTURE_CRITERION = Registry.register(
		BuiltInRegistries.TRIGGER_TYPES,
		Identifier.fromNamespaceAndPath(MOD_ID, "mob_capture"),
		new MobCaptureCriterion()
	);

	public static final MobCaptureCriterion LAVA_CHICKEN_CRITERION = Registry.register(
		BuiltInRegistries.TRIGGER_TYPES,
		Identifier.fromNamespaceAndPath(MOD_ID, "lava_chicken"),
		new MobCaptureCriterion()
	);

	// Creative mode tab
	public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(
		Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "magic_egg"));

	@Override
	public void onInitialize() {
		// Guarded class load: EggQuestRegistration names village-quests types
		// directly, so it must not be touched when that mod is absent.
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("village-quests-justfatlard")) {
			justfatlard.magic_egg.integration.EggQuestRegistration.register();
		}

		// Register with Pandorical if available
		if (PandoricalApi.isAvailable()) {
			PandoricalApi.content().registerItem(MOD_ID + ":magic_egg", new ItemRegistration()
				.model(MOD_ID + ":item/magic_egg")
				.maxStackSize(16));
			PandoricalApi.content().registerModAssets(MOD_ID);
		}

		// Register entity and item
		Registry.register(BuiltInRegistries.ENTITY_TYPE, MAGIC_EGG_ID, MAGIC_EGG_ENTITY_TYPE);
		PandoricalApi.registerEntityRenderer(MAGIC_EGG_ENTITY_TYPE, "thrown_item");
		Registry.register(BuiltInRegistries.ITEM, MAGIC_EGG_ID, MAGIC_EGG_ITEM);

		// Creative mode tab
		CreativeModeTab magicEggGroup = FabricCreativeModeTab.builder()
			.title(Component.literal("Magic Egg"))
			.icon(() -> new ItemStack(MAGIC_EGG_ITEM))
			.displayItems((context, entries) -> {
				entries.accept(new ItemStack(MAGIC_EGG_ITEM));
			})
			.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY, magicEggGroup);

		LOGGER.info("Loaded Magic Egg mod (server-side with Pandorical)");
	}
}
