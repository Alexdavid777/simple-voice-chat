package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends AbstractOptionList<T> {

    public ListScreenListBase(int width, int height, int top, int bottom, int size) {
        super(Minecraft.getInstance(), width, height, top, bottom, size);
    }

    @Override
    public void render(MatrixStack poseStack, int x, int y, float partialTicks) {
        double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) getRowLeft() * scale), (int) ((double) (height - y1) * scale), (int) ((double) (getScrollbarPosition() + 6) * scale), (int) ((double) (height - (height - y1) - y0 - 4) * scale));
        super.render(poseStack, x, y, partialTicks);
        RenderSystem.disableScissor();
    }

}
