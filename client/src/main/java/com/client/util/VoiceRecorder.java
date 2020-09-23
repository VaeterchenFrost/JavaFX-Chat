package com.client.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.client.chatwindow.Listener;

import org.slf4j.LoggerFactory;

/**
 * @author Dominic
 *         <p>
 *         Website: www.dominicheal.com
 *         <p>
 *         Github: www.github.com/DomHeal
 * @since 16-Oct-16
 */
public class VoiceRecorder extends VoiceUtil {

    public static void captureAudio() {
        try {
            final AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    isRecording = true;
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                        while (isRecording) {
                            int count = line.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                out.write(buffer, 0, count);
                            }
                        }
                        line.close();
                        line.flush();
                        Listener.sendVoiceMessage(out.toByteArray());
                    } catch (IOException e) {
                        LoggerFactory.getLogger(VoiceRecorder.class).error(e.getLocalizedMessage());
                    }
                }
            };
            Thread captureThread = new Thread(runner);
            captureThread.start();
        } catch (LineUnavailableException e) {
            LoggerFactory.getLogger(VoiceRecorder.class).error("Line unavailable: ", e);
        }
    }
}
