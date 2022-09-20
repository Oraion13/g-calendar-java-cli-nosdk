package authorization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.json.JSONObject;

public class Credentials {
    private InputStream CREDENTIAL_FILE_STREAM;

    // get the FileStream which contains the JSON object
    public Credentials(InputStream fileStream) {
        this.CREDENTIAL_FILE_STREAM = fileStream;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public JSONObject getJSON() throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(CREDENTIAL_FILE_STREAM));

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }

        return new JSONObject(sb.toString());
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

}
