package fr.bouyguestelecom.tv.bboxapi;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannel;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetChannels;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetEpg;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetListEpg;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetRecommendationTv;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetSpideoTv;
import fr.bouyguestelecom.tv.bboxapi.callback.IBboxGetToken;
import fr.bouyguestelecom.tv.bboxapi.model.ChannelProfil;
import fr.bouyguestelecom.tv.bboxapi.model.Epg;
import fr.bouyguestelecom.tv.bboxapi.model.EpgMode;
import fr.bouyguestelecom.tv.bboxapi.model.Moment;
import fr.bouyguestelecom.tv.bboxapi.model.Univers;
import fr.bouyguestelecom.tv.bboxapi.util.Parser;

/**
 * Created by tao on 30/11/17.
 */

public class BboxCloud implements IBboxCloud {
    private static final String TAG = BboxCloud.class.getSimpleName();
    private static final String VERSION = "v1.3";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String URL_API_TOKEN = "https://api.bbox.fr/" + VERSION + "/security/token";
    private static final String URL_GET_RECO_TV = "https://api.bbox.fr/" + VERSION + "/media/live/recommendations";
    private static final String URL_GET_EPG = "https://api.bbox.fr/" + VERSION + "/media/live";
    private static final String URL_GET_EPG_SIMPLE = "https://api.bbox.fr/" + VERSION + "/media/live/epg";
    private static final String URL_GET_CHANNELS = "https://api.bbox.fr/" + VERSION + "/media/channels";

    private static BboxCloud instance;

    private final String appId;
    private final String appSecret;

    private OkHttpClient mClient;
    private String mToken;
    private Long mValidityToken = (long) -1;
    private Long mValiditySessionId = (long) -1;

    private BboxCloud(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(1, 5, TimeUnit.MINUTES))
                .build();
    }

    public static synchronized BboxCloud getInstance(String appId, String appSecret) {
        if (instance == null) {
            instance = new BboxCloud(appId, appSecret);
        }
        return instance;
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

    public Call getToken(final IBboxGetToken iBboxGetToken) {
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

    @Override
    public void getChannel(final int epgChannelNumber, final IBboxGetChannel iBboxGetChannel) {
        getToken(new IBboxGetToken() {
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
    public void getChannels(final ChannelProfil profil, final IBboxGetChannels iBboxGetChannels) {
        getToken(new IBboxGetToken() {
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
    public void getRecoTv(final String user, final Moment moment, final IBboxGetSpideoTv iBboxGetSpideoTv) {
        getToken(new IBboxGetToken() {
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
    public void getRecommendationsTV(final String user, final Univers[] universes, final int limit,
                                     final IBboxGetRecommendationTv iBboxGetRecommendationTv) {
        getToken(new IBboxGetToken() {
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

                    StringBuilder universesStr = new StringBuilder();
                    for (Univers univers : universes) {
                        universesStr.append(univers.getValue());
                        universesStr.append(',');
                    }
                    universesStr.deleteCharAt(universesStr.length() - 1);
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
                }           }

            @Override
            public void onFailure(Request request, int errorCode) {
                iBboxGetRecommendationTv.onFailure(request, errorCode);
            }
        });
    }

    @Override
    public void getEpg(final int epgChannelNumber, final int period, final String externalId, final int limit,
                       final int page, final EpgMode mode, final IBboxGetListEpg iBboxGetListEpg) {
        getToken(new IBboxGetToken() {
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
    public void getEpgByEventId(final String eventId, final EpgMode mode, final IBboxGetEpg iBboxGetEpg) {
        getToken(new IBboxGetToken() {
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
}
