package fr.bouyguestelecom.tv.bboxapi.callback;

import fr.bouyguestelecom.tv.bboxapi.model.Iot;

/**
 * Created by rmessara on 30/01/17.
 * bboxapi-library
 */

public interface IBboxIot {
    void onNewApplication(Iot iot);
}
