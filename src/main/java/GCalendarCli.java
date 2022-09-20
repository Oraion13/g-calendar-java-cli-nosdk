import java.io.IOException;

import authorization.Credentials;
import authorization.TokenCenter;

public class GCalendarCli {
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String CREDENTIALS_FILE_PATH = "./credentials.json";
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static void getCredentials() throws IOException {
        String accessToken = new Credentials(GCalendarCli.class.getResourceAsStream(CREDENTIALS_FILE_PATH), new TokenCenter(new java.io.File(TOKENS_DIRECTORY_PATH))).execute();

        System.out.println("Access Token: " + accessToken);
    }

    public static void main(String[] args) throws IOException {
        getCredentials();
    }
}
