package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import okhttp3.Request;

/**
 * Created by rmessara on 15/09/17.
 * bboxapi-library
 */

public interface IBboxGetEpgChannelNumber {

    void onResponse(int epgChannelNumber);

    void onFailure(Request request, int errorCode);
}
