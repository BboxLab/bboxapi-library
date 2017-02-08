package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;

public interface IBboxGetChannels {

    void onResponse(List<Channel> channels);

    void onFailure(Request request, int errorCode);

}
