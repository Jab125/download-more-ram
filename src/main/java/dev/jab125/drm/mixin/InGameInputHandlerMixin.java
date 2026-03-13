package dev.jab125.drm.mixin;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.ingame.InGameInputHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InGameInputHandler.class)
public class InGameInputHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private Controlify controlify;
@Overwrite
	private boolean canProcessLookInput() {
//		boolean mouseNotGrabbed = !minecraft.mouseHandler.isMouseGrabbed() && !controlify.config().getSettings().globalSettings().outOfFocusInput;
//		boolean outOfFocus = !minecraft.isWindowActive() && !controlify.config().getSettings().globalSettings().outOfFocusInput;
		boolean screenVisible = minecraft.screen != null;
		boolean playerExists = minecraft.player != null;

		return !screenVisible && playerExists;
	}

}
