package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ToggleImageButton extends ImageButton {

    @Nullable
    protected Supplier<Boolean> stateSupplier;

    public ToggleImageButton(int x, int y, ResourceLocation texture, @Nullable Supplier<Boolean> stateSupplier, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, texture, onPress, tooltipSupplier);
        this.stateSupplier = stateSupplier;
    }

    @Override
    protected void renderImage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (stateSupplier == null) {
            return;
        }
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (stateSupplier.get()) {
            guiGraphics.blit(RenderType::guiTextured, texture, getX() + 2, getY() + 2, 16, 0, 16, 16, 32, 32);
        } else {
            guiGraphics.blit(RenderType::guiTextured, texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 32, 32);
        }
    }

}
