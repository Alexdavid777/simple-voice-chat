package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.QuiltServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.QuiltCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.QuiltPermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraft.client.Minecraft;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public class QuiltVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        ServerLifecycleEvents.READY.register(server -> {
            SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), true, QuiltServerConfig::new);
        });

        initialize();
    }

    @Override
    public CommonCompatibilityManager createCompatibilityManager() {
        return new QuiltCommonCompatibilityManager();
    }

    @Override
    protected PermissionManager createPermissionManager() {
        return new QuiltPermissionManager();
    }

    public static boolean isClient() {
        try {
            Minecraft.getInstance();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

}
