package tv.bouyguestelecom.fr.bboxapilibrary.model;


import java.io.Serializable;

public class Media implements Serializable {
    public static final String TAG_URL = "url";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_MEDIA_TYPE_CODE = "mediaTypeCode";
    public static final String TAG_ASPECT_RATIO = "aspectRatio";

    private String url;
    private int width;
    private int height;
    private int mediaTypeCode;
    private double aspectRatio;

    public Media(String url, int width, int height, int mediaTypeCode, double aspectRatio) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.mediaTypeCode = mediaTypeCode;
        this.aspectRatio = aspectRatio;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public int getMediaTypeCode() {
        return mediaTypeCode;
    }

    @Override
    public String toString() {
        return "Media{" +
                "url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", aspectRatio=" + aspectRatio +
                ", mediaTypeCode=" + mediaTypeCode +
                '}';
    }
}
