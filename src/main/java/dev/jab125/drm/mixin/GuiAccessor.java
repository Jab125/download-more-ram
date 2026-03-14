package dev.jab125.drm.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.gui.Gui.class)
public interface GuiAccessor {
	@Mutable
	@Accessor
	void setChat(ChatComponent chat);
}
