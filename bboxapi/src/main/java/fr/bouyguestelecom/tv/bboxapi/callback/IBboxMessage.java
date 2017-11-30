package fr.bouyguestelecom.tv.bboxapi.callback;


import fr.bouyguestelecom.tv.bboxapi.model.MessageResource;

public interface IBboxMessage {

    void onNewMessage(MessageResource message);
}
