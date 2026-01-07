package me.chrommob.minestore.api.web;

import java.util.List;
import java.util.Map;

public class WebContext extends Exception {
    private final WebRequest<?> request;
    private final String url;
    private final int responseCode;
    private final String responseString;
    private final Map<String, List<String>> headers;
    private final boolean isCloudflare;

    public WebContext(WebRequest<?> request, String url, int responseCode, String responseString, Map<String, List<String>> headers, Throwable cause) {
        super("Error occurred while making request to MineStore API!" + System.lineSeparator()
                +"URL: " + url + System.lineSeparator()
                + "Method: " + request.getType().name() + System.lineSeparator()
                + "Request: " + request.getParams() + System.lineSeparator()
                + "Body: " + (request.getBody() == null ? "null" : new String(request.getBody())) + System.lineSeparator()
                + "Response code: " + responseCode + System.lineSeparator()
                + "Response: " + responseString + System.lineSeparator()
                + "Headers: " + headers, cause);
        this.request = request;
        this.url = url;
        this.responseCode = responseCode;
        this.responseString = responseString;
        this.headers = headers;
        this.isCloudflare = false;
    }

    public WebContext(boolean isCloudflare, WebRequest<?> request, String url, int responseCode, String responseString, Map<String, List<String>> headers) {
        super("Error occurred while making request to MineStore API!" + System.lineSeparator()
                +"URL: " + url + System.lineSeparator()
                + "Method: " + request.getType().name() + System.lineSeparator()
                + "Request: " + request.getParams() + System.lineSeparator()
                + "Body: " + (request.getBody() == null ? "null" : new String(request.getBody())) + System.lineSeparator()
                + "Response code: " + responseCode + System.lineSeparator()
                + "Response: " + responseString + System.lineSeparator()
                + "Headers: " + headers);
        this.request = request;
        this.url = url;
        this.responseCode = responseCode;
        this.responseString = responseString;
        this.headers = headers;
        this.isCloudflare = isCloudflare;
    }

    public WebContext(WebRequest<?> request, String url, int responseCode, String responseString, Map<String, List<String>> headers) {
        super("Error occurred while making request to MineStore API!" + System.lineSeparator()
                +"URL: " + url + System.lineSeparator()
                + "Method: " + request.getType().name() + System.lineSeparator()
                + "Request: " + request.getParams() + System.lineSeparator()
                + "Body: " + (request.getBody() == null ? "null" : new String(request.getBody())) + System.lineSeparator()
                + "Response code: " + responseCode + System.lineSeparator()
                + "Response: " + responseString + System.lineSeparator()
                + "Headers: " + headers);
        this.isCloudflare = false;
        this.request = request;
        this.url = url;
        this.responseCode = responseCode;
        this.responseString = responseString;
        this.headers = headers;
    }

    public WebContext(WebRequest<?> request, String url, Throwable cause) {
        super("Error occurred while making request to MineStore API!" + System.lineSeparator()
                + "URL: " + url + System.lineSeparator()
                + "Method: " + request.getType().name() + System.lineSeparator()
                + "Request: " + request.getParams() + System.lineSeparator()
                + "Body: " + (request.getBody() == null ? "null" : new String(request.getBody())), cause);
        this.isCloudflare = false;
        this.request = request;
        this.url = url;
        this.responseCode = -1;
        this.responseString = null;
        this.headers = null;
    }

    public WebContext(String message) {
        super(message);
        this.isCloudflare = false;
        this.request = null;
        this.url = null;
        this.responseCode = -1;
        this.responseString = null;
        this.headers = null;
    }

    public boolean isCloudflare() {
        return isCloudflare;
    }

    public WebRequest<?> request() {
        return request;
    }

    public String url() {
        return url;
    }

    public int responseCode() {
        return responseCode;
    }

    public String responseString() {
        return responseString;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    @Override
    public String toString() {
        return "WebContext{" +
                "request=" + request +
                ", url='" + url + '\'' +
                ", responseCode=" + responseCode +
                ", responseString='" + responseString + '\'' +
                ", headers=" + headers +
                ", isCloudflare=" + isCloudflare +
                '}';
    }
}