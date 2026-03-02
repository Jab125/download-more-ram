package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.jab125.drm.Indexables;
import dev.jab125.drm.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/network/PacketProcessor$ListenerAndPacket")
public class ListenerAndPacketMixin {
	@WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
	<T extends PacketListener> void  handle(Packet<T> instance, T t, Operation<Void> original) {
		if (t instanceof Indexables f) {
			int index = f.getIndex();
			if (index == -1) {
				original.call(instance, t);
				return;
			}
			int localPlayerId = ((MinecraftExtension) Minecraft.getInstance()).getLocalPlayerId();
			//System.out.println(index + " handling " + instance);
			((MinecraftExtension) Minecraft.getInstance()).setLocalPlayerId(index);
			original.call(instance, t);
			((MinecraftExtension) Minecraft.getInstance()).setLocalPlayerId(localPlayerId);
		} else {
			original.call(instance, t);
		}
	}
}
