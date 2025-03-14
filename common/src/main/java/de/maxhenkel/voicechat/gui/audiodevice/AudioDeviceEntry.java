package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AudioDeviceEntry extends ListScreenEntryBase<AudioDeviceEntry> {

    protected static final ResourceLocation SELECTED = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/device_selected.png");

    protected static final int PADDING = 4;
    protected static final int BG_FILL = ARGB.color(255, 74, 74, 74);
    protected static final int BG_FILL_HOVERED = ARGB.color(255, 90, 90, 90);
    protected static final int BG_FILL_SELECTED = ARGB.color(255, 40, 40, 40);
    protected static final int DEVICE_NAME_COLOR = ARGB.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final String device;
    protected final String visibleDeviceName;
    @Nullable
    protected final ResourceLocation icon;
    protected final Supplier<Boolean> isSelected;

    public AudioDeviceEntry(String device, String name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected) {
        this.device = device;
        this.icon = icon;
        this.isSelected = isSelected;
        this.visibleDeviceName = name;
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        boolean selected = isSelected.get();
        if (selected) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_SELECTED);
        } else if (hovered) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_HOVERED);
        } else {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL);
        }

        if (icon != null) {
            guiGraphics.blit(RenderType::guiTextured, icon, left + PADDING, top + height / 2 - 8, 16, 16, 16, 16, 16, 16);
        }
        if (selected) {
            guiGraphics.blit(RenderType::guiTextured, SELECTED, left + PADDING, top + height / 2 - 8, 16, 16, 16, 16, 16, 16);
        }

        float deviceWidth = minecraft.font.width(visibleDeviceName);
        float space = width - PADDING - 16 - PADDING - PADDING;
        float scale = Math.min(space / deviceWidth, 1F);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(left + PADDING + 16 + PADDING, top + height / 2 - (minecraft.font.lineHeight * scale) / 2, 0D);
        guiGraphics.pose().scale(scale, scale, 1F);

        guiGraphics.drawString(minecraft.font, visibleDeviceName, 0, 0, DEVICE_NAME_COLOR, false);
        guiGraphics.pose().popPose();
    }

    public String getDevice() {
        return device;
    }
}
