package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import okhttp3.Request;

public interface IBboxUnsubscribe {

    void onUnsubscribe();

    void onFailure(Request request, int errorCode);
}