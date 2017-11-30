package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.ChannelLight;

public interface IBboxGetChannelListOnBox {

    void onResponse(List<ChannelLight> channels);

    void onFailure(Request request, int errorCode);

}
