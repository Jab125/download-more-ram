package dev.jab125.drm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.jab125.drm.DisplaySection;
import dev.jab125.drm.Drm;
import dev.jab125.drm.MinecraftExtension;
import dev.jab125.drm.TemporarySwitcher;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.FramerateLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.User;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(value = Minecraft.class, priority = 500)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, MinecraftExtension {
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

	public MinecraftMixin(String name, boolean propagatesCrashes) {
		super(name, propagatesCrashes);
	}

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

	@Shadow
	public abstract void stop();

	@Shadow
	private @Nullable CompletableFuture<Void> pendingReload;

	@Shadow
	public abstract CompletableFuture<Void> reloadResourcePacks();

	@Shadow
	private @Nullable Overlay overlay;

	@Shadow
	public abstract Gizmos.TemporaryCollection collectPerTickGizmos();

	@Shadow
	@Final
	private PacketProcessor packetProcessor;

	@Shadow
	protected abstract boolean isLevelRunningNormally();

	@Shadow
	@Final
	private TextureManager textureManager;
	@Shadow
	private List<SimpleGizmoCollector.GizmoInstance> drainedLatestTickGizmos;
	@Shadow
	@Final
	private SimpleGizmoCollector perTickGizmos;
	@Shadow
	@Final
	private SoundManager soundManager;
	@Shadow
	@Final
	private ToastManager toastManager;
	@Shadow
	@Final
	public MouseHandler mouseHandler;
	@Shadow
	private volatile boolean pause;

	@Shadow
	public abstract boolean hasSingleplayerServer();

	@Shadow
	private @Nullable IntegratedServer singleplayerServer;
	@Shadow
	@Final
	private RenderTarget mainRenderTarget;

	@Shadow
	protected abstract void pauseIfInactive();

	@Shadow
	public abstract boolean isGameLoadFinished();

	@Shadow
	protected abstract void pick(float partialTicks);

	@Shadow
	@Final
	public Options options;
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;
	@Shadow
	@Final
	private ItemModelResolver itemModelResolver;
	@Shadow
	@Final
	public Gui gui;
	private static final int MAX_COUNT = 4;
	private int localPlayerId = 0;
	private LocalPlayer[] localPlayers = new LocalPlayer[MAX_COUNT];
	private MultiPlayerGameMode[] localGameModes = new MultiPlayerGameMode[MAX_COUNT];
	private ItemInHandRenderer[] itemInHandRenderers = new ItemInHandRenderer[MAX_COUNT];
	private Screen[] localScreens = new Screen[MAX_COUNT];
	private DisplaySection[] displaySections = new DisplaySection[MAX_COUNT];
	private Optional<ControllerEntity>[] controllers = new Optional[]{Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()};
	private Optional<InGameInputHandler>[] inputHandlers = new Optional[]{Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()};
	private InputMode[] currentInputMode = new InputMode[MAX_COUNT];
	private ChatComponent[] chatComponents = new ChatComponent[MAX_COUNT];

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
		((GuiAccessor)gui).setChat(chatComponents[id]);

		((ControlifyAccessor) Controlify.instance()).setCurrentController(controllers[id].orElse(null));
		((ControlifyAccessor) Controlify.instance()).setCurrentInputMode(currentInputMode[id]);
		((ControlifyAccessor) Controlify.instance()).setConsecutiveInputSwitches(-2147438381);
		if (id == 1 && player != null) {
			player.input = new KeyboardInput(options);
		}
		if (id == 0 && localPlayers[1] != null) {
			Drm.houston();
		}
		return true;
	}

	private void setup() {
		if (throwIfSetupAgain) throw new RuntimeException("ERR ERR ERR");
		localPlayers[0] = player;
		localGameModes[0] = gameMode;
		itemInHandRenderers[0] = gameRenderer.itemInHandRenderer;
		displaySections[0] = new DisplaySection(0, 0, 1, 1);
		controllers[0] = Controlify.instance().getCurrentController();
		inputHandlers[0] = Controlify.instance().inGameInputHandler();
		currentInputMode[0] = InputMode.MIXED;
		currentInputMode[1] = InputMode.KEYBOARD_MOUSE;
		chatComponents[0] = new ChatComponent((Minecraft) (Object) this);
		chatComponents[1] = new ChatComponent((Minecraft) (Object) this);


//		localPlayers[1] = localPlayers[1] != null ? localPlayers[1] : player;
//		localGameModes[1] = localGameModes[1] != null ? localGameModes[1] : gameMode;
		itemInHandRenderers[1] = new ItemInHandRenderer((Minecraft) (Object) this, entityRenderDispatcher, itemModelResolver);
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
	private void renderFrame(final boolean advanceGameTime) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("update");
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
		this.pauseIfInactive();
		this.window.updateFullscreenIfChanged();

		if (getConnection() == null || player == null) {
			Drm.x = -1;
			Drm.y = -1;
			Drm.d = 1;
			if (this.isGameLoadFinished() && advanceGameTime && this.level != null) {
				this.level.update();
			}

			this.gameRenderer.update(this.deltaTracker, advanceGameTime);
			float worldPartialTicks = this.deltaTracker.getGameTimeDeltaPartialTick(false);
			this.pick(worldPartialTicks);
			profiler.popPush("extract");
			this.gameRenderer.getGameRenderState().framerateLimit = this.framerateLimitTracker.getFramerateLimit();
			this.gameRenderer.extract(this.deltaTracker, advanceGameTime);
			profiler.popPush("gpuAsync");
			RenderSystem.executePendingTasks();
			profiler.pop();
			this.gameRenderer.render(this.deltaTracker, advanceGameTime);
			profiler.push("present");
			if (!this.gameRenderer.getGameRenderState().windowRenderState.isMinimized) {
				this.mainRenderTarget.blitToScreen();
			}
		} else {
			try (var _ = new TemporarySwitcher()) {
				for (int i = 0; i < MAX_COUNT; i++) {
//					Drm.houston();
					if (setLocalPlayerId(i)) {

						DisplaySection displaySection = displaySections[i];
						//	if (i == 1) continue;
						Drm.x = (int) Mth.lerp(displaySection.xD, 0, window.getWidth());
						Drm.y = (int) Mth.lerp(displaySection.yD, 0, window.getHeight());
						mainRenderTarget.resize((int) (this.window.getWidth() * displaySection.xW), (int) (this.window.getHeight() * displaySection.yW));

						if (screen != null) {
							screen.width = (int) (window.getGuiScaledWidth() * displaySection.xW);
							screen.height = (int) (window.getGuiScaledHeight() * displaySection.yW);
						}
						this.gameRenderer.resize((int) (this.window.getWidth() * displaySection.xW), (int) (this.window.getHeight() * displaySection.yW));


						if (this.isGameLoadFinished() && advanceGameTime && this.level != null) {
							this.level.update();
						}

						Drm.d = displaySection.xW;
						Drm.d2 = displaySection.yW;

						this.gameRenderer.update(this.deltaTracker, advanceGameTime);
						float worldPartialTicks = this.deltaTracker.getGameTimeDeltaPartialTick(false);
						this.pick(worldPartialTicks);
						profiler.popPush("extract");
						this.gameRenderer.getGameRenderState().framerateLimit = this.framerateLimitTracker.getFramerateLimit();

						this.gameRenderer.extract(this.deltaTracker, advanceGameTime);
						profiler.popPush("gpuAsync");
						RenderSystem.executePendingTasks();
						profiler.pop();
						this.gameRenderer.render(this.deltaTracker, advanceGameTime);
						Drm.d = 1;
						Drm.d2 = 1;
						profiler.push("present");
						if (!this.gameRenderer.getGameRenderState().windowRenderState.isMinimized) {
							Drm.x = (int) Mth.lerp(displaySection.xD, 0, window.getWidth());
							Drm.y = (int) Mth.lerp(displaySection.yD, 0, window.getHeight());
							this.mainRenderTarget.blitToScreen();
						}
					}
				}
			}
		}

		this.frameTimeNs = Util.getNanos() - renderStartTimer;
		if (recordGpuUtilization) {
			this.currentFrameProfile = TimerQuery.getInstance().endProfile();
		}

		profiler.popPush("swapBuffers");
		if (this.tracyFrameCapture != null) {
			this.tracyFrameCapture.upload();
			this.tracyFrameCapture.capture(this.mainRenderTarget);
		}

		RenderSystem.flipFrame(this.tracyFrameCapture);
		profiler.popPush("frameLimiter");
		int framerateLimit = this.gameRenderer.getGameRenderState().framerateLimit;
		if (framerateLimit < 260) {
			FramerateLimiter.limitDisplayFPS(framerateLimit);
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
