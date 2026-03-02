package dev.jab125.drm.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor
	ClientLevel getLevel();

	@Accessor("level")
	void setdLevel(ClientLevel level);
}
