package tv.bouyguestelecom.fr.bboxapilibrary.model;


import java.io.Serializable;

public class AudioInfo implements Serializable {

    public static final String TAG_SOUND_MODE = "soundMode";
    public static final String TAG_LANGUAGE = "language";

    private String soundMode;
    private String language;

    public AudioInfo(String soundMode, String language) {
        this.soundMode = soundMode;
        this.language = language;
    }

    public String getSoundMode() {
        return soundMode;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "AudioInfo{" +
                "soundMode='" + soundMode + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}