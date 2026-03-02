package dev.jab125.drm.mixin;

import com.mojang.authlib.GameProfile;
import dev.jab125.drm.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	void s(ClientLevel level, GameProfile gameProfile, CallbackInfo ci) {
//		var d=  ((MinecraftExtension) Minecraft.getInstance());
//		System.out.println(Arrays.toString(d.getLocalPlayers()));
//		System.out.println(d.getLocalPlayerId());
//		System.out.println(Minecraft.getInstance().getCameraEntity());
//		if (!gameProfile.name().endsWith("12")) {
//			try {
//				throw new RuntimeException("????");
//			}catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
	}
}
