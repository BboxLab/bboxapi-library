package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.io.IOException;

import okhttp3.Request;

public interface IBboxNotif {

    void onResponse();

    void onFailure(Request request, IOException e);

}
