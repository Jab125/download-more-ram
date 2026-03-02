package dev.jab125.drm.mixin;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
	@Overwrite
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
