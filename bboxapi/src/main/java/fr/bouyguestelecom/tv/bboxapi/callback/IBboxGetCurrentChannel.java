package fr.bouyguestelecom.tv.bboxapi.callback;


import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Channel;

public interface IBboxGetCurrentChannel {

    void onResponse(Channel channel);

    void onFailure(Request request, int errorCode);

}
