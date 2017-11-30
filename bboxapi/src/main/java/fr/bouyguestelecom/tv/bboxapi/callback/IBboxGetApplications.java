package fr.bouyguestelecom.tv.bboxapi.callback;


import java.util.List;

import okhttp3.Request;
import fr.bouyguestelecom.tv.bboxapi.model.Application;

public interface IBboxGetApplications {

    void onResponse(List<Application> applications);

    void onFailure(Request request, int errorCode);

}
