package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import okhttp3.Request;

public interface IBboxGetLogoChannel {

    void onResponse(String sessionId, String logoUrl);

    void onFailure(Request request, int errorCode);
}
