package fr.bouyguestelecom.tv.bboxapi.callback;


import fr.bouyguestelecom.tv.bboxapi.model.MediaResource;

public interface IBboxMedia {

    void onNewMedia(MediaResource media);
}
