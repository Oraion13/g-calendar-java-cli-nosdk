package authorization;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.URIParameter;
import java.time.Instant;
import java.util.Base64;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.*;

import org.json.JSONObject;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

public class Credentials {
    /** Credentials Stored in JSON format */
    private static InputStream CREDENTIAL_FILE_STREAM;
    private static TokenCenter tokenCenter;
    /**
     * See, edit, share, and permanently delete all the calendars you can access
     * using Google Calendar.
     */
    public static final String CALENDAR = "https://www.googleapis.com/auth/calendar";

    /** View and edit events on all your calendars. */
    public static final String CALENDAR_EVENTS = "https://www.googleapis.com/auth/calendar.events";

    /** View events on all your calendars. */
    public static final String CALENDAR_EVENTS_READONLY = "https://www.googleapis.com/auth/calendar.events.readonly";

    /** See and download any calendar you can access using your Google Calendar. */
    public static final String CALENDAR_READONLY = "https://www.googleapis.com/auth/calendar.readonly";

    /** View your Calendar settings. */
    public static final String CALENDAR_SETTINGS_READONLY = "https://www.googleapis.com/auth/calendar.settings.readonly";

    private static final Logger LOGGER = Logger.getLogger(Credentials.class.getName());

    // get the FileStream which contains the JSON object
    public Credentials(InputStream fileStream, TokenCenter tokenCenter) {
        Credentials.CREDENTIAL_FILE_STREAM = fileStream;
        Credentials.tokenCenter = tokenCenter;
    }

    /**
     * Extract the credentials from a JSON file
     * 
     * @return a JSON object containing the credentials
     * @throws IOException
     */
    public static JSONObject getJSON() throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(CREDENTIAL_FILE_STREAM));

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }

        return new JSONObject(sb.toString());
    }

    public String execute() throws IOException {
        if(!tokenCenter.isTokenExpired()){
            return tokenCenter.getAccessToken();
        }

        if(tokenCenter.refreshAccessToken()){
            return tokenCenter.getAccessToken();
        }

        Credential credential = new Credential();

        String auth_url = credential.getAUTH_URL();
        browse(auth_url);

        Reciever reciever = new Reciever();
        reciever.startRecievingServer();

        String code = reciever.waitForCode();
        credential.setCODE(code);
        tokenCenter.setTokenCredentials(credential.setTokenBody());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = credential.generateTokenURL(tokenCenter);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(Credentials::parseBody).join();

        return tokenCenter.getAccessToken();
    }

    public static String parseBody(String responseBody) {
        try{
            StringBuilder str = new StringBuilder(responseBody);
            str.deleteCharAt(str.length() - 1);
            str.append(",").append("\"created_at\":\"").append(Instant.now()).append("\"}");
            TokenCenter.setTokens(str.toString());
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public void browse(String url) {
        // Ask user to open in their browser using copy-paste
        System.out.println("Please open the following address in your browser:");
        System.out.println("  " + url);
        // Attempt to open it in the browser
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Action.BROWSE)) {
                    System.out.println("Attempting to open that address in the default browser now...");
                    desktop.browse(URI.create(url));
                }
            }
        } catch (IOException | InternalError e) {
            LOGGER.log(Level.WARNING, "Unable to open browser", e);
        }
    }

    /**
     * PKCE code_verifier and code_challange generator class
     */
    private static class PKCE {
        private final String verifier;
        private String challenge;
        private String challengeMethod;

        public PKCE() {
            verifier = generateVerifier();
            generateChallenge(verifier);
        }

        private static String generateVerifier() {
            SecureRandom sr = new SecureRandom();
            byte[] code = new byte[32];
            sr.nextBytes(code);
            return Base64.getEncoder().encodeToString(code);
        }

        /**
         * A unique code verifier is created for every authorization request, and its
         * transformed value, called "code_challenge", is sent to the authorization
         * server to obtain the authorization code.
         */
        private void generateChallenge(String verifier) {
            try {
                byte[] bytes = verifier.getBytes();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(bytes, 0, bytes.length);
                byte[] digest = md.digest();
                challenge = Base64.getEncoder().encodeToString(digest);
                challengeMethod = "S256";
            } catch (NoSuchAlgorithmException e) {
                challenge = verifier;
                challengeMethod = "plain";
            }
        }

        public String getVerifier() {
            return verifier;
        }

        public String getChallenge() {
            return challenge;
        }

        public String getChallengeMethod() {
            return challengeMethod;
        }
    }

    private static class Credential {
        private String AUTH_URI;
        private String TOKEN_URI;

        private String CLIENT_ID;
        private String CLIENT_SECRET;
        private String REDIRECT_URI;
        private String RESPONSE_TYPE;
        private String SCOPE;
        private String STATE;
        private String CODE_CHALLANGE;
        private String CODE_CHALLANGE_METHOD;
        private String CODE_VERIFIER;

        private String CODE;
        private String GRANT_TYPE;

        private int PORT = 8888;

        private String AUTH_URL;
        private String TOKEN_URL;

        public Credential() throws IOException {
            System.out.println("Generating Auth URL...");
            JSONObject credentials = getJSON().getJSONObject("installed");

            AUTH_URI = credentials.getString("auth_uri");
            TOKEN_URI = credentials.getString("token_uri");

            CLIENT_ID = credentials.getString("client_id");
            CLIENT_SECRET = credentials.getString("client_secret");
            REDIRECT_URI = (String) credentials.getJSONArray("redirect_uris").get(0);
            RESPONSE_TYPE = "code";
            SCOPE = CALENDAR + "%20" + CALENDAR_EVENTS;
            STATE = "secure_token";
            GRANT_TYPE = "authorization_code";

            PKCE pkce = new PKCE();
            CODE_CHALLANGE = pkce.getChallenge();
            CODE_CHALLANGE_METHOD = pkce.getChallengeMethod();
            CODE_VERIFIER = pkce.getVerifier();

            generateAuthURL();
        }

        private void generateAuthURL() {
            AUTH_URL = AUTH_URI + "?" + "scope=" + SCOPE + "&" +
                    "response_type=" + RESPONSE_TYPE + "&"
                    + "state=" + STATE + "&"
                    + "redirect_uri=" + REDIRECT_URI + "&"
                    + "client_id=" + CLIENT_ID;
        }

        public HttpRequest generateTokenURL(TokenCenter tokenCenter) throws FileNotFoundException {
            return HttpRequest.newBuilder().uri(URI.create(TOKEN_URI))
                    .POST(HttpRequest.BodyPublishers.ofFile(tokenCenter.getTokenCredentialsPath())).build();
        }

        public String setTokenBody() {
            return "{\"client_id\":\"" + CLIENT_ID + "\","
                    + "\"client_secret\":\"" + CLIENT_SECRET + "\","
                    + "\"code\":\"" + CODE + "\","
                    + "\"grant_type\":\"" + GRANT_TYPE + "\","
                    + "\"redirect_uri\":\"" + REDIRECT_URI + "\"}";
        }

        public String getCLIENT_ID() {
            return CLIENT_ID;
        }

        public String getCLIENT_SECRET() {
            return CLIENT_SECRET;
        }

        public String getREDIRECT_URI() {
            return REDIRECT_URI;
        }

        public String getRESPONSE_TYPE() {
            return RESPONSE_TYPE;
        }

        public String getSCOPE() {
            return SCOPE;
        }

        public String getSTATE() {
            return STATE;
        }

        public String getCODE_CHALLANGE() {
            return CODE_CHALLANGE;
        }

        public String getCODE_CHALLANGE_METHOD() {
            return CODE_CHALLANGE_METHOD;
        }

        public void setPORT(int PORT) {
            this.PORT = PORT;
        }

        public int getPORT() {
            return PORT;
        }

        public String getAUTH_URL() {
            return AUTH_URL;
        }

        public String getCODE() {
            return CODE;
        }

        public void setCODE(String CODE) {
            this.CODE = CODE;
        }

        public String getGRANT_TYPE() {
            return GRANT_TYPE;
        }

    }

}
