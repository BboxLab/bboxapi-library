package fr.bouyguestelecom.tv.bboxapi;

import fr.bouyguestelecom.tv.bboxapi.callback.IBboxApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxDisplayToast;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetApplicationIcon;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetApplications;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannelListOnBox;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetCurrentChannel;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetOpenedChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetSessionId;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetVolume;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxMedia;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxMessage;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxRegisterApp;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSendMessage;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSetVolume;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxStartApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxStopApplication;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxSubscribe;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxUnsubscribe;

public interface IBbox {

    /*

    void getToken(IBboxGetToken iBboxGetToken);

    */

    void getSessionId(IBboxGetSessionId iBboxGetSessionId);

    void getLocalChannels(final IBboxGetChannels iBboxGetChannels);

    void getApps(IBboxGetApplications iBboxGetApplications);

    void getCurrentChannel(IBboxGetCurrentChannel iBboxGetCurrentChannel);

    void registerApp(String appName, IBboxRegisterApp iBboxRegisterApp);

    void subscribeNotification(String appRegisterId, String ressourceId, IBboxSubscribe iBboxSubscribe);

    void removeMediaListener(String appId, String channelListenerId);

    void removeAppListener(String appId, String channelListenerId);

    void removeMsgListener(String appId, String channelListenerId);

    String addListener(IBboxMedia iBboxMedia);

    String addListener(IBboxApplication iBboxApplication);

    String addListener(IBboxMessage iBboxMessage);

    void startApp(String packageName, IBboxStartApplication iBboxStartApplication);

    void startApp(String packageName, String deeplink, IBboxStartApplication iBboxStartApplication);

    void stopApp(String packageName, IBboxStopApplication iBboxStopApplication);

    void displayToast(final String msg, final String color, final String duration,
                      final String pos_x, final String pos_y,
                      final IBboxDisplayToast iBboxDisplayToast);

    void setVolume(final String volume,
                   final IBboxSetVolume iBboxSetVolume);

    void getVolume(final IBboxGetVolume iBboxGetVolume);

    void getChannelListOnBox(final IBboxGetChannelListOnBox iBboxGetChannelListOnBox);

    void getAppInfo(String packageName, IBboxGetApplications iBboxGetApplications);

    void getAppIcon(String packageName, IBboxGetApplicationIcon iBboxGetApplicationIcon);

    void getOpenedChannels(IBboxGetOpenedChannels iBboxGetOpenedChannels);

    void unsubscribeNotification(String channelId, IBboxUnsubscribe iBboxUnsubscribe);

    void sendMessage(final String channelIdOrRoomName, final String appIdFromRegister, final String msgToSend,
                     final IBboxSendMessage iBboxSendMessage);

    /*
    void getApps(String ip, String appId, String appSecret, IBboxGetApplications iBboxGetApplications);

    void getUrlLogoChannel(String ip, String appId, String appSecret, int channel, IBboxGetLogoChannel IBboxGetLogoChannel);

    void registerApp( String ip, String appId, String appSecret,  String appName,  IBboxRegisterApp iBboxRegisterApp);

    void subscribeNotifChannel( String ip,  String appId, String appSecret,  String registerApp,  IBboxSubscribe iBboxSubscribe);

    String addNotifChannelListener(IBboxMedia iChannelListener);

    void removeNotifChannelListener(String channelListenerId);

    void cancelRequest();
    */
}
