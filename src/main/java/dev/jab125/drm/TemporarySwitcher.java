package dev.jab125.drm;

import net.minecraft.client.Minecraft;

public class TemporarySwitcher implements AutoCloseable {

	private final int localPlayerId;

	public TemporarySwitcher() {
		localPlayerId = ((MinecraftExtension) Minecraft.getInstance()).getLocalPlayerId();
	}
	@Override
	public void close() {
		((MinecraftExtension) Minecraft.getInstance()).setLocalPlayerId(localPlayerId);
	}
}
