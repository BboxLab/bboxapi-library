package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import java.util.List;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Application;

public interface IBboxGetApplications {

    void onResponse(List<Application> applications);

    void onFailure(Request request, int errorCode);

}
