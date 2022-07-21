package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    protected static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.select_microphone.title");
    protected static final ITextComponent NO_MICROPHONE = new TranslationTextComponent("message.voicechat.no_microphone").withStyle(TextFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return MicrophoneManager.deviceNames();
    }

    @Override
    public String getSelectedDevice() {
        return VoicechatClient.CLIENT_CONFIG.microphone.get();
    }

    @Override
    public ResourceLocation getIcon(String device) {
        return MICROPHONE_ICON;
    }

    @Override
    public ITextComponent getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    @Override
    public void onSelect(String device) {
        VoicechatClient.CLIENT_CONFIG.microphone.set(device).save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }
}
