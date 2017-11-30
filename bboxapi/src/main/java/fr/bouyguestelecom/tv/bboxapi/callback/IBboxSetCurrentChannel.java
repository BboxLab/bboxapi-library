package fr.bouyguestelecom.tv.bboxapi.callback;

import okhttp3.Request;

/**
 * Created by tao on 28/03/17.
 */

public interface IBboxSetCurrentChannel {

    void onResponse();

    void onFailure(Request request, int errorCode);
}
