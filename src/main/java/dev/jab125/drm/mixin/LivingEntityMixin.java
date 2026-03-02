package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.jab125.drm.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityMixin {
	@WrapOperation(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
	Entity S(Minecraft instance, Operation<Entity> original, @Local(argsOnly = true)LivingEntity livingEntity) {
		Entity call = original.call(instance);
		if (livingEntity instanceof Player player) {
//			System.out.println("" + livingEntity + ((MinecraftExtension) instance).getLocalPlayerId());
//			System.out.println(instance.getCameraEntity() + ", " + livingEntity);
//			if (((MinecraftExtension) instance).getLocalPlayerId() == 0 && livingEntity != instance.getCameraEntity()) {
//				System.out.println("WHAT WHAT WHAT WHAT");
//			}

		}
//		if (livingEntity instanceof Player) {
//			System.out.println("" + call + livingEntity + ((MinecraftExtension) Minecraft.getInstance()).getLocalPlayerId() + (Minecraft.getInstance().player == livingEntity));
//			try {
//				throw new RuntimeException("WHY");
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
		return call;
	}

}
