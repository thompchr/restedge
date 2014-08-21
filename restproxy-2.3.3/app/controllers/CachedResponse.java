package controllers;

/**
 * Created by skircher on 8/19/14.
 */
public class CachedResponse {
    public String   requestMatch;
    public String   responseValue;
    public int      responseStatus;
    public String   responseContentType;

    public CachedResponse(String _requestMatch, String _responseValue, int _responseStatus, String _responseContentType) {
        requestMatch = _requestMatch;
        responseValue = _responseValue;
        responseStatus = _responseStatus;
        responseContentType = _responseContentType;
    }
}
