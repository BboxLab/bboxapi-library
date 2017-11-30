package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Channel;

public interface IBboxGetChannels {

    void onResponse(List<Channel> channels);

    void onFailure(Request request, int errorCode);

}
