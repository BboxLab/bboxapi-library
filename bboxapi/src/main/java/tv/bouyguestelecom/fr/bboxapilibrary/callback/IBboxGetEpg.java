package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;

public interface IBboxGetEpg {

    void onResponse(Epg epg);

    void onFailure(Request request, int errorCode);

}
