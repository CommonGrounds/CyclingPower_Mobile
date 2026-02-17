package dev.java4now.AudioUtils;

import com.gluonhq.attach.audio.Audio;
import com.gluonhq.attach.audio.AudioService;
import com.gluonhq.attach.util.Platform;
import dev.java4now.App;
import dev.java4now.http.SaveUser;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class Sound {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sound.class);

    private AudioService audioService;
    private Abstract_Audio audio;

    public static String sonar = Objects.requireNonNull(App.class.getResource("sounds/sonar-sound-effect.mp3")).toExternalForm();
    public static String click = Objects.requireNonNull(App.class.getResource("sounds/bike_ping_2khz.wav")).toExternalForm();
    public static String finish = Objects.requireNonNull(App.class.getResource("sounds/finish_ping.wav")).toExternalForm();

    public Sound(String audio_file){
        if(Platform.isDesktop()){
//            LOGGER.debug("DESKTOP");
            audio = new Desktop_Audio(audio_file);
        }else{                              // attach pristup
//            LOGGER.debug("MOBILE");
            create_audio();
            if (audioService == null) { // It seems the audio service is implemented only for Android, so this happens on other platforms
                LOGGER.warn("Unable to load Gluon Audio Service - No sound/music will be played");
            }
            audio= new Mobile_Audio(audio_file);
        }
    }

    public void setVolume(double d) {
        audio.setVolume(d);
    }
    public void setLooping(boolean b) {
        audio.setLooping(b);
    }
    public void play() {
        audio.play();
    }
    public void stop() {
        audio.stop();
    }

    //------------------------------------------
    private void create_audio(){
        if(!com.gluonhq.attach.util.Platform.isDesktop()){
            audioService = com.gluonhq.attach.audio.AudioService.create().orElse(null);
        }
    }



    //------------------------------------------------------
    private Audio load_mobile_audio_resource(String str){
        Optional<Audio> audio;

        try {
            URL url = new URL(str);
            audio = audioService.loadMusic(url);
            if (audio.isEmpty()) {
                LOGGER.warn("WARNING: Unable to load " + url);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("ERROR: while loading " + str);
            e.printStackTrace();
            return null;
        }

        return audio.get();
    }


    //------------------------------------------------------
// Abstract klasa koja implementira metode zajednicke za sve klase koje je extenduju tako da mogu da stavim da svaka moze biti Sorting tip = new SomeOtherExtendedClass()
    abstract static class Abstract_Audio{
        abstract void setVolume(double d);
        abstract void setLooping(boolean b);
        abstract void play();
        abstract void stop();
    }



//--------------------------------------------------
    private static class Desktop_Audio extends Abstract_Audio{
        AudioClip audio;

        Desktop_Audio(String source){
            this.audio = new AudioClip(source);
        }

        @Override
        void setVolume(double d) {
            audio.setVolume(d);
        }

        @Override
        void setLooping(boolean b) {
            if(b){
                audio.setCycleCount(Integer.MAX_VALUE);
            }
        }

        @Override
        void play() {
            audio.play();
        }

        @Override
        void stop() {
            audio.stop();
        }
    }


    //--------------------------------------------------
    private class Mobile_Audio extends Abstract_Audio{
        Audio audio;

        Mobile_Audio(String source){
            this.audio = load_mobile_audio_resource(source);
        }

        @Override
        void setVolume(double d) {
            audio.setVolume(d);
        }

        @Override
        void setLooping(boolean b) {
            audio.setLooping(b);
        }

        @Override
        void play() {
            audio.play();
        }

        @Override
        void stop() {
            audio.stop();
        }
    }
}