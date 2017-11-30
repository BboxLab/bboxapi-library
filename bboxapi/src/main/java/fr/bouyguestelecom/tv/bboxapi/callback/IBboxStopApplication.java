package fr.bouyguestelecom.tv.bboxapi.callback;

import okhttp3.Request;

public interface IBboxStopApplication {

    void onResponse();

    void onFailure(Request request, int errorCode);
}
