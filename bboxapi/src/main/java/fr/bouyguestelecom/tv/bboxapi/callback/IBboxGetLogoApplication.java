package fr.bouyguestelecom.tv.bboxapi.callback;


public interface IBboxGetLogoApplication {

    void onResponse(String sessionId, String url);

    void onFailure(Exception e);
}
