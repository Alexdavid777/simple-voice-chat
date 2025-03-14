package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.tooltips.DisableTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.HideTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.MuteTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.RecordingTooltipSupplier;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final ResourceLocation HIDE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/hide_button.png");
    private static final ResourceLocation VOLUMES = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/adjust_volumes.png");
    private static final ResourceLocation SPEAKER = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_button.png");
    private static final ResourceLocation RECORD = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/record_button.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.voice_chat.title");
    private static final Component SETTINGS = Component.translatable("message.voicechat.settings");
    private static final Component GROUP = Component.translatable("message.voicechat.group");
    public static final Component ADJUST_PLAYER_VOLUMES = Component.translatable("message.voicechat.adjust_volumes");

    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private HoverArea recordingHoverArea;

    private ClientPlayerStateManager stateManager;

    public VoiceChatScreen() {
        super(TITLE, 195, 76);
        stateManager = ClientManager.getPlayerStateManager();
    }

    @Override
    protected void init() {
        super.init();
        @Nullable ClientVoicechat client = ClientManager.getClient();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addRenderableWidget(mute);

        disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addRenderableWidget(disable);

        ImageButton volumes = new ImageButton(guiLeft + 6 + 20 + 2 + 20 + 2, guiTop + ySize - 6 - 20, VOLUMES, button -> {
            minecraft.setScreen(new AdjustVolumesScreen());
        }, (button, guiGraphics, font, mouseX, mouseY) -> {
            guiGraphics.renderTooltip(font, ADJUST_PLAYER_VOLUMES, mouseX, mouseY);
        });
        addRenderableWidget(volumes);

        if (client != null && VoicechatClient.CLIENT_CONFIG.useNatives.get()) {
            if (client.getRecorder() != null || (client.getConnection() != null && client.getConnection().getData().allowRecording())) {
                ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> ClientManager.getClient() != null && ClientManager.getClient().getRecorder() != null, button -> toggleRecording(), new RecordingTooltipSupplier(this));
                addRenderableWidget(record);
            }
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get()).save();
        }, new HideTooltipSupplier(this));
        addRenderableWidget(hide);

        Button settings = Button.builder(SETTINGS, button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        }).bounds(guiLeft + 6, guiTop + 6 + 15, 75, 20).build();
        addRenderableWidget(settings);

        Button group = Button.builder(GROUP, button -> {
            ClientGroup g = stateManager.getGroup();
            if (g != null) {
                minecraft.setScreen(new GroupScreen(g));
            } else {
                minecraft.setScreen(new JoinGroupScreen());
            }
        }).bounds(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20).build();
        addRenderableWidget(group);

        group.active = client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled();
        recordingHoverArea = new HoverArea(6 + 20 + 2 + 20 + 2 + 20 + 2, ySize - 6 - 20, xSize - ((6 + 20 + 2 + 20 + 2) * 2 + 20 + 2), 20);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = MuteTooltipSupplier.canMuteMic();
        disable.active = stateManager.canEnable();
    }

    private void toggleRecording() {
        ClientVoicechat c = ClientManager.getClient();
        if (c == null) {
            return;
        }
        c.toggleRecording();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()) {
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(RenderType::guiTextured, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        guiGraphics.drawString(font, TITLE.getVisualOrderText(), guiLeft + (xSize - titleWidth) / 2, guiTop + 7, FONT_COLOR, false);

        ClientVoicechat client = ClientManager.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            MutableComponent time = Component.literal(recorder.getDuration());
            guiGraphics.drawString(font, time.withStyle(ChatFormatting.DARK_RED), guiLeft + recordingHoverArea.getPosX() + recordingHoverArea.getWidth() / 2 - font.width(time) / 2, guiTop + recordingHoverArea.getPosY() + recordingHoverArea.getHeight() / 2 - font.lineHeight / 2, 0, false);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, Component.translatable("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
