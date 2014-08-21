import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.Handler;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

/**
 * Created by skircher on 8/21/14.
 */
public class Global extends GlobalSettings {

    @Override
    public void beforeStart(Application application) {
        controllers.EdgeProxy.initEdgeProxy();
        super.beforeStart(application);
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader requestHeader) {
        Logger.info(requestHeader.toString());
        System.out.println("no handler found for... " + requestHeader.path() + " for " + requestHeader.uri());
        return F.Promise.<Result>pure((Result)controllers.EdgeProxy.serviceRequest());
    }

    @Override
    public Handler onRouteRequest(Http.RequestHeader requestHeader) {
        System.out.println("before each request... " + requestHeader.host() + " for " + requestHeader.uri());
        //return (play.api.mvc.Handler) controllers.EdgeProxy.serviceRequest();
        return super.onRouteRequest(requestHeader);
    }

    public Action onRequest(Http.Request request, Method actionMethod) {
        System.out.println("after routing each request... " + request.path() + " for " + actionMethod.getName() );
        return super.onRequest(request, actionMethod);

    }

}
