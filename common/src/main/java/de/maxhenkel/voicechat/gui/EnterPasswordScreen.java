package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_enter_password.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.enter_password.title");
    private static final Component JOIN_GROUP = Component.translatable("message.voicechat.join_group");
    private static final Component ENTER_GROUP_PASSWORD = Component.translatable("message.voicechat.enter_group_password");
    private static final Component PASSWORD = Component.translatable("message.voicechat.password");

    private EditBox password;
    private Button joinGroup;
    private ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(TITLE, 195, 74);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        password = new EditBox(font, guiLeft + 7, guiTop + 7 + (font.lineHeight + 5) * 2 - 5 + 2, xSize - 7 * 2, 10, Component.empty());
        password.setMaxLength(32);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        joinGroup = Button.builder(JOIN_GROUP, button -> {
            joinGroup();
        }).bounds(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20).build();
        addRenderableWidget(joinGroup);
    }

    private void joinGroup() {
        if (!password.getValue().isEmpty()) {
            NetManager.sendToServer(new JoinGroupPacket(group.getId(), password.getValue()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        joinGroup.active = !password.getValue().isEmpty();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(RenderType::guiTextured, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, ENTER_GROUP_PASSWORD, guiLeft + xSize / 2 - font.width(ENTER_GROUP_PASSWORD) / 2, guiTop + 7, FONT_COLOR, false);
        guiGraphics.drawString(font, PASSWORD, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            joinGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String passwordText = password.getValue();
        init(client, width, height);
        password.setValue(passwordText);
    }

}
