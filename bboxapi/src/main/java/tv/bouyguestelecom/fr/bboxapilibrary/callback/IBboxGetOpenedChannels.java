package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import java.util.List;

import okhttp3.Request;

public interface IBboxGetOpenedChannels {

    void onResponse(List<String> channels);

    void onFailure(Request request, int errorCode);
}
