package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jab125.drm.MinecraftExtension;
import dev.jab125.drm.TemporarySwitcher;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapMethod(method = "keyPress")
	void k(long handle, int action, KeyEvent event, Operation<Void> original) {
		if (minecraft.getConnection() == null) {
			original.call(handle, action, event);
			return;
		}
		try (var _ = new TemporarySwitcher()) {
		//	for (int i = 0; i < ((MinecraftExtension) minecraft).getLocalPlayers().length; i++) {
				if (((MinecraftExtension) minecraft).setLocalPlayerId(1)) {
					original.call(handle, action, event);
				}
		//	}
		}
	}
}
