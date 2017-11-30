package fr.bouyguestelecom.tv.bboxapi.callback;


import okhttp3.Request;

public interface IBboxRegisterApp {

    void onResponse(String appId);

    void onFailure(Request request, int errorCode);

}
