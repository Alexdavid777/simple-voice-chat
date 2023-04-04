package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = 10000)
public abstract class EntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "renderNameTag")
    private void renderNameTag(Entity entity, ITextComponent component, MatrixStack poseStack, IRenderTypeBuffer multiBufferSource, int light, CallbackInfo info) {
        if (info.isCancelled()) {
            return;
        }
        if (!shouldShowName(entity)) {
            return;
        }
        if (!entity.getDisplayName().equals(component)) {
            return;
        }
        RenderEvents.RENDER_NAMEPLATE.invoker().render(entity, component, poseStack, multiBufferSource, light);
    }

    @Shadow
    protected abstract boolean shouldShowName(Entity entity);

}
