package dev.jab125.drm.mixin;

import dev.jab125.drm.client.DrmClient;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SplashManager.class, priority = 94000)
public class SplashManagerMixin {
	@Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
	void getR(CallbackInfoReturnable<SplashRenderer> cir) {
		cir.setReturnValue(DrmClient.SIX_SEVEN);
	}
}
