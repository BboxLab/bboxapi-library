package fr.bouyguestelecom.tv.bboxapi.callback;


import java.io.IOException;

import okhttp3.Request;

public interface IBboxNotif {

    void onResponse();

    void onFailure(Request request, IOException e);

}
