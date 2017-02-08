package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import okhttp3.Request;

public interface IBboxSubscribe {

    void onSubscribe();

    void onFailure(Request request, int errorCode);
}