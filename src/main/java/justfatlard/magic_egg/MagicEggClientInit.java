package justfatlard.magic_egg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@Environment(EnvType.CLIENT)
public class MagicEggClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(Main.MAGIC_EGG_ENTITY_TYPE, ThrownItemRenderer::new);
	}
}
