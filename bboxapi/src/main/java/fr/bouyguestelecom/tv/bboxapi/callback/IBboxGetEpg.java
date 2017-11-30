package fr.bouyguestelecom.tv.bboxapi.callback;


import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Epg;

public interface IBboxGetEpg {

    void onResponse(Epg epg);

    void onFailure(Request request, int errorCode);

}
