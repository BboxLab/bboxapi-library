package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgSimple;

public interface IBboxGetEpgSimple {

    void onResponse(List<EpgSimple> epgSimple);

    void onFailure(Request request, int errorCode);

}
