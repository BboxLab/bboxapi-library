package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Epg;

public interface IBboxGetRecommendationTv {

    void onResponse(List<Epg> epgList);

    void onFailure(Request request, int errorCode);

}
