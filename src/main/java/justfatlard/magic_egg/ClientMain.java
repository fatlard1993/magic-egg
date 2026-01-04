package justfatlard.magic_egg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ClientMain implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the entity renderer for the magic egg projectile
		EntityRendererRegistry.register(Main.MAGIC_EGG_ENTITY_TYPE, FlyingItemEntityRenderer::new);

		System.out.println("[magic-egg] Client initialized - entity renderer registered");
	}
}
