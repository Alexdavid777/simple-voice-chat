package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.events.*;
import de.maxhenkel.voicechat.mixin.ConnectionAccessor;
import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

import java.net.SocketAddress;
import java.util.function.Consumer;

public class FabricClientCompatibilityManager extends ClientCompatibilityManager {

    private static final Minecraft mc = Minecraft.getInstance();

    @Override
    public void onRenderNamePlate(RenderNameplateEvent onRenderNamePlate) {
        RenderEvents.RENDER_NAMEPLATE.register(onRenderNamePlate);
    }

    @Override
    public void onRenderHUD(RenderHUDEvent onRenderHUD) {
        RenderEvents.RENDER_HUD.register(guiGraphics -> onRenderHUD.render(guiGraphics, mc.getDeltaTracker().getRealtimeDeltaTicks()));
    }

    @Override
    public void onKeyboardEvent(KeyboardEvent onKeyboardEvent) {
        InputEvents.KEYBOARD_KEY.register(onKeyboardEvent);
    }

    @Override
    public void onMouseEvent(MouseEvent onMouseEvent) {
        InputEvents.MOUSE_KEY.register(onMouseEvent);
    }

    @Override
    public void onClientTick(Runnable onClientTick) {
        ClientTickEvents.START_CLIENT_TICK.register(client -> onClientTick.run());
    }

    @Override
    public InputConstants.Key getBoundKeyOf(KeyMapping keyBinding) {
        return KeyBindingHelper.getBoundKeyOf(keyBinding);
    }

    @Override
    public void onHandleKeyBinds(Runnable onHandleKeyBinds) {
        InputEvents.HANDLE_KEYBINDS.register(onHandleKeyBinds);
    }

    @Override
    public KeyMapping registerKeyBinding(KeyMapping keyBinding) {
        return KeyBindingHelper.registerKeyBinding(keyBinding);
    }

    @Override
    public void emitVoiceChatConnectedEvent(ClientVoicechatConnection client) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.invoker().accept(client);
    }

    @Override
    public void emitVoiceChatDisconnectedEvent() {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.invoker().run();
    }

    @Override
    public void onVoiceChatConnected(Consumer<ClientVoicechatConnection> onVoiceChatConnected) {
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.register(onVoiceChatConnected);
    }

    @Override
    public void onVoiceChatDisconnected(Runnable onVoiceChatDisconnected) {
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.register(onVoiceChatDisconnected);
    }

    @Override
    public void onDisconnect(Runnable onDisconnect) {
        ClientWorldEvents.DISCONNECT.register(onDisconnect);
    }

    @Override
    public void onJoinWorld(Runnable onJoinWorld) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoinWorld.run());
    }

    @Override
    public void onPublishServer(Consumer<Integer> onPublishServer) {
        PublishServerEvents.SERVER_PUBLISHED.register(onPublishServer);
    }

    @Override
    public SocketAddress getSocketAddress(Connection connection) {
        return ((ConnectionAccessor) connection).getChannel().remoteAddress();
    }

    @Override
    public void addResourcePackSource(PackRepository packRepository, RepositorySource repositorySource) {
        IPackRepository repository = (IPackRepository) packRepository;
        repository.addSource(repositorySource);
    }
}
