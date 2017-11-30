package fr.bouyguestelecom.tv.bboxapi.callback;


import okhttp3.Request;

public interface IBboxGetToken {

    void onResponse(String token);

    void onFailure(Request request, int errorCode);

}
