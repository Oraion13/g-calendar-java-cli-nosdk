package authorization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONObject;

public class Credentials {
    private InputStream FILE_STREAM;

    // get the FileStream which contains the JSON object
    public Credentials(InputStream fileStream) {
        this.FILE_STREAM = fileStream;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public JSONObject getJSON() throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(FILE_STREAM));

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }

        return new JSONObject(sb.toString());
    }
}
