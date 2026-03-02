package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import dev.jab125.drm.DisplaySection;
import dev.jab125.drm.Drm;
import dev.jab125.drm.MinecraftExtension;
import dev.jab125.drm.TemporarySwitcher;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtension {
	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;
	@Shadow
	@Nullable
	public LocalPlayer player;
	@Shadow
	@Final
	public GameRenderer gameRenderer;
	@Shadow
	@Nullable
	public ClientLevel level;
	@Shadow
	@Final
	public ParticleEngine particleEngine;

	@Shadow
	public abstract void setCameraEntity(@Nullable Entity cameraEntity);

	@Shadow
	@Final
	private DeltaTracker.Timer deltaTracker;
	@Shadow
	@Final
	public DebugScreenEntryList debugEntries;
	@Shadow
	private MetricsRecorder metricsRecorder;
	@Shadow
	private double gpuUtilization;
	@Shadow
	private TimerQuery. @Nullable FrameProfile currentFrameProfile;

	@Shadow
	public abstract RenderTarget getMainRenderTarget();

	@Shadow
	public boolean noRender;
	@Shadow
	@Final
	private Window window;
	@Shadow
	private long frameTimeNs;
	@Shadow
	@Final
	private @Nullable TracyFrameCapture tracyFrameCapture;
	@Shadow
	@Final
	private FramerateLimitTracker framerateLimitTracker;
	@Shadow
	private int frames;
	@Shadow
	private long lastNanoTime;
	@Shadow
	private long savedCpuDuration;

	@Shadow
	public abstract DebugScreenOverlay getDebugOverlay();

	@Shadow
	private long lastTime;
	@Shadow
	private static int fps;
	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	@Nullable
	public abstract ClientPacketListener getConnection();

	@Shadow
	public abstract User getUser();

	@Shadow
	public abstract @Nullable Entity getCameraEntity();

	@Shadow
	protected abstract void updateLevelInEngines(@Nullable ClientLevel level, boolean stopSound);

	@Shadow
	@Final
	public LevelRenderer levelRenderer;
	private static final int MAX_COUNT = 4;
	private int localPlayerId = 0;
	private LocalPlayer[] localPlayers = new LocalPlayer[MAX_COUNT];
	private MultiPlayerGameMode[] localGameModes = new MultiPlayerGameMode[MAX_COUNT];
	private ItemInHandRenderer[] itemInHandRenderers = new ItemInHandRenderer[MAX_COUNT];
	private Screen[] localScreens = new Screen[MAX_COUNT];
	private DisplaySection[] displaySections = new DisplaySection[MAX_COUNT];

	@Inject(method = "setScreen", at = @At("HEAD"))
	void setScreen(Screen screen, CallbackInfo ci) {
		localScreens[localPlayerId] = screen;
		System.out.println("Setting screen to " + screen + " for side " + localPlayerId);
		try {
			throw new RuntimeException("screen set");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	@Override
	public LocalPlayer[] getLocalPlayers() {
		return localPlayers;
	}

	@Override
	public DisplaySection[] getDisplaySections() {
		return displaySections;
	}

	private boolean setupAlready = false;

	private boolean throwIfSetupAgain = false;
	private boolean firstTime = true;
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V"))
	void a(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
		for (int i = 0; i < localPlayers.length; i++) {
			localPlayers[i] = null;
			setCameraEntity(null);
			setupAlready = false;

		}
		if (firstTime) {
			firstTime = false;
		} else {
			//throwIfSetupAgain = true;
		}
	}

	@Inject(method = "doWorldLoad", at = @At("RETURN"))
	void doWorldLoad(LevelStorageSource.LevelStorageAccess levelSourceAccess, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, boolean newWorld, CallbackInfo ci) {

	}
	void peakRagebait() {
		setLocalPlayerId(1);
		Instant worldLoadStart = Instant.now();
		IntegratedServer singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
		Duration worldLoadDuration = Duration.between(worldLoadStart, Instant.now());
		SocketAddress socketAddress = singleplayerServer.getConnection().startMemoryChannel();
		Connection connection = Connection.connectToLocalServer(socketAddress);
		connection.initiateServerboundPlayConnection(
				socketAddress.toString(),
				0,
				new ClientHandshakePacketListenerImpl(connection, (Minecraft)(Object)this, null, null, false, worldLoadDuration, status -> {}, new LevelLoadTracker(), null)
		);
		connection.send(new ServerboundHelloPacket(this.getUser().getName() + "12", UUID.randomUUID()));
	}

	int i = 100;
	@Inject(method = "tick", at = @At("HEAD"))
	void tick (CallbackInfo ci) {
		if (level != null && i-- == 0) {
			peakRagebait();
		}
		//	System.out.println(Arrays.toString(localPlayers));
	}

//	@WrapMethod(method = "handleKeybinds")
//	void ftick (Operation<Void> original) {
//		for (int i = 0; i < MAX_COUNT; i++) {
//			if (setLocalPlayerId(i)) {
//				original.call();
//			}
//		}
//	}

	@WrapMethod(method = "tick")
	void tick(Operation<Void> original) {
		if (getConnection() == null) {
			original.call();
			return;
		}
		try (var _ = new TemporarySwitcher()) {
			for (int i = 0; i < MAX_COUNT; i++) {
				if (setLocalPlayerId(i)) {
					//	System.out.println("Ticking connection " + player);
					//player.connection.tick();
					original.call();
				}
			}
		}


	}
	@Override
	public MultiPlayerGameMode[] getLocalGameModes() {
		return localGameModes;
	}
	@Override
	public int getLocalPlayerId() {
		return localPlayerId;
	}
	@Override
	public boolean setLocalPlayerId(int id)
	{
		if (!setupAlready) {
			setupAlready = true;
			setup();
		}
		localPlayerId = id;
		// If the player is not null, but the game mode is then this is just a temp player
		// whose only real purpose is to hold the viewport position
		if(localPlayers[id] == null || localGameModes[id] == null) return false;

		gameMode = localGameModes[id];
//		if (player != localPlayers[0] && id == 1) {
//			localPlayers[1] = player;
//		}
		player = localPlayers[id];

//		if (id == 0) {
//			player.setPos(player.position().add(0, -10, 0));
//			player.yOld -= 10;
//			player.yo-= 10;
//		}
//		if (id == 1) {
//			player.setPos(player.position().add(0, 10, 0));
//			player.yOld += 10;
//			player.yo += 10;
//		}
		setCameraEntity(localPlayers[id]);
		((GameRendererAccessor) gameRenderer).setItemInHandRenderer(itemInHandRenderers[id]);
		level = (ClientLevel) localPlayers[id].level();
		this.gameRenderer.setLevel(level);
		((LevelRendererAccessor) this.levelRenderer).setdLevel(level);
		//this.gameRenderer.le.setLevel(level);
		particleEngine.setLevel(level);
		screen = localScreens[id];

		return true;
	}

	private void setup() {
		if (throwIfSetupAgain) throw new RuntimeException("ERR ERR ERR");
		localPlayers[0] = player;
		localGameModes[0] = gameMode;
		itemInHandRenderers[0] = gameRenderer.itemInHandRenderer;
		displaySections[0] = new DisplaySection(0, 0, 1, 1);

//		localPlayers[1] = localPlayers[1] != null ? localPlayers[1] : player;
//		localGameModes[1] = localGameModes[1] != null ? localGameModes[1] : gameMode;
		itemInHandRenderers[1] = gameRenderer.itemInHandRenderer;
	}

//	@WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
//	void renderFrame(GameRenderer instance, DeltaTracker report, boolean category, Operation<Void> original) {
//		for (int i = 0; i < MAX_COUNT; i++) {
//			if (setLocalPlayerId(i)) {
//				original.call(instance, report, category);
//			}
//		}
//
//	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void renderFrame(final boolean renderLevel) {
		this.deltaTracker.advanceRealTime(Util.getMillis());
		boolean recordGpuUtilization;
		if (!this.debugEntries.isCurrentlyEnabled(DebugScreenEntries.GPU_UTILIZATION) && !this.metricsRecorder.isRecording()) {
			recordGpuUtilization = false;
			this.gpuUtilization = 0.0;
		} else {
			recordGpuUtilization = (this.currentFrameProfile == null || this.currentFrameProfile.isDone()) && !TimerQuery.getInstance().isRecording();
			if (recordGpuUtilization) {
				TimerQuery.getInstance().beginProfile();
			}
		}

		long renderStartTimer = Util.getNanos();
		ProfilerFiller profiler = Profiler.get();
		profiler.push("gpuAsync");
		RenderSystem.executePendingTasks();
		RenderTarget mainRenderTarget = this.getMainRenderTarget();
		if (getConnection() == null) {
			Drm.x = -1;
			Drm.y = -1;
			Drm.d = 1;
			mainRenderTarget.resize(this.window.getWidth(), this.window.getHeight());
			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(mainRenderTarget.getColorTexture(), 0, mainRenderTarget.getDepthTexture(), 1.0);
			profiler.popPush("gameRenderer");
			if (!this.noRender) {

				this.gameRenderer.render(this.deltaTracker, renderLevel);
			}

			profiler.popPush("blit");
			if (!this.window.isMinimized()) {
				mainRenderTarget.blitToScreen();
			}
		} else {
			try (var _ = new TemporarySwitcher()) {
				for (int i = 0; i < MAX_COUNT; i++) {
					if (setLocalPlayerId(i)) {
						DisplaySection displaySection = displaySections[i];
						//	if (i == 1) continue;
						Drm.x = (int) Mth.lerp(displaySection.xD, 0, window.getWidth());
						Drm.y = (int) Mth.lerp(displaySection.yD, 0, window.getHeight());
						mainRenderTarget.resize((int) (this.window.getWidth() * displaySection.xW), (int) (this.window.getHeight() * displaySection.yW));
						RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(mainRenderTarget.getColorTexture(), 0, mainRenderTarget.getDepthTexture(), 1.0);
						profiler.popPush("gameRenderer");
						if (!this.noRender) {
							//	System.out.println(getCameraEntity());
							if (screen != null) {
								screen.width = (int) (window.getGuiScaledWidth() * displaySection.xW);
								screen.height = (int) (window.getGuiScaledHeight() * displaySection.yW);
							}
							this.gameRenderer.resize((int) (this.window.getWidth() * displaySection.xW), (int) (this.window.getHeight() * displaySection.yW));
							Drm.d = displaySection.xW;
							Drm.d2 = displaySection.yW;
							this.gameRenderer.getMainCamera().setEntity(player);
							setLocalPlayerId(i);
//						System.out.println(localPlayers[0].level() == localPlayers[1].level());

							this.gameRenderer.setLevel((ClientLevel) player.level());
							this.gameRenderer.render(this.deltaTracker, renderLevel);
							Drm.d = 1;
							Drm.d2 = 1;
						}

						profiler.popPush("blit");
						Drm.x = (int) Mth.lerp(displaySection.xD, 0, window.getWidth());
						Drm.y = (int) Mth.lerp(displaySection.yD, 0, window.getHeight());

						if (!this.window.isMinimized()) {
							mainRenderTarget.blitToScreen();
						}
					}
				}
			}
		}



		this.frameTimeNs = Util.getNanos() - renderStartTimer;
		if (recordGpuUtilization) {
			this.currentFrameProfile = TimerQuery.getInstance().endProfile();
		}

		profiler.popPush("updateDisplay");
		if (this.tracyFrameCapture != null) {
			this.tracyFrameCapture.upload();
			this.tracyFrameCapture.capture(mainRenderTarget);
		}

		this.window.updateDisplay(this.tracyFrameCapture);
		int framerateLimit = this.framerateLimitTracker.getFramerateLimit();
		if (framerateLimit < 260) {
			RenderSystem.limitDisplayFPS(framerateLimit);
		}

		profiler.popPush("fpsUpdate");
		this.frames++;
		long currentTime = Util.getNanos();
		long frameDuration = currentTime - this.lastNanoTime;
		if (recordGpuUtilization) {
			this.savedCpuDuration = frameDuration;
		}

		this.getDebugOverlay().logFrameDuration(frameDuration);
		this.lastNanoTime = currentTime;
		if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
			this.gpuUtilization = this.currentFrameProfile.get() * 100.0 / this.savedCpuDuration;
		}

		while (Util.getMillis() >= this.lastTime + 1000L) {
			fps = this.frames;
			this.lastTime += 1000L;
			this.frames = 0;
		}

		profiler.pop();
	}

	@WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(II)V"))
	void init (Screen instance, int width, int height, Operation<Void> original) {
		System.out.println("Calling screen init while being " + localPlayerId);
		if (instance instanceof LevelLoadingScreen screen) {
			System.out.println("WHAT?");
		}
		original.call(instance, getConnection() == null ? width : (int) (width * displaySections[localPlayerId].xW), getConnection() == null ? height : (int) (height * displaySections[localPlayerId].yW));
	}
}
