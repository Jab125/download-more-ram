package dev.jab125.drm.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.GameRenderer.class)
public interface GameRendererAccessor {
	@Mutable
	@Accessor
	void setItemInHandRenderer(ItemInHandRenderer itemInHandRenderer);
}
