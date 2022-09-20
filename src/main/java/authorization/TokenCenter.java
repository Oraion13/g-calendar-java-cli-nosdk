package authorization;

import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class TokenCenter {
    private static File dataDirectory;
    private static File credentialsFile;

    public TokenCenter(File dataDirectory) throws IOException {
        // create parent directory (if necessary)
        if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
            throw new IOException("unable to create directory: " + dataDirectory);
        }
        TokenCenter.dataDirectory = dataDirectory;
        credentialsFile = new java.io.File("bin/main/credentials.json");
    }

    public TokenCenter(){
        TokenCenter.dataDirectory = new java.io.File("tokens");
        credentialsFile = new java.io.File("bin/main/credentials.json");

    }

    public Path getTokenCredentialsPath(){
        return Path.of(dataDirectory.getAbsolutePath() + "/tokenCredentials.json");
    }

    public void setTokenCredentials(String token) throws IOException {
        Path tokenCredentials = Path.of(dataDirectory.getAbsolutePath() + "/tokenCredentials.json");
        Files.writeString(tokenCredentials, token);
    }

    public Path getTokenPath(){
        return Path.of(dataDirectory.getAbsolutePath() + "/tokens.json");
    }

    public static void setTokens(String token) throws IOException {
        Path tokenCredentials = Path.of(dataDirectory.getAbsolutePath() + "/tokens.json");
        Files.writeString(tokenCredentials, token);
    }

    public static JSONObject getJSON(Path fileName) throws IOException {
        return new JSONObject(Files.readString(fileName));
    }

    private JSONObject getJSON(String fileName) throws IOException {
        return new JSONObject(Files.readString(Path.of(fileName)));
    }

    public boolean isTokenExpired()  {
        try {
            JSONObject token = getJSON(dataDirectory.getAbsolutePath() + "/tokens.json");
            long expires_in = token.getLong("expires_in");
            Instant created_at = Instant.parse(token.getString("created_at"));
            long difference = Duration.between(created_at, Instant.now()).getSeconds();

            if(difference >= expires_in || (expires_in - difference) <= 60){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean refreshAccessToken() throws IOException {
        JSONObject credentials = getJSON(credentialsFile.getAbsolutePath()).getJSONObject("installed");
        JSONObject tokens = getJSON(dataDirectory.getAbsolutePath() + "/tokens.json");

        if(tokens.isNull("refresh_token")){
            return false;
        }

        String tokenCredentialBody = "{\"client_id\":\"" + credentials.getString("client_id") + "\","
                + "\"client_secret\":\"" + credentials.getString("client_secret") + "\","
                + "\"grant_type\":\"" + "refresh_token" + "\","
                + "\"refresh_token\":\"" + tokens.getString("refresh_token") + "\"}";

        setTokenCredentials(tokenCredentialBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://oauth2.googleapis.com/token"))
                .POST(HttpRequest.BodyPublishers.ofFile(getTokenCredentialsPath())).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Credentials::parseBody).join();

        return true;
    }

    public boolean revokeToken() throws IOException {
        String accessToken = getAccessToken();
        if(accessToken == null) return false;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://oauth2.googleapis.com/revoke?token=" + accessToken))
                .POST(HttpRequest.BodyPublishers.ofString("")).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .join();

        new File(dataDirectory.getAbsolutePath() + "/tokenCredentials.json").deleteOnExit();
        new File(dataDirectory.getAbsolutePath() + "/tokens.json").deleteOnExit();

        return true;
    }

    public String getAccessToken() throws IOException {
        JSONObject tokens = getJSON(dataDirectory.getAbsolutePath() + "/tokens.json");

        if(tokens.isNull("access_token")){
            return null;
        }

        return tokens.getString("access_token");
    }

}
