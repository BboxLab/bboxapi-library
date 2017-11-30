package fr.bouyguestelecom.tv.bboxapi.callback;


import android.graphics.Bitmap;

import okhttp3.Request;

public interface IBboxGetApplicationIcon {

    void onResponse(Bitmap img);

    void onFailure(Request request, int errorCode);

}
