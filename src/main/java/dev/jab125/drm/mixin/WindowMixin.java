package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import dev.jab125.drm.Drm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Window.class)
public class WindowMixin {
	@Shadow
	private int framebufferWidth;

	/**
	 * @author
	 * @reason
	 */
	@WrapMethod(method = {"getWidth", "getScreenWidth", "getGuiScaledWidth"})
	public int getWidth(Operation<Integer> original) {
		return (int) (original.call() * Drm.d);
	}

	@WrapMethod(method = {"getHeight", "getScreenWidth", "getGuiScaledHeight"})
	public int getHeight(Operation<Integer> original) {
		return (int) (original.call() * Drm.d2);
	}
}
