package calendar;

import authorization.Credentials;
import netscape.javascript.JSObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class Calendar {
    public static final String DEFAULT_ROOT_URL = "https://www.googleapis.com/";
    public static final String DEFAULT_SERVICE_PATH = "calendar/v3/";
    public static final String DEFAULT_BASE_URL = (DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH);

    public Calendar(String accessToken){
        this.ACCESS_TOKEN = accessToken;
    }
    private String ACCESS_TOKEN = null;
    public void setAccessToken(String accessToken){
        ACCESS_TOKEN = accessToken;
    }

    public String CALENDAR_ID = "calendars/primary/";
    public void setCalendarId(String calendarId){
        this.CALENDAR_ID = "calendars/" + calendarId + "/";
    }

    public String requestUrl = null;
    public void setRequestUrl(String requestUrl){
        this.requestUrl = requestUrl;
    }

    public String responseBody = null;
    public String sendRequest(String method, Path bodyPath) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = null;
        URI uri = URI.create(DEFAULT_BASE_URL + this.CALENDAR_ID + this.requestUrl);
        String authorization = this.ACCESS_TOKEN;

        switch (method) {
            case "GET":
                request = this.GET(uri, authorization);
                break;
            case "POST":
                request = this.POST(uri, authorization, bodyPath);
                break;
            case "PUT":
                request = this.PUT(uri, authorization, bodyPath);
                break;
            case "DELETE":
                request = this.DELETE(uri, authorization);
                break;
        }

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private HttpRequest GET(URI uri, String authorization){
        return HttpRequest.newBuilder()
                .uri(uri).GET().header("Authorization", authorization).build();
    }

    private HttpRequest POST(URI uri, String authorization, Path bodyPath) throws FileNotFoundException {
        return HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofFile(bodyPath)).header("Authorization", authorization).build();
    }

    private HttpRequest PUT(URI uri, String authorization, Path bodyPath) throws FileNotFoundException {
        return HttpRequest.newBuilder()
                .uri(uri)
                .PUT(HttpRequest.BodyPublishers.ofFile(bodyPath)).header("Authorization", authorization).build();
    }

    private HttpRequest DELETE(URI uri, String authorization) throws FileNotFoundException {
        return HttpRequest.newBuilder()
                .uri(uri).DELETE().header("Authorization", authorization).build();
    }
}
