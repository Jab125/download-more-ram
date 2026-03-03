package dev.jab125.drm.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
	@Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V", at = @At("RETURN"))
	void render(final GuiGraphics graphics, final int width, final float alpha, final int heightOffset, CallbackInfo ci) {
		int logoHeight = 44 + 14;
		int logoWidth = LogoRenderer.LOGO_WIDTH;
		Font font = Minecraft.getInstance().font;
		int width1 = font.width("67");
		graphics.pose().pushMatrix();
		graphics.pose().translate(width / 2 - 128, heightOffset);
		graphics.pose().scale((float) logoWidth / width1, (float) logoHeight / font.lineHeight);
		graphics.drawString(font, "67", 0, 0, ARGB.color(alpha, 0xffffff));
		graphics.pose().popMatrix();
	}
}
