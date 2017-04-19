package tv.bouyguestelecom.fr.bboxapilibrary.ws;

import android.util.JsonReader;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import tv.bouyguestelecom.fr.bboxapilibrary.Bbox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMedia;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ApplicationResource;
import tv.bouyguestelecom.fr.bboxapilibrary.model.MediaResource;
import tv.bouyguestelecom.fr.bboxapilibrary.model.MessageResource;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Resource;

public class WebSocket {
    private static final String TAG = WebSocket.class.getSimpleName();
    private static final String WEBSOCKET_PREFIX = "ws://";
    private static final String WEBSOCKET_PORT = "9090";
    private static WebSocket instance = null;
    private WebSocketClient mWebSocketClient;
    private Bbox bbox;



    private String mAppId;
    private String mWebsocketAddress;

    public WebSocket(String ip, String appId) {
        mAppId = appId;
        mWebsocketAddress = WEBSOCKET_PREFIX + ip + ":" + WEBSOCKET_PORT;
        init();
    }

    public static WebSocket getInstance(String appId, Bbox bbox) {
        if(instance == null) {
            instance = new WebSocket(appId, appId);
        }

        return instance;
    }

    private void init() {
        mWebSocketClient = new WebSocketClient(URI.create(mWebsocketAddress)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                mWebSocketClient.send(mAppId);
            }

            @Override
            public void onMessage(String message) {
                try {
                    InputStream stream = new ByteArrayInputStream(message.getBytes());
                    JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
                    Resource resource = new Resource(reader);

                    JSONObject obj = new JSONObject(message);

                    if (resource.getResourceId().equals("Media")) {
                        MediaResource mediaResource = new MediaResource(new JSONObject(resource.getBody()));

                        for (Map.Entry<String, IBboxMedia> iChannelListenerEntry : Bbox.getInstance().getNotifMedia().getMap().entrySet()) {
                            iChannelListenerEntry.getValue().onNewMedia(mediaResource);
                        }
                    }

                    if (obj.get("resourceId").toString().equals("Application")) {

                        ApplicationResource appResource = new ApplicationResource(obj.getJSONObject("body").get("packageName").toString(),
                                obj.getJSONObject("body").get("state").toString());

                        for (Map.Entry<String, IBboxApplication> iAppListenerEntry : Bbox.getInstance().getNotifApps().getMap().entrySet()) {
                            iAppListenerEntry.getValue().onNewApplication(appResource);
                        }
                    }

                    if (obj.get("resourceId").toString().contains("Message") || obj.get("resourceId").toString().contains(".")) {

                        MessageResource messageResource = new MessageResource(obj.getJSONObject("body").get("source").toString(),
                                obj.getJSONObject("body").get("message").toString());

                        for (Map.Entry<String, IBboxMessage> iMsgListenerEntry : Bbox.getInstance().getNotifMsg().getMap().entrySet()) {
                            iMsgListenerEntry.getValue().onNewMessage(messageResource);
                        }
                    }

                    if (obj.get("resourceId").toString().contains("Iot")) {

                        MessageResource messageResource = new MessageResource(obj.getJSONObject("body").get("source").toString(),
                                obj.getJSONObject("body").get("message").toString());

                        for (Map.Entry<String, IBboxMessage> iMsgListenerEntry : Bbox.getInstance().getNotifMsg().getMap().entrySet()) {
                            iMsgListenerEntry.getValue().onNewMessage(messageResource);
                        }
                    }

                } catch (UnsupportedEncodingException | JSONException e) {
                    Log.e(TAG, "Error occurred", e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i(TAG, "onClose");
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError");
            }
        };

        mWebSocketClient.connect();
    }

    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }



}