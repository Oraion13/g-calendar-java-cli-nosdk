package authorization;

import java.io.File;

public class TokenCenter {
    File StoredCredentials = null;

    public TokenCenter(File file) {
        this.StoredCredentials = file;
    }

}
