package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.DebugOverlay;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.macos.PermissionCheck;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.macos.avfoundation.AVAuthorizationStatus;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.RequestSecretPacket;
import de.maxhenkel.voicechat.net.SecretPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.channel.local.LocalAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientManager {

    @Nullable
    private ClientVoicechat client;
    private final ClientPlayerStateManager playerStateManager;
    private final ClientGroupManager groupManager;
    private final ClientCategoryManager categoryManager;
    private final PTTKeyHandler pttKeyHandler;
    private final RenderEvents renderEvents;
    private final DebugOverlay debugOverlay;
    private final KeyEvents keyEvents;
    private final Minecraft minecraft;
    private boolean hasShownPermissionsMessage;

    private ClientManager() {
        playerStateManager = new ClientPlayerStateManager();
        groupManager = new ClientGroupManager();
        categoryManager = new ClientCategoryManager();
        pttKeyHandler = new PTTKeyHandler();
        renderEvents = new RenderEvents();
        debugOverlay = new DebugOverlay();
        keyEvents = new KeyEvents();
        minecraft = Minecraft.getInstance();

        ClientCompatibilityManager.INSTANCE.onJoinWorld(this::onJoinWorld);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
        ClientCompatibilityManager.INSTANCE.onPublishServer(this::onPublishServer);

        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(connection -> {
            if (client != null) {
                client.onVoiceChatConnected(connection);
            }
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(() -> {
            if (client != null) {
                client.onVoiceChatDisconnected();
            }
        });

        CommonCompatibilityManager.INSTANCE.getNetManager().secretChannel.setClientListener((player, packet) -> authenticate(packet));
    }

    private void authenticate(SecretPacket secretPacket) {
        if (client == null) {
            Voicechat.LOGGER.error("Received secret without a client being present");
            return;
        }
        Voicechat.LOGGER.info("Received secret");
        if (client.getConnection() != null) {
            ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
        }
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            try {
                SocketAddress socketAddress = ClientCompatibilityManager.INSTANCE.getSocketAddress(connection.getConnection());
                client.connect(new InitializationData(resolveAddress(socketAddress), secretPacket));
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to connect to voice chat server", e);
            }
        }
    }

    private static String resolveAddress(SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof LocalAddress) {
            return "127.0.0.1";
        }
        if (!(socketAddress instanceof InetSocketAddress address)) {
            throw new IOException(String.format("Failed to determine server address with SocketAddress of type %s", socketAddress.getClass().getSimpleName()));
        }
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null) {
            return address.getHostString();
        }
        return inetAddress.getHostAddress();
    }

    private void onJoinWorld() {
        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            playerStateManager.setMuted(true);
        }
        if (client != null) {
            Voicechat.LOGGER.info("Disconnecting from previous connection due to server change");
            onDisconnect();
        }
        hasShownPermissionsMessage = false;
        Voicechat.LOGGER.info("Sending secret request to the server");
        NetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        client = new ClientVoicechat();
    }

    public void checkMicrophonePermissions() {
        if (!VoicechatClient.CLIENT_CONFIG.macosCheckMicrophonePermission.get()) {
            return;
        }
        if (VersionCheck.isMacOSNativeCompatible()) {
            AVAuthorizationStatus status = PermissionCheck.getMicrophonePermissions();
            if (status.equals(AVAuthorizationStatus.DENIED)) {
                if (!hasShownPermissionsMessage) {
                    ChatUtils.sendPlayerError("message.voicechat.macos_no_mic_permission", null);
                    hasShownPermissionsMessage = true;
                }
                Voicechat.LOGGER.warn("User hasn't granted microphone permissions: {}", status.name());
            } else if (!status.equals(AVAuthorizationStatus.AUTHORIZED)) {
                if (!hasShownPermissionsMessage) {
                    ChatUtils.sendPlayerError("message.voicechat.macos_unsupported_launcher", null);
                    hasShownPermissionsMessage = true;
                }
                Voicechat.LOGGER.warn("User has an unsupported launcher: {}", status.name());
            }
        }
    }

    private void onDisconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    private void onPublishServer(int port) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        try {
            Voicechat.LOGGER.info("Changing voice chat port to {}", port);
            server.changePort(port);
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                ClientVoicechatConnection connection = client.getConnection();
                if (connection != null) {
                    Voicechat.LOGGER.info("Force disconnecting due to port change");
                    connection.disconnect();
                }
            }
            NetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to change voice chat port", e);
        }
        Component portComponent = ComponentUtils.copyOnClickText(String.valueOf(server.getPort()));
        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("message.voicechat.server_port", portComponent));
    }

    @Nullable
    public static ClientVoicechat getClient() {
        return instance().client;
    }

    public static ClientPlayerStateManager getPlayerStateManager() {
        return instance().playerStateManager;
    }

    public static ClientGroupManager getGroupManager() {
        return instance().groupManager;
    }

    public static ClientCategoryManager getCategoryManager() {
        return instance().categoryManager;
    }

    public static PTTKeyHandler getPttKeyHandler() {
        return instance().pttKeyHandler;
    }

    public static RenderEvents getRenderEvents() {
        return instance().renderEvents;
    }

    public static DebugOverlay getDebugOverlay() {
        return instance().debugOverlay;
    }

    public KeyEvents getKeyEvents() {
        return keyEvents;
    }

    private static ClientManager instance;

    public static synchronized ClientManager instance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

}
