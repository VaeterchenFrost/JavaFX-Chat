package com.client.util;

import javax.sound.sampled.AudioFormat;

/**
 * @author Dominic
 *         <p>
 *         Website: www.dominicheal.com
 *         <p>
 *         Github: www.github.com/DomHeal
 * @since 16-Oct-16
 */
public class VoiceUtil {
    public static void setRecording(boolean flag) {
        isRecording = flag;
    }

    public static boolean isRecording() {
        return isRecording;
    }

    protected static boolean isRecording = false;

    /**
     * Defines an audio format
     */
    static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
