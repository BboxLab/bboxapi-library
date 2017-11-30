package fr.bouyguestelecom.tv.bboxapi.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {
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

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

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

    protected Media(Parcel in) {
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
        mediaTypeCode = in.readInt();
        aspectRatio = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeInt(width);
        parcel.writeInt(height);
        parcel.writeInt(mediaTypeCode);
        parcel.writeDouble(aspectRatio);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
