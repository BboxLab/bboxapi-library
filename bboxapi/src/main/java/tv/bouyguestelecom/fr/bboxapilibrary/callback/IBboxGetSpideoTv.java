package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;

public interface IBboxGetSpideoTv {

    void onResponse(List<Epg> epgList);

    void onFailure(Request request, int errorCode);

}
