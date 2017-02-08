package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;

public interface IBboxGetCurrentChannel {

    void onResponse(Channel channel);

    void onFailure(Request request, int errorCode);

}
