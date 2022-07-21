package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public interface Speaker {

    void open() throws SpeakerException;

    void play(short[] data, float volume, @Nullable Vector3d position, float maxDistance);

    default void play(short[] data, float volume, @Nullable Vector3d position) {
        play(data, volume, position, 0F);
    }

    void close();

}
