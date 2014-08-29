package controllers;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.FakeRequest;
import play.test.Helpers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * Created by skircher on 8/21/14.
 */
public class EdgeProxyTest extends TestCase {
    //    private final Http.Request request = mock(Http.Request.class);
    public static FakeApplication app;

    @Before
    public void setUp() throws Exception {
        EdgeProxy.PROXIED_HOST = "http://localhost:9001";
        EdgeProxy._responseCache.put("GET " + EdgeProxy.PROXIED_HOST + "/time", new CachedResponse("GET " + EdgeProxy.PROXIED_HOST  + "/time", "{\"name\":\"tipsy\"}", Http.Status.OK, "application/json"));
        EdgeProxy._responseCache.put("PUT " + EdgeProxy.PROXIED_HOST  + "/trial", new CachedResponse("PUT " + EdgeProxy.PROXIED_HOST  + "/trial", "{\"name\":\"turvey\"}", Http.Status.CREATED, "application/json"));
    }

    @Test
    public void testInitEdgeProxy() throws Exception {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = EdgeProxy.getCachedResponse("GET http://localhost:9001/time");
                assertThat(status(result)).isEqualTo(OK);
                result = EdgeProxy.getCachedResponse("PUT http://localhost:9001/trial");
                assertThat(contentAsString(result, 2000).contains("{\"name\":\"turvey\"}"));
                assertThat(status(result)).isEqualTo(CREATED);
                Logger.info(" content = " + contentAsString(result));
            }
        });
    }

    @Test
    public void testGetCachedResponse() throws Exception {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = EdgeProxy.getCachedResponse("GET http://localhost:9001/time");
                assertThat(status(result)).isEqualTo(OK);
                result = EdgeProxy.getCachedResponse("PUT http://localhost:9001/trial");
                assertThat(contentAsString(result, 2000).contains("{\"name\":\"turvey\"}"));
                assertThat(status(result)).isEqualTo(CREATED);
                Logger.info(" content = " + contentAsString(result));
            }
        });
    }

    @Test
    public void testServiceRequest() {
        running(fakeApplication(), new Runnable() {
                    public void run() {
                        EdgeProxy.PROXIED_HOST = "https://st-dev.optiolabshq.com";
                        FakeRequest fakeRequest = new FakeRequest(GET, "/v1/frontend/device/b600e2da-1ab6-3e3c-8c77-8fcb75fd9e06");
                        fakeRequest.withHeader("Authorization", "7a3443a94d34385356a388f0609acf6ec245b057f096534dfb3a133fb3d3ce17628af42438b0788d56ad2ae2dc6a3c1defcbf8976866e4600c03e74971b326e6");
                        Result result = callAction(
                                routes.ref.EdgeProxy.serviceRequest(),
                                fakeRequest);
                        assertThat(status(result)).isEqualTo(OK);
                        assertThat(contentType(result)).isEqualTo("application/json");
                        assertThat(result).toString().contains("com.optiolabs.st.server.data.GetDeviceInfoResponse");
                    }
                }
        );
    }


    @Test
    public void testServiceRequestDoesntExist() {
        running(fakeApplication(), new Runnable() {
                    public void run() {
                        EdgeProxy.PROXIED_HOST = "https://doesntexist";
                        FakeRequest fakeRequest = new FakeRequest(GET, "/neverfindthis");
                        Result result = callAction(
                                routes.ref.EdgeProxy.serviceRequest(),
                                fakeRequest);
                        assertThat(status(result)).isEqualTo(SERVICE_UNAVAILABLE);
                    }
                }
        );
    }

    @Test
    public void testForwardReqest() throws Exception {
        running(fakeApplication(), new Runnable() {
                    public void run() {
                        EdgeProxy.PROXIED_HOST = "https://st-dev.optiolabshq.com";
                        FakeRequest fakeRequest = new FakeRequest(GET, "/v1/frontend/device/b600e2da-1ab6-3e3c-8c77-8fcb75fd9e06");
                        fakeRequest.withHeader("Authorization", "7a3443a94d34385356a388f0609acf6ec245b057f096534dfb3a133fb3d3ce17628af42438b0788d56ad2ae2dc6a3c1defcbf8976866e4600c03e74971b326e6");
                        Result result = callAction(
                                routes.ref.EdgeProxy.forwardRequest(),
                                fakeRequest);
                        assertThat(status(result)).isEqualTo(OK);
                        assertThat(contentType(result)).isEqualTo("application/json");
                        assertThat(result).toString().contains("com.optiolabs.st.server.data.GetDeviceInfoResponse");
                    }
                }
        );

    }
    @Test
    public void testForwardReqestUnsupportedMediaTypes() throws Exception {
        running(fakeApplication(), new Runnable() {
                    public void run() {
                        EdgeProxy.PROXIED_HOST = "https://st-dev.optiolabshq.com";
                        FakeRequest fakeRequest = new FakeRequest(GET, "/login");
                        Result result = callAction(
                                routes.ref.EdgeProxy.forwardRequest(),
                                fakeRequest);
                        assertThat(status(result)).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
                    }
                }
        );

    }

}
