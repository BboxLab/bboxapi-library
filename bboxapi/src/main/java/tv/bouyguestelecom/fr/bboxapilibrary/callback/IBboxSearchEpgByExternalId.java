package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;

/**
 * Created by rmessara on 11/09/17.
 * bboxapi-library
 */

public interface IBboxSearchEpgByExternalId {

    void onResponse(List<Epg> epgList);

    void onFailure(Request request, int errorCode);
}

