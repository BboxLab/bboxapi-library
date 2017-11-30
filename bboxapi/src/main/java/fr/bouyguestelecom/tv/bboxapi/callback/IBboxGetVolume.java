package fr.bouyguestelecom.tv.bboxapi.callback;

import okhttp3.Request;

public interface IBboxGetVolume {

    void onResponse(String volume);

    void onFailure(Request request, int errorCode);
}
