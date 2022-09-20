import java.io.IOException;

import authorization.Credentials;

public class GCalendarCli {
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String CREDENTIALS_FILE_PATH = "./credentials.json";

    private static void getCredentials() throws IOException {
        new Credentials(GCalendarCli.class.getResourceAsStream(CREDENTIALS_FILE_PATH));
    }

    public static void main(String[] args) throws IOException {
        getCredentials();
    }
}
