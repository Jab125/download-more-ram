package dev.jab125.drm.mixin;

import dev.jab125.drm.Drm;
import dev.jab125.drm.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiplayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "destroyBlock", at = @At("HEAD"))
	void start(CallbackInfoReturnable<Boolean> cir) {
		if (minecraft instanceof MinecraftExtension extension && extension.getLocalPlayerId() == 1) {
			Drm.houston2();
		}

	}
}
