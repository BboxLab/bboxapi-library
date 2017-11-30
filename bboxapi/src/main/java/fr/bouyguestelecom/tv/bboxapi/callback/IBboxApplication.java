package fr.bouyguestelecom.tv.bboxapi.callback;


import fr.bouyguestelecom.tv.bboxapi.model.ApplicationResource;

public interface IBboxApplication {

    void onNewApplication(ApplicationResource application);
}
