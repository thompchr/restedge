package controllers;


import org.joda.time.DateTime;
import org.joda.time.Duration;
import play.Logger;

/**
 * Created by skircher on 8/19/14.
 */
public class CachedResponse {
    public String   requestMatch;
    public String   responseValue;
    public int      responseStatus;
    public String   responseContentType;
    public DateTime modifiedTime;

    public CachedResponse(String _requestMatch, String _responseValue, int _responseStatus, String _responseContentType) {
        requestMatch = _requestMatch;
        responseValue = _responseValue;
        responseStatus = _responseStatus;
        responseContentType = _responseContentType;
        modifiedTime =  DateTime.now();
    }
    public boolean expired(long _thresholdMiliseconds) {
        Duration duration = new Duration(modifiedTime, DateTime.now());
        Logger.info("Cached request is " + duration.getMillis() + " old");
        return (duration.getMillis() >_thresholdMiliseconds)?true:false;
    }

}
