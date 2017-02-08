package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import okhttp3.Request;

public interface IBboxSetVolume {

    void onResponse();

    void onFailure(Request request, int errorCode);
}
