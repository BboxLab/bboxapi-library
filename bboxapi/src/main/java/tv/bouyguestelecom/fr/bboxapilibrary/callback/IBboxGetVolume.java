package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import okhttp3.Request;

public interface IBboxGetVolume {

    void onResponse(String volume);

    void onFailure(Request request, int errorCode);
}
