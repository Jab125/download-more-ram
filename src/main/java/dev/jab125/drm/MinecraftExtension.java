package dev.jab125.drm;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;

public interface MinecraftExtension {
	LocalPlayer[] getLocalPlayers();

	DisplaySection[] getDisplaySections();

	MultiPlayerGameMode[] getLocalGameModes();

	int getLocalPlayerId();

	boolean setLocalPlayerId(int id);
}
