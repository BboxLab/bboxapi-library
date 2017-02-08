package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;

public interface IBboxGetListEpg {

    void onResponse(List<Epg> epgs);

    void onFailure(Request request, int errorCode);

}
