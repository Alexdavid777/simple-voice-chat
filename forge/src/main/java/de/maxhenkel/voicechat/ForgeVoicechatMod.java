package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ConfigMigrator;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;

@Mod(ForgeVoicechatMod.MODID)
public class ForgeVoicechatMod extends Voicechat {

    protected FMLJavaModLoadingContext context;

    public ForgeVoicechatMod(FMLJavaModLoadingContext context) {
        this.context = context;
        context.getModEventBus().addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new ForgeVoicechatClientMod(context));
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        MinecraftForge.EVENT_BUS.register(new ConfigMigrator());
        MinecraftForge.EVENT_BUS.register(CommonCompatibilityManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PermissionManager.INSTANCE);
        context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> {
            return new IExtensionPoint.DisplayTest(() -> String.valueOf(Voicechat.COMPATIBILITY_VERSION), (incoming, isNetwork) -> {
                return Objects.equals(incoming, String.valueOf(Voicechat.COMPATIBILITY_VERSION));
            });
        });
    }

}