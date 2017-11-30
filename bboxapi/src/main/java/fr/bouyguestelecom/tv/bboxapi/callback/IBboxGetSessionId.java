package fr.bouyguestelecom.tv.bboxapi.callback;


import okhttp3.Request;

public interface IBboxGetSessionId {

    void onResponse(String sessionId);

    void onFailure(Request request, int errorCode);

}
