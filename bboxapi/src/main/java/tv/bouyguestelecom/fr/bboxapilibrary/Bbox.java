package tv.bouyguestelecom.fr.bboxapilibrary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxDisplayToast;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetApplicationIcon;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetApplications;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannel;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannelListOnBox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetChannels;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetCurrentChannel;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetEpg;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetEpgSimple;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetListEpg;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetOpenedChannels;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetRecommendationTv;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSessionId;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetSpideoTv;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetToken;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxGetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMedia;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSendMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSetVolume;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStartApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxStopApplication;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxUnsubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Channel;
import tv.bouyguestelecom.fr.bboxapilibrary.model.ChannelProfil;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Epg;
import tv.bouyguestelecom.fr.bboxapilibrary.model.EpgMode;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Moment;
import tv.bouyguestelecom.fr.bboxapilibrary.model.Univers;
import tv.bouyguestelecom.fr.bboxapilibrary.util.ListenerList;
import tv.bouyguestelecom.fr.bboxapilibrary.util.Parser;
import tv.bouyguestelecom.fr.bboxapilibrary.ws.WebSocket;


public class Bbox implements IBbox {
    private static final String TAG = Bbox.class.getSimpleName();
    private static final String VERSION = "v1.3";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String URL_API_TOKEN = "https://api.bbox.fr/" + VERSION + "/security/token";
    private static final String URL_GET_RECO_TV = "https://api.bbox.fr/" + VERSION + "/media/live/recommendations";
    private static final String URL_GET_EPG = "https://api.bbox.fr/" + VERSION + "/media/live";
    private static final String URL_GET_EPG_SIMPLE = "https://api.bbox.fr/" + VERSION + "/media/live/epg";
    private static final String URL_GET_CHANNELS = "https://api.bbox.fr/" + VERSION + "/media/channels";

    private static final String URL_API_BOX = "http://@IP:8080/api.bbox.lan/v0";
    private static final String URL_SESSION_ID = URL_API_BOX + "/security/sessionId";
    private static final String URL_GET_APPLICATIONS = URL_API_BOX + "/applications";
    private static final String LOCAL_URL_GET_CHANNELS = URL_API_BOX + "/media/tvchannellist";
   // private static final String LOCAL_URL_GET_TODAY_EPG = URL_API_BOX + "/media/programs";
  //  private static final String LOCAL_URL_GET_EPG_BY_ID = URL_API_BOX + "/media/program";
    private static final String URL_GET_CURRENT_CHANNEL = URL_API_BOX + "/media";
    private static final String URL_REGISTER_APP = URL_API_BOX + "/applications/register";
    private static final String URL_NOTIFICATION = URL_API_BOX + "/notification";

    private static final String URL_START_APP = URL_API_BOX + "/applications";

    private static final String URL_DISPLAY_TOAST = URL_API_BOX + "/userinterface/toast";
    private static final String URL_VOLUME = URL_API_BOX + "/userinterface/volume";

    private static Bbox instance;

    private OkHttpClient mClient;

    private WebSocket mWebSocket;

    private String mSessionId;
    private String mToken;
    private Long mValidityToken = (long) -1;
    private Long mValiditySessionId = (long) -1;
    private boolean hasSecurity = true;


    private ListenerList<IBboxMedia> notifMedia = new ListenerList<>();
    private ListenerList<IBboxApplication> notifApps = new ListenerList<>();
    private ListenerList<IBboxMessage> notifMsg = new ListenerList<>();

    private static int ALLEPG = 0;
    private static int CURRENTEPG = 1;
    private static int SELECTEDEPG = 2;


    private Bbox() {
        mClient = new OkHttpClient();
        mClient.newBuilder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).connectionPool(new ConnectionPool(1, 5, TimeUnit.MINUTES));

    }

    public synchronized static Bbox getInstance() {
        if (instance == null)
            instance = new Bbox();

        return instance;
    }

    public Call getToken(String appId, String appSecret, final IBboxGetToken iBboxGetToken) {
        if (mValidityToken == -1 || mValidityToken <= System.currentTimeMillis()) {
            RequestBody body = RequestBody.create(JSON, buildJsonRequestToken(appId, appSecret));
            Request request = new Request.Builder()
                    .url(URL_API_TOKEN)
                    .post(body)
                    .build();

            Call call = mClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Get token failed", e);
                    iBboxGetToken.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                            && response.headers().get("x-token") != null
                            && !response.headers().get("x-token").isEmpty()) {
                        String tokenCloud = response.headers().get("x-token");
                        iBboxGetToken.onResponse(tokenCloud);
                        mValidityToken = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
                        mToken = tokenCloud;
                    } else {
                        iBboxGetToken.onFailure(call.request(), response.code());
                        Log.e(TAG, "Get token failed");
                    }

                    response.body().close();
                }
            });

            return call;
        } else {
            iBboxGetToken.onResponse(mToken);
            return null;
        }

    }


    public void getSessionId(final String ip, String appId, String appSecret, final IBboxGetSessionId iBboxGetSessionId) {
        if (hasSecurity) {
            getToken(appId, appSecret, new IBboxGetToken() {
                @Override
                public void onResponse(String token) {
                    Long currentTimeMillis = System.currentTimeMillis();

                    if (mValiditySessionId == -1 || mValiditySessionId <= currentTimeMillis) {
                        try {
                            RequestBody body = RequestBody.create(JSON, new JSONObject().put("token", token).toString());
                            Request request = new Request.Builder()
                                    .url(URL_SESSION_ID.replace("@IP", ip))
                                    .post(body)
                                    .build();
                            //Log.d(TAG, "url =  : " + URL_SESSION_ID.replace("@IP", ip));
                            Call call = mClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e(TAG, "Get sessionId failed", e);
                                    iBboxGetSessionId.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                                            && response.headers().get("x-sessionid") != null
                                            && !response.headers().get("x-sessionid").isEmpty()) {
                                        mSessionId = response.headers().get("x-sessionid");
                                        mValiditySessionId = System.currentTimeMillis() + 60 * 1000;
                                        iBboxGetSessionId.onResponse(mSessionId);
                                    } else { int responseCode = response.code();
                                        Log.w(TAG, "Cannot obtain bboxapi sessionId: "+responseCode);
                                        mSessionId = null;
                                        mValiditySessionId = (long) -1;
                                        iBboxGetSessionId.onFailure(call.request(), response.code());
                                    }

                                    response.body().close();
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "Cannot build getSessionId() request", e);
                            iBboxGetSessionId.onFailure(null, 500);
                        }
                    } else {
                        iBboxGetSessionId.onResponse(mSessionId);
                    }
                }

                @Override
                public void onFailure(Request request, int errorCode) {
                    mSessionId = null;
                    mValiditySessionId = (long) -1;
                    iBboxGetSessionId.onFailure(request, errorCode);
                }
            });
        } else {
            mSessionId = "855e4aa2f043550045c298e589220eca46769be82294f2bdda8345bffc160754";
            mValiditySessionId = System.currentTimeMillis() + 60 * 1000;
            iBboxGetSessionId.onResponse(mSessionId);
        }
    }

    @Override
    public void getChannel(String appId, String appSecret, final int epgChannelNumber, final IBboxGetChannel iBboxGetChannel) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_CHANNELS).newBuilder();
                urlBuilder.addPathSegment(String.valueOf(epgChannelNumber));

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannel.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() != HttpURLConnection.HTTP_OK)
                            iBboxGetChannel.onFailure(response.request(), response.code());

                        else
                            iBboxGetChannel.onResponse(Parser.parseJsonChannel(response.body().byteStream()));

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannel.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getChannels(final String ip, String appId, String appSecret, final IBboxGetChannels iBboxGetChannels) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                final Request request = new Request.Builder()
                        .url(LOCAL_URL_GET_CHANNELS.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "Get channels success");
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            List<Channel> channels = Parser.parseJsonChannels(response);
                            String logoUrlPrefix = URL_API_BOX.replace("@IP", ip);
                            for (Channel channel : channels) {
                                channel.setLogo(logoUrlPrefix + "/media/" + channel.getPositionId() + "/image");
                            }
                            iBboxGetChannels.onResponse(channels);
                        } else {
                            iBboxGetChannels.onFailure(response.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannels.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getChannels(String appId, String appSecret, final ChannelProfil profil, final IBboxGetChannels iBboxGetChannels) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_CHANNELS).newBuilder();
                urlBuilder.addQueryParameter("profil", profil.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .header("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "Get channels success");
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetChannels.onResponse(Parser.parseJsonChannels(response));
                        else
                            iBboxGetChannels.onFailure(response.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannels.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpg(String appId, String appSecret, final int epgChannelNumber, final int period, final String externalId, final int limit,
                       final int page, final EpgMode mode, final IBboxGetListEpg iBboxGetListEpg) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG).newBuilder();
                urlBuilder.addQueryParameter("period", String.valueOf(period));
                urlBuilder.addQueryParameter("epgChannelNumber", String.valueOf(epgChannelNumber));
                urlBuilder.addQueryParameter("externalId", String.valueOf(externalId));
                urlBuilder.addQueryParameter("limit", String.valueOf(limit));
                urlBuilder.addQueryParameter("page", String.valueOf(page));
                urlBuilder.addQueryParameter("mode", mode.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .addHeader("", "")
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetListEpg.onFailure(call.request(), HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetListEpg.onResponse(Parser.parseJsonListEpg(response));

                        else
                            iBboxGetListEpg.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });

            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetListEpg.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpgByEventId(String appId, String appSecret, final String eventId, final EpgMode mode, final IBboxGetEpg iBboxGetEpg) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG).newBuilder();
                urlBuilder.addPathSegment(eventId);
                urlBuilder.addQueryParameter("mode", mode.getValue());

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetEpg.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetEpg.onResponse(Parser.parseJsonEpg(response));

                        else
                            iBboxGetEpg.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpg.onFailure(request, errorCode);
            }
        });
    }

    /*public void getEpgByProgramId(final String ip, String appId, String appSecret, final String programId, final IBboxGetEpg iBboxGetEpg) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(LOCAL_URL_GET_EPG_BY_ID.replace("@IP", ip)).newBuilder();
                urlBuilder.addPathSegment(programId);

                final Request request = new Request.Builder()
                        .url(urlBuilder.build())
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetEpg.onResponse(Parser.parseJsonEpg(response));
                        else
                            iBboxGetEpg.onFailure(call.request(), response.code());

                        response.body().close();
                    }

                    public void onFailure(Call call, IOException e) {
                        iBboxGetEpg.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpg.onFailure(request, errorCode);
            }
        });
    }*/

    @Override
    public void getEpgSimple(String appId, String appSecret, final String startTime, final String endTime, final EpgMode mode, final IBboxGetEpgSimple iBboxGetEpgSimple) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_EPG_SIMPLE).newBuilder();
                urlBuilder.addQueryParameter("mode", mode.getValue());
                urlBuilder.addQueryParameter("startTime", startTime);
                urlBuilder.addQueryParameter("endTime", endTime);

                Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .addHeader("x-token", token)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        iBboxGetEpgSimple.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetEpgSimple.onResponse(Parser.parseJsonEpgSimple(response));

                        else
                            iBboxGetEpgSimple.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpgSimple.onFailure(request, errorCode);
            }
        });
    }

   /* @Override
    public void getTodayEpgFromBox(final String ip, String appId, String appSecret, final IBboxGetEpgSimple iBboxGetEpgSimple) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                final Request request = new Request.Builder()
                        .url(LOCAL_URL_GET_TODAY_EPG.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "Get epg success");
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            iBboxGetEpgSimple.onResponse(parseJsonEpgSimple(response));
                        } else {
                            iBboxGetEpgSimple.onFailure(response.request(), response.code());
                        }

                        response.body().close();
                    }

                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "getTodayEpgFromBox()", e);
                        iBboxGetEpgSimple.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetEpgSimple.onFailure(request, errorCode);
            }
        });
    }*/

    @Override
    public void getRecoTv(final String appId, final String appSecret, final String user, final Moment moment, final IBboxGetSpideoTv iBboxGetSpideoTv) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("user", user).toString());
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_RECO_TV).newBuilder();
                    urlBuilder.addPathSegment(moment.getValue());

                    Request request = new Request.Builder()
                            .url(urlBuilder.build().toString())
                            .post(body)
                            .addHeader("x-token", token)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            iBboxGetSpideoTv.onFailure(call.request(), HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_OK)
                                iBboxGetSpideoTv.onResponse(Parser.parseJsonListEpg(response));

                            else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                iBboxGetSpideoTv.onResponse(new ArrayList<Epg>());
                            } else
                                iBboxGetSpideoTv.onFailure(call.request(), response.code());

                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetSpideoTv.onFailure(request, errorCode);
            }
        });
    }
    @Override
    public void getRecommendationsTV(final String appId, final String appSecret, final String user, final Univers[] universes, final int limit,
                                     final IBboxGetRecommendationTv iBboxGetRecommendationTv) {
        getToken(appId, appSecret, new IBboxGetToken() {
            @Override
            public void onResponse(String token) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("user", user).toString());
                    HttpUrl.Builder urlBuilder = HttpUrl.parse(URL_GET_RECO_TV).newBuilder();

                    String pathSegment = "";

                    for (Univers univers : universes) {
                        pathSegment += univers.getValue() + ",";
                    }

                    pathSegment = pathSegment.substring(0, pathSegment.length() - 1);

                    urlBuilder.addPathSegment(pathSegment);

                    StringBuilder universesStr=new StringBuilder();
                    for (Univers univers : universes) {
                        universesStr.append(univers.getValue());
                        universesStr.append(',');
                    }
                    universesStr.deleteCharAt(universesStr.length()-1);
                    urlBuilder.addPathSegment(universesStr.toString());
                    urlBuilder.addQueryParameter("limit", String.valueOf(limit));

                    Request request = new Request.Builder()
                            .url(urlBuilder.build().toString())
                            .post(body)
                            .addHeader("x-token", token)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            iBboxGetRecommendationTv.onFailure(call.request(), HttpURLConnection.HTTP_INTERNAL_ERROR);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_OK)
                                iBboxGetRecommendationTv.onResponse(Parser.parseJsonListEpg(response));

                            else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                iBboxGetRecommendationTv.onResponse(new ArrayList<Epg>());
                            } else
                                iBboxGetRecommendationTv.onFailure(call.request(), response.code());

                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    iBboxGetRecommendationTv.onFailure(null, 500);
                }
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetRecommendationTv.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getApps(final String ip, String appId, String appSecret, final IBboxGetApplications iBboxGetApplications) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip));
                if (sessionId != null) {
                    requestBuilder.header("x-sessionid", sessionId);
                }
                final Request request = requestBuilder.build();
                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplications.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetApplications.onResponse(Parser.parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

                        else
                            iBboxGetApplications.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetApplications.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getCurrentChannel(final String ip, String appId, String appSecret, final IBboxGetCurrentChannel iBboxGetCurrentChannel) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                //Log.v(TAG, "SessionId ==> " + sessionId);
                final Request request = new Request.Builder()
                        .url(URL_GET_CURRENT_CHANNEL.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetCurrentChannel.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetCurrentChannel.onResponse(Parser.parseJsonChannel(response.body().byteStream()));

                        else
                            iBboxGetCurrentChannel.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetCurrentChannel.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void registerApp(final String ip, String appId, String appSecret, final String appName, final IBboxRegisterApp iBboxRegisterApp) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                try {
                    RequestBody body = RequestBody.create(JSON, new JSONObject().put("appName", appName).toString());
                    final Request request = new Request.Builder()
                            .url(URL_REGISTER_APP.replace("@IP", ip))
                            .header("x-sessionid", sessionId)
                            .post(body)
                            .build();

                    Call call = mClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "Register app failed", e);
                            iBboxRegisterApp.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT
                                    && response.headers().get("Location") != null
                                    && !response.headers().get("Location").isEmpty()) {
                                //Log.i(TAG, "Register app success");
                                String location = response.headers().get("Location");
                                String appId = location.substring(location.lastIndexOf('/') + 1);
                                iBboxRegisterApp.onResponse(appId);
                            } else {
                                //Log.i(TAG, "Register app failed");
                                iBboxRegisterApp.onFailure(call.request(), response.code());
                            }

                            response.body().close();
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Error occured", e);
                }
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxRegisterApp.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void subscribeNotification(final String ip, String appId, String appSecret, final String appRegisterId, final String ressourceId,
                                      final IBboxSubscribe iBboxSubscribe) {
        //notification_body='{"appId":"'"${appId}"'","resources" : [ {"resourceId":"Iot"} ] }'
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                IBboxSubscribe bothSubscribe = iBboxSubscribe;
                if (mWebSocket == null || mWebSocket.isClosed()) {
                    bothSubscribe = new IBboxSubscribe() {
                        CountDownLatch countDown = new CountDownLatch(2);

                        @Override
                        public void onSubscribe() {
                            countDown.countDown();
                            if (countDown.getCount() == 0) {
                                iBboxSubscribe.onSubscribe();
                            }
                        }

                        @Override
                        public void onFailure(Request request, int errorCode) {
                            iBboxSubscribe.onFailure(request, errorCode);
                        }
                    };
                    mWebSocket = new WebSocket(ip, appRegisterId, bothSubscribe);
                }
                RequestBody body = RequestBody.create(JSON, buildJsonRequestNotif(appRegisterId, ressourceId));
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();

                Call call = mClient.newCall(request);
                final IBboxSubscribe finalBboxSubscribe = bothSubscribe;
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Subscribe Notification failed", e);
                        finalBboxSubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Subscribe Notification success");
                            finalBboxSubscribe.onSubscribe();
                        } else {
                            //Log.i(TAG, "Subscribe Notification failed");
                            finalBboxSubscribe.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSubscribe.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public String addListener(String ip, final String appId, IBboxMedia iBboxMedia) {
        return addNotifChannelListener(iBboxMedia);
    }

    @Override
    public String addListener(String ip, String appId, IBboxApplication iBboxApplication) {
        return addNotifApplication(iBboxApplication);
    }

    @Override
    public String addListener(String ip, String appId, IBboxMessage iBboxMessage) {
        return addNotifMessage(iBboxMessage);
    }

    @Override
    public void removeMediaListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId, null);

        if (getNotifMedia() != null) {
            removeNotifChannelListener(channelListenerId);

            if (getNotifMedia().size() == 0)
                mWebSocketBis.close();
        }
    }

    @Override
    public void removeAppListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId, null);

        if (getNotifApps() != null) {
            removeNotifApps(channelListenerId);

            if (getNotifApps().size() == 0)
                mWebSocketBis.close();
        }
    }

    @Override
    public void removeMsgListener(String ip, String appId, String channelListenerId) {
        WebSocket mWebSocketBis = new WebSocket(ip, appId, null);

        if (getNotifMsg() != null) {
            removeNotifMsg(channelListenerId);

            if (getNotifMsg().size() == 0)
                mWebSocketBis.close();
        }
    }



    private String buildJsonRequestToken(String appId, String appSecret) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("appId", appId);
            jObject.put("appSecret", appSecret);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot build token request", e);
        }
        return jObject.toString();
    }


    private String buildJsonRequestNotif(String appResgisterId, String ressourceId) {
        JSONObject jObject = new JSONObject();
        JSONArray ressouces = new JSONArray();
        JSONObject resourceChannel = new JSONObject();

        try {
            resourceChannel.put("resourceId", ressourceId);
            ressouces.put(resourceChannel);
            jObject.put("appId", appResgisterId);
            jObject.put("resources", ressouces);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot build notif request", e);
        }
        return jObject.toString();
    }

    @Override
    public void startApp(final String ip, String appId, String appSecret,
                         final String packageName,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Start app failed before response", e);
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Start app failed "+response.code());
                            iBboxStartApplication.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStartApplication.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void startApp(final String ip, String appId, String appSecret,
                         final String packageName, final String deeplink,
                         final IBboxStartApplication iBboxStartApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestDeeplink(deeplink));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Start app failed before response", e);
                        iBboxStartApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Start app success");
                            iBboxStartApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Start app failed "+response.code());
                            iBboxStartApplication.onFailure(call.request(), response.code());
                        }
                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStartApplication.onFailure(request, errorCode);
            }
        });
    }

    private String buildJsonRequestDeeplink(String deeplink) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("intent", deeplink);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }

    private String buildJsonRequestToast(String msg, String color, String duration, String x, String y) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("message", msg);
            jObject.put("color", color);
            jObject.put("duration", duration);
            jObject.put("pos_x", x);
            jObject.put("pos_y", y);
        } catch (JSONException e) {
            Log.e(TAG, "Error occured", e);
        }
        return jObject.toString();
    }

    @Override
    public void displayToast(final String ip, String appId, String appSecret,
                             final String msg, final String color, final String duration,
                             final String pos_x, final String pos_y,
                             final IBboxDisplayToast iBboxDisplayToast) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_DISPLAY_TOAST;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestToast(msg, color, duration, pos_x, pos_y));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Display toast failed", e);
                        iBboxDisplayToast.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Display toast success");
                            iBboxDisplayToast.onResponse();
                        } else {
                            //Log.i(TAG, "Display toast failed");
                            iBboxDisplayToast.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxDisplayToast.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void stopApp(final String ip, String appId, String appSecret,
                        final String packageName,
                        final IBboxStopApplication iBboxStopApplication) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_START_APP + "/" + packageName;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Stop app failed before response", e);
                        iBboxStopApplication.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Stop app success");
                            iBboxStopApplication.onResponse();
                        } else {
                            //Log.i(TAG, "Stop app failed");
                            iBboxStopApplication.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxStopApplication.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void setVolume(final String ip, String appId, String appSecret,
                          final String volume,
                          final IBboxSetVolume iBboxSetVolume) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestVolume(volume));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //Log.e(TAG, "Set volume failed", e);
                        iBboxSetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Set volume success");
                            iBboxSetVolume.onResponse();
                        } else {
                            //Log.i(TAG, "Set volume failed");
                            iBboxSetVolume.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSetVolume.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getVolume(final String ip, String appId, String appSecret,
                          final IBboxGetVolume iBboxGetVolume) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_VOLUME;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Get volume failed", e);
                        iBboxGetVolume.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            //Log.i(TAG, "Get volume success");
                            String volume = response.body().string();
                            iBboxGetVolume.onResponse(volume);
                        } else {
                            //Log.i(TAG, "Get volume failed");
                            iBboxGetVolume.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetVolume.onFailure(request, errorCode);
            }
        });
    }


    private String buildJsonRequestVolume(String volume) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("volume", volume);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


    @Override
    public void getChannelListOnBox(final String ip, String appId, String appSecret,
                                    final IBboxGetChannelListOnBox iBboxGetChannelListOnBox) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {

                String url = LOCAL_URL_GET_CHANNELS;
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetChannelListOnBox.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() != HttpURLConnection.HTTP_OK)
                            iBboxGetChannelListOnBox.onFailure(response.request(), response.code());

                        else
                            iBboxGetChannelListOnBox.onResponse(Parser.parseJsonChannelsLight(response));
                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetChannelListOnBox.onFailure(request, errorCode);
            }
        });
    }


    @Override
    public void getAppInfo(final String ip, String appId, String appSecret, final String packageName, final IBboxGetApplications iBboxGetApplications) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip) + "/" + packageName)
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplications.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK)
                            iBboxGetApplications.onResponse(Parser.parseJsonApplications(URL_API_BOX.replace("@IP", ip), response));

                        else
                            iBboxGetApplications.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetApplications.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getAppIcon(final String ip, String appId, String appSecret, final String packageName, final IBboxGetApplicationIcon iBboxGetApplicationIcon) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_GET_APPLICATIONS.replace("@IP", ip) + "/" + packageName + "/image")
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        iBboxGetApplicationIcon.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            InputStream in = response.body().byteStream();
                            Bitmap bmp = BitmapFactory.decodeStream(in);
                            iBboxGetApplicationIcon.onResponse(bmp);
                        } else
                            iBboxGetApplicationIcon.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                iBboxGetApplicationIcon.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getOpenedChannels(final String ip, String appId, String appSecret, final IBboxGetOpenedChannels iBboxGetOpenedChannels) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            public void onResponse(final String sessionId) {
                final Request request = new Request.Builder()
                        .url(URL_NOTIFICATION.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .build();

                Call mCall = mClient.newCall(request);
                mCall.enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Get notification channels failed", e);
                        iBboxGetOpenedChannels.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            //Log.i(TAG, "Get notification channels success");
                            iBboxGetOpenedChannels.onResponse(Parser.parseJsonOpenedChannel(response));
                        } else
                            iBboxGetOpenedChannels.onFailure(call.request(), response.code());

                        response.body().close();
                    }
                });
            }

            public void onFailure(Request request, int errorCode) {
                //Log.i(TAG, "Get notification channels failed");
                iBboxGetOpenedChannels.onFailure(request, errorCode);
            }
        });
    }



    @Override
    public void unsubscribeNotification(final String ip, String appId, String appSecret, final String channelId,
                                        final IBboxUnsubscribe iBboxUnsubscribe) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelId;
                RequestBody body = RequestBody.create(JSON, "");
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .delete(body)
                        .build();

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Unsubscribe Notification failed", e);
                        iBboxUnsubscribe.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Unsubscribe Notification success");
                            iBboxUnsubscribe.onUnsubscribe();
                        } else {
                            //Log.i(TAG, "Subscribe Notification failed");
                            iBboxUnsubscribe.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxUnsubscribe.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void sendMessage(final String ip, final String appId, String appSecret,
                            final String channelIdOrRoomName, final String appIdFromRegister, final String msgToSend,
                            final IBboxSendMessage iBboxSendMessage) {
        getSessionId(ip, appId, appSecret, new IBboxGetSessionId() {
            @Override
            public void onResponse(String sessionId) {
                String url = URL_NOTIFICATION + "/" + channelIdOrRoomName;
                RequestBody body = RequestBody.create(JSON, buildJsonRequestMessage(appIdFromRegister, msgToSend));
                final Request request = new Request.Builder()
                        .url(url.replace("@IP", ip))
                        .header("x-sessionid", sessionId)
                        .post(body)
                        .build();
                //Log.d(TAG, request.toString());

                Call call = mClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Send message failed before response", e);
                        iBboxSendMessage.onFailure(call.request(), HttpURLConnection.HTTP_BAD_REQUEST);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                            //Log.i(TAG, "Send message success");
                            iBboxSendMessage.onResponse();
                        } else {
                            //Log.i(TAG, "Send message failed "+response.code());
                            iBboxSendMessage.onFailure(call.request(), response.code());
                        }

                        response.body().close();
                    }
                });
            }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxSendMessage.onFailure(request, errorCode);
            }
        });
    }




    /*
    @epgChannelNumber set to null if @typeEpg = 0 or 1
    @typeEpg 0 all
             1 current channel
             2 list epg need to set epgChannelNumber
    */

    @Override
    public void SearchEpgBySummary(String appid, String appSecret, final String token, final String period, final String profil, final int typeEpg, final String epgChannelNumber, final String longSummary) {

        Bbox.getInstance().getCurrentChannel("127.0.0.1", appid, appSecret, new IBboxGetCurrentChannel() {
            @Override
            public void onResponse(Channel channel) {
                Log.v(TAG, "Get current channels response");

                if (channel != null
                        && "play".equals(channel.getMediaState())) {
                    Log.v(TAG, "curent channel ==>  " + channel.toString());
                    getChanel(token, profil, channel.getName(), period, typeEpg, epgChannelNumber, longSummary);
                }
            }

            @Override
            public void onFailure(Request request, int i) {
                Log.e(TAG, "Get current channels failure " + i);
            }
        });
    }

    private static void getChanel(final String token, final String profil, String name, final String period, final int typeEpg, final String epgChannelNumber, final String longSummary) {
        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.bbox.fr/v1.3/media/channels").newBuilder();
        urlBuilder.addQueryParameter("profil", profil);
        urlBuilder.addQueryParameter("name", name);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .addHeader("x-token", token)
                .build();

        Call call = httpClient.newCall(request);

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Get channels failure");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.v(TAG, "Get channels response");

                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.v(TAG, "Get channels success");
                    List<Channel> channels = Parser.parseChannels(response);

                    if (!channels.isEmpty()) {
                        Log.v(TAG, "channel ==> " + channels.get(0));

                        if (typeEpg == ALLEPG)
                            getDetailProgram(token, period, profil, null, longSummary);
                        else if (typeEpg == CURRENTEPG)
                            getDetailProgram(token, period, profil, String.valueOf(channels.get(0).getEpgChannelNumber()), longSummary);

                        else if (typeEpg == SELECTEDEPG)
                            getDetailProgram(token, period, profil, epgChannelNumber, longSummary);

                    }
                }
            }
        });
    }

    private static void getDetailProgram(String token, String period, String profil, String epgChannelNumber, String longSummary) {
        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.bbox.fr/v1.3/media/live").newBuilder();
        urlBuilder.addQueryParameter("period", period);
        urlBuilder.addQueryParameter("profil", profil);
        if (epgChannelNumber != null && !epgChannelNumber.isEmpty())
            urlBuilder.addQueryParameter("epgChannelNumber", epgChannelNumber);
        urlBuilder.addQueryParameter("longSummary", longSummary);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .addHeader("x-token", token)
                .build();

        Call call = httpClient.newCall(request);

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Get getDetailProgram failure");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.v(TAG, "Get getDetailProgram response");

                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.v(TAG, "Get getDetailProgram success = " + response.toString());

                    List<Epg> detailEpg = Parser.parseJsonListEpg(response);

                    if (!detailEpg.isEmpty()) {
                        Log.v(TAG, "getDetailProgram ==> " + detailEpg.get(0).getProgramInfo().getLongTitle());
                        Log.v(TAG, "getDetailProgram ==> " + detailEpg.get(0).getProgramInfo().getLongSummary());
                        Log.v(TAG, "getDetailProgram ==> " + detailEpg.get(0).getEpgChannelNumber());

                    }
                }
            }
        });
    }




    private String buildJsonRequestMessage(String appId, String msg) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("appId", appId);
            jObject.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
    }


    public String addNotifChannelListener(IBboxMedia iChannelListener) {
        return notifMedia.add(iChannelListener);
    }

    public void removeNotifChannelListener(String iMediaListenerId) {
        notifMedia.remove(iMediaListenerId);
    }

    public String addNotifApplication(IBboxApplication iAppListenerId) {
        return notifApps.add(iAppListenerId);
    }

    public void removeNotifApps(String iAppListenerId) {
        notifApps.remove(iAppListenerId);
    }

    public String addNotifMessage(IBboxMessage iMsgListenerId) {
        return notifMsg.add(iMsgListenerId);
    }

    public void removeNotifMsg(String iMsgListenerId) {
        notifMsg.remove(iMsgListenerId);
    }

    public ListenerList<IBboxMedia> getNotifMedia() {
        return notifMedia;
    }

    public ListenerList<IBboxApplication> getNotifApps() {
        return notifApps;
    }

    public ListenerList<IBboxMessage> getNotifMsg() {
        return notifMsg;
    }
}