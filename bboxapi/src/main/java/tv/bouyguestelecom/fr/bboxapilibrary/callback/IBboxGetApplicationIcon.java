package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import android.graphics.Bitmap;

import okhttp3.Request;

public interface IBboxGetApplicationIcon {

    void onResponse(Bitmap img);

    void onFailure(Request request, int errorCode);

}
