package controllers;

import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skircher on 8/19/14.
 */
public class EdgeProxy extends Controller {

    protected static Map<String, CachedResponse> _responseCache = new HashMap<String, CachedResponse>();
    //    public static String PROXIED_HOST = "www.cnn.com";
    public static String PROXIED_HOST = "https://st-dev.optiolabshq.com";
    //public static String PROXIED_HOST = "http://localhost:9001";

    public static void initEdgeProxy() {
        _responseCache.put("GET " + PROXIED_HOST + "/time", new CachedResponse("GET " + PROXIED_HOST + "/time", "{\"name\":\"tipsy\"}", Http.Status.OK, "application/json"));
        _responseCache.put("PUT " + PROXIED_HOST + "/trial", new CachedResponse("PUT " + PROXIED_HOST + "/trial", "{\"name\":\"turvey\"}", Http.Status.CREATED, "application/json"));
        _responseCache.put("GET /forward1", new CachedResponse("PUT /trial", "{\"name\":\"forwarded request\"}", Http.Status.OK, "application/json"));
    }

    public static Result serviceRequest() {
        //@ApiParam(value = "tag's name", required = true) @PathParam("name") String name) {
        //request().headers().put("X-Forwarded-Host",new String[]{request().host()});

        Logger.info("host= " + Json.toJson(request().host()));
        Logger.info("headers = " + Json.toJson(request().headers()));
        String request = request().toString();
        Logger.info("EdgeProxy-ing " + request);
        Logger.info("EdgeProxy cache" + _responseCache);
        if (_responseCache.containsKey(request)) {
            Logger.info("found cached EdgeProxy-ing " + request);
            CachedResponse resp = (CachedResponse) _responseCache.get(getCacheKeyFromRequest());
            if (!resp.expired(10000)) {
                if (resp.responseContentType == "application/json") {
                    Logger.info(" cached EdgeProxy-ing json response " + Json.parse(resp.responseValue));
                    resp.modifiedTime = DateTime.now();
                    return status(resp.responseStatus, Json.parse(resp.responseValue));
                } else {
                    Logger.info(" cached EdgeProxy-ing text/html response " + resp.responseValue);
                    response().setContentType("text/html");
                    return status(resp.responseStatus, resp.responseValue);
                }
            }
        }
        return forwardRequest();
    }

    public static String getCacheKeyFromRequest() {
        return new StringBuilder().append(request().method()).
                append(" ").
                append(PROXIED_HOST).
                append(request().path()).
                toString();
    }

    public static Result getCachedResponse() {
        return getCachedResponse(getCacheKeyFromRequest());
    }

    public static Result getCachedResponse(String key) {
        if (_responseCache != null) {
            if (_responseCache.containsKey(key)) {
                Logger.info("found cached EdgeProxy-ing " + key);
                CachedResponse resp = (CachedResponse) _responseCache.get(key);
                if (resp.responseContentType == "application/json") {
                    Logger.info(" cached EdgeProxy-ing json response " + Json.parse(resp.responseValue));
                    return status(resp.responseStatus, Json.parse(resp.responseValue));
                } else {
                    Logger.info(" cached EdgeProxy-ing text/html response " + resp.responseValue);
                    response().setContentType("text/html");
                    return status(resp.responseStatus, resp.responseValue);
                }
            }
        }
        return notFound("Cached response not found");
    }

    public static Result forwardRequest() {
        Logger.info("forwarding proxied request " + PROXIED_HOST + request().path());
        //Logger.info("forwarding proxied request " + Json.toJson(request().headers()));
        final WSRequestHolder holder = WS.url(PROXIED_HOST + request().path());
        for (String key : request().headers().keySet()) {
            for (int i = 0; i < request().headers().get(key).length; ++i) {
                holder.setHeader(key, request().headers().get(key)[i]);
            }
        }
        try {
            final F.Promise<Result> resultPromise = holder.get().map(
                    new F.Function<WSResponse, Result>() {
                        public Result apply(WSResponse response) {
                            // need original request.  put in header?
                            if (response.getStatus() >= 400) {
                                return getCachedResponse();
                            }

                            CachedResponse resp = new CachedResponse(
                                    request().toString(),
                                    response.getBody(),
                                    response.getStatus(),
                                    "application/json"
                            );

                            if (request().accepts("application/json")) {
                                response().setContentType("application/json");
//                            } else if (request().accepts("text/html")) {
//                                resp.responseContentType = "text/html";
//                                response().setContentType("text/html");
                            } else {
                                return notFound();
                            }
                            _responseCache.put(request().method() + " " + request().path(), resp);
                            return status(response.getStatus(), response.getBody());
                        }
                    }
            );

            return resultPromise.get(20001);
        } catch (Exception proxyError) {
            return getCachedResponse(holder.getUrl());
        }
    }
}
