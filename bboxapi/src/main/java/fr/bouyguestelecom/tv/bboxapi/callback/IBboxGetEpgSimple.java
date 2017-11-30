package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.EpgSimple;

public interface IBboxGetEpgSimple {

    void onResponse(List<EpgSimple> epgSimple);

    void onFailure(Request request, int errorCode);

}
