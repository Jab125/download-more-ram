package dev.jab125.drm.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DrmClient implements ClientModInitializer {
	public static final SplashRenderer SIX_SEVEN = new SplashRenderer(Component.literal("Siiiix seven!").setStyle(Style.EMPTY.withColor(-256)));
	@Override
	public void onInitializeClient() {

	}
}
