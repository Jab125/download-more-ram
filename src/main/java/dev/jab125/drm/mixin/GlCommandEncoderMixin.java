package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import dev.jab125.drm.Drm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(targets = "com/mojang/blaze3d/opengl/GlCommandEncoder")
public class GlCommandEncoderMixin {
	@WrapOperation(method = "presentTexture" ,at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_viewport(IIII)V"))
	void present(int x, int y, int width, int height, Operation<Void> original) {
//		int q;
//		if ((q = Drm.x) != -1) {
//			//	Drm.x = -1;
//			x = q;
//		}
		original.call(x, y, width, height);
	}

	@WrapOperation(method = "presentTexture" ,at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"))
	void y(DirectStateAccess instance, int source, int dest, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter, Operation<Void> original) {
		int q;
		if ((q = Drm.x) != -1) {
				Drm.x = -1;
			dstX0 = q;
			dstX1 += q;
		}
		original.call(instance, source, dest, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}
}
