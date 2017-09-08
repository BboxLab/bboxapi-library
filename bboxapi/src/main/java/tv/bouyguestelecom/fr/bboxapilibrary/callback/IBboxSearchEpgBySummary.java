package tv.bouyguestelecom.fr.bboxapilibrary.callback;

import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;

/**
 * Created by rmessara on 08/09/17.
 * bboxapi-library
 */

public interface IBboxSearchEpgBySummary {

    void onResponse(List<Epg> epgList);

    void onFailure(Request request, int errorCode);
}
