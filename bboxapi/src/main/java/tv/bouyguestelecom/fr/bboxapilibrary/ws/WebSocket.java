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
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
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
    private final IBboxSubscribe iBboxSubscribe;

    public WebSocket(String ip, String appId, IBboxSubscribe iBboxSubscribe) {
        mAppId = appId;
        mWebsocketAddress = WEBSOCKET_PREFIX + ip + ":" + WEBSOCKET_PORT;
        this.iBboxSubscribe = iBboxSubscribe;
        init();
    }

    private void init() {
        mWebSocketClient = new WebSocketClient(URI.create(mWebsocketAddress)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                mWebSocketClient.send(mAppId);
                if (iBboxSubscribe != null) {
                    iBboxSubscribe.onSubscribe();
                }
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

                        JSONObject body = obj.getJSONObject("body");
                        MessageResource messageResource = new MessageResource(body.get("source").toString(),
                                body.getString("message"));

                        for (IBboxMessage msg : Bbox.getInstance().getNotifMsg().getMap().values()) {
                            msg.onNewMessage(messageResource);
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
                    Log.e(TAG, "Error occured", e);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i(TAG, "onClose");
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError", ex);
            }
        };

        mWebSocketClient.connect();
    }

    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    public boolean isClosed() {
        if (mWebSocketClient != null) {
            org.java_websocket.WebSocket realWebSocket = mWebSocketClient.getConnection();
            if (realWebSocket != null) {
                return realWebSocket.isClosed();
            }
        }
        return true;
    }
}