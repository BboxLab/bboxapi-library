package tv.bouyguestelecom.fr.bboxapilibrary.callback;


import tv.bouyguestelecom.fr.bboxapilibrary.model.MessageResource;

public interface IBboxMessage {

    void onNewMessage(MessageResource message);
}
