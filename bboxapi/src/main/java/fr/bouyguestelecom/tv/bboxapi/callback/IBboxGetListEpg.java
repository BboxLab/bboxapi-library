package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Epg;

public interface IBboxGetListEpg {

    void onResponse(List<Epg> epgs);

    void onFailure(Request request, int errorCode);

}
