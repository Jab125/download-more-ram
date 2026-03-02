package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.jab125.drm.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapMethod(method = "onButton")
	void inbuytON(long handle, MouseButtonInfo rawButtonInfo, int action, Operation<Void> original) {
		if (minecraft.getConnection() == null) {
			original.call(handle, rawButtonInfo, action);
			return;
		}
		for (int i = 0; i < ((MinecraftExtension) minecraft).getLocalPlayers().length; i++) {
			if (((MinecraftExtension) minecraft).setLocalPlayerId(i)) {
				original.call(handle, rawButtonInfo, action);
			}
		}
	}
}
