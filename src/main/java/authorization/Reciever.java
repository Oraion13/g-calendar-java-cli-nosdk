package authorization;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

public class Reciever {

    private static HttpServer server;

    static String callbackPath = "/oauth/callback";
    String error;
    String code;

    final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

    public void startRecievingServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8888), 0);
        HttpContext context = server.createContext(callbackPath, new CallbackHandler());
        server.setExecutor(null);

        try {
            server.start();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String waitForCode() throws IOException {
        waitUnlessSignaled.acquireUninterruptibly();
        if (error != null) {
            throw new IOException("User authorization failed (" + error + ")");
        }
        return code;
    }

    class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if (!callbackPath.equals(httpExchange.getRequestURI().getPath())) {
                return;
            }

                Map<String, String> parms = this.queryToMap(httpExchange.getRequestURI().getQuery());
                error = parms.get("error");
                code = parms.get("code");

                Headers respHeaders = httpExchange.getResponseHeaders();
                writeLandingHtml(httpExchange, respHeaders);
                httpExchange.close();
                waitUnlessSignaled.release();
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<String, String>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String pair[] = param.split("=");
                    if (pair.length > 1) {
                        result.put(pair[0], pair[1]);
                    } else {
                        result.put(pair[0], "");
                    }
                }
            }
            return result;
        }

        private void writeLandingHtml(HttpExchange exchange, Headers headers) throws IOException {
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(HTTP_OK, 0);
                headers.add("ContentType", "text/html");

                OutputStreamWriter doc = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                doc.write("<html>");
                doc.write("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
                doc.write("<body>");
                doc.write("Received verification code. You may now close this window.");
                doc.write("</body>");
                doc.write("</html>\n");
                doc.flush();
            }
        }
    }

}
