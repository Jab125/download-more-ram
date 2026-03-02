package dev.jab125.drm.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.jab125.drm.Indexables;
import dev.jab125.drm.MinecraftExtension;
import dev.jab125.drm.TemporarySwitcher;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl implements ClientGamePacketListener, TickablePacketListener, Indexables {
	@Shadow
	private Set<ResourceKey<Level>> levels;

	@Shadow
	private int serverChunkRadius;

	@Shadow
	private int serverSimulationDistance;

	@Shadow
	private ClientLevel.ClientLevelData levelData;

	@Shadow
	private ClientLevel level;

	private int index = -1;
	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
		super(minecraft, connection, cookie);
	}

	@Shadow
	protected abstract void setClientLoaded(boolean loaded);

	@Shadow
	@Final
	private ClientDebugSubscriber debugSubscriber;

	@Shadow
	protected abstract void startWaitingForNewLevel(LocalPlayer player, ClientLevel level, LevelLoadingScreen.Reason reason);

	@Shadow
	private @Nullable LocalChatSession chatSession;

	@Shadow
	private SignedMessageChain.Encoder signedMessageEncoder;

	@Shadow
	private int nextChatIndex;

	@Shadow
	private LastSeenMessagesTracker lastSeenMessages;

	@Shadow
	private MessageSignatureCache messageSignatureCache;

	@Shadow
	public abstract void prepareKeyPair();

	@Shadow
	private boolean serverEnforcesSecureChat;

	@Shadow
	protected abstract boolean enforcesSecureChat();

	@Shadow
	@Final
	private static Component UNSECURE_SERVER_TOAST_TITLE;

	@Shadow
	@Final
	private static Component UNSERURE_SERVER_TOAST;

	private static boolean one = true;
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void handleLogin(final ClientboundLoginPacket packet) {
//		if (true) {
//			try {
//				throw new RuntimeException("E");
//			} catch (Throwable throwable) {
//				throwable.printStackTrace();
//			}
//		}
		PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft.packetProcessor());
		this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, (ClientPacketListener) (Object) this);
		CommonPlayerSpawnInfo spawnInfo = packet.commonPlayerSpawnInfo();
		List<ResourceKey<Level>> levels = Lists.<ResourceKey<Level>>newArrayList(packet.levels());
		Collections.shuffle(levels);
		this.levels = Sets.<ResourceKey<Level>>newLinkedHashSet(levels);
		ResourceKey<Level> dimension = spawnInfo.dimension();
		Holder<DimensionType> dimensionType = spawnInfo.dimensionType();
		this.serverChunkRadius = packet.chunkRadius();
		this.serverSimulationDistance = packet.simulationDistance();
		boolean isDebug = spawnInfo.isDebug();
		boolean isFlat = spawnInfo.isFlat();
		int seaLevel = spawnInfo.seaLevel();
		ClientLevel.ClientLevelData levelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, packet.hardcore(), isFlat);
		this.levelData = levelData;
		this.level = new ClientLevel(
				(ClientPacketListener) (Object) this,
				levelData,
				dimension,
				dimensionType,
				this.serverChunkRadius,
				this.serverSimulationDistance,
				this.minecraft.levelRenderer,
				isDebug,
				spawnInfo.seed(),
				seaLevel
		);
		this.minecraft.setLevel(this.level);
		LocalPlayer newPlayer = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
		MinecraftExtension ext = (MinecraftExtension) this.minecraft;
		LocalPlayer[] localPlayers = ext.getLocalPlayers();
		if (localPlayers[0] == null && one) {
			one = false;
			localPlayers[0] = newPlayer;
			minecraft.player = newPlayer;
						if (this.minecraft.getSingleplayerServer() != null) {
				this.minecraft.getSingleplayerServer().setUUID(newPlayer.getUUID());
			}
						index = 0;
		} else {
			localPlayers[1] = newPlayer;
			((MinecraftExtension)this.minecraft).getLocalGameModes()[1] = this.minecraft.gameMode;
			index = 1;
			System.out.println("SETTING UP SECOND PLAYER");
			ext.getDisplaySections()[0].xW /= 2;
			ext.getDisplaySections()[1] = ext.getDisplaySections()[0].clone();
			ext.getDisplaySections()[1].xD += 0.5;
		}
//		if (newPlayer == null) {
//			newPlayer = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
//			newPlayer.setYRot(-180.0F);
//			if (this.minecraft.getSingleplayerServer() != null) {
//				this.minecraft.getSingleplayerServer().setUUID(newPlayer.getUUID());
//			}
//		}

		this.setClientLoaded(false);
		this.debugSubscriber.clear();
		this.minecraft.levelRenderer.debugRenderer.refreshRendererList();
		newPlayer.resetPos();
		newPlayer.setId(packet.playerId());
		this.level.addEntity(newPlayer);
		newPlayer.input = new KeyboardInput(this.minecraft.options);
		this.minecraft.gameMode.adjustPlayer(newPlayer);
		//this.minecraft.setCameraEntity(newPlayer);
		try (var _ = new TemporarySwitcher()) {
			((MinecraftExtension)this.minecraft).setLocalPlayerId(1);
			this.startWaitingForNewLevel(newPlayer, this.level, LevelLoadingScreen.Reason.OTHER);
		}

		newPlayer.setReducedDebugInfo(packet.reducedDebugInfo());
		newPlayer.setShowDeathScreen(packet.showDeathScreen());
		newPlayer.setDoLimitedCrafting(packet.doLimitedCrafting());
		newPlayer.setLastDeathLocation(spawnInfo.lastDeathLocation());
		newPlayer.setPortalCooldown(spawnInfo.portalCooldown());
		this.minecraft.gameMode.setLocalMode(spawnInfo.gameType(), spawnInfo.previousGameType());
		this.minecraft.options.setServerRenderDistance(packet.chunkRadius());
		this.chatSession = null;
		this.signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
		this.nextChatIndex = 0;
		this.lastSeenMessages = new LastSeenMessagesTracker(20);
		this.messageSignatureCache = MessageSignatureCache.createDefault();
		if (this.connection.isEncrypted()) {
			this.prepareKeyPair();
		}

		this.telemetryManager.onPlayerInfoReceived(spawnInfo.gameType(), packet.hardcore());
		this.minecraft.quickPlayLog().log(this.minecraft);
		this.serverEnforcesSecureChat = packet.enforcesSecureChat();
		if (this.serverData != null && !this.seenInsecureChatWarning && !this.enforcesSecureChat()) {
			SystemToast toast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastId.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
			this.minecraft.getToastManager().addToast(toast);
			this.seenInsecureChatWarning = true;
		}
	}

//	@Inject(method = "handleEntityPositionSync", at = @At("HEAD"))
//	void handle(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
//		try {
//			throw new RuntimeException();
//		} catch ( Throwable t) {
//			t.printStackTrace();;
//		}
//	}

}
