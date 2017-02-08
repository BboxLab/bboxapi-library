package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelLight;

public interface IBboxGetChannelListOnBox {

    void onResponse(List<ChannelLight> channels);

    void onFailure(Request request, int errorCode);

}
