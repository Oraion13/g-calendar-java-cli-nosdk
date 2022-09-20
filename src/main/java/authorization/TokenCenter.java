package authorization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TokenCenter {
    private static File dataDirectory;

    public TokenCenter(File dataDirectory) throws IOException {
        // create parent directory (if necessary)
        if (!dataDirectory.exists() && !dataDirectory.mkdirs()) {
            throw new IOException("unable to create directory: " + dataDirectory);
        }
        TokenCenter.dataDirectory = dataDirectory;
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

}
