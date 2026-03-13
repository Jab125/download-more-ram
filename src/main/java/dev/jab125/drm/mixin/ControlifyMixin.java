package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.gui.guide.InGameButtonGuide;
import dev.isxander.controlify.ingame.ControllerPlayerMovement;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.jab125.drm.MinecraftExtension;
import dev.jab125.drm.TemporarySwitcher;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(Controlify.class)
public class ControlifyMixin {
	@Shadow
	private Minecraft minecraft;

	@Shadow
	private @Nullable InGameInputHandler inGameInputHandler;

	@Shadow
	@Nullable
	public InGameButtonGuide inGameButtonGuide;

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
	<T> void tick(Optional instance, Consumer<? super T> action, Operation<Void> original, @Local(argsOnly = true) Minecraft client) {
		if (client.getConnection() != null)try (var _ = new TemporarySwitcher()) {
			MinecraftExtension minecraft1 = (MinecraftExtension) minecraft;
			for (int i = 0; i < 4; i++) {
				if (minecraft1.setLocalPlayerId(i)) {
					original.call(instance, action);
				}
			}
		} else original.call(instance, action);
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void setupForController(@Nullable ControllerEntity controller) {
		ControllerPlayerMovement.updatePlayerInput(minecraft.player);

		if (controller == null) {
			this.inGameInputHandler = null;
			this.inGameButtonGuide = null;
			return;
		}

		this.inGameInputHandler = new InGameInputHandler(controller);
		this.inGameButtonGuide = new InGameButtonGuide(controller, this.minecraft);

		controller.input().ifPresent(input -> {
			input.rawStateNow().clearState();
			input.rawStateThen().clearState();
		});
	}
}
