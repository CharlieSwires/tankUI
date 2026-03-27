package GenericGet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import Const.Constant;

public class GenericGet {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final int MAX_RETRIES = 5;
    private static final int TIMEOUT_SECONDS = 5;
    private static final Logger log = Logger.getLogger(GenericGet.class.getName());

    public void getGenericAsync(String suffix, Consumer<Double> onSuccess, Consumer<String> onError) {
        executor.submit(() -> {
            int retries = MAX_RETRIES;

            while (retries > 0) {
                Future<Double> future = executor.submit(() -> {
                    URL url = new URI(Constant.PI_HOME + Constant.PORT + Constant.PATH_PREFIX + suffix).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("HTTP error code: " + conn.getResponseCode());
                    }

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder inString = new StringBuilder();
                        String output;

                        while ((output = br.readLine()) != null) {
                            inString.append(output);
                        }
                        if (inString != null && inString.toString() != null && inString.toString().length() > 0) {
                            return Double.parseDouble(inString.toString());
                        } else {
                            return (double) Constant.ERROR;
                        }
                    } finally {
                        conn.disconnect();
                    }
                });

                try {
                    Double result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    onSuccess.accept(result);
                    return;
                } catch (TimeoutException e) {
                    future.cancel(true);
                    log.severe("Request timed out after " + TIMEOUT_SECONDS + " seconds: " + e.getMessage());
                    retries--;
                } catch (Exception e) {
                    log.severe("Error in HTTP request. Retries left: " + retries + ": " + e.getMessage());
                    retries--;
                }

                if (retries == 0) {
                    onError.accept("Failed after " + MAX_RETRIES + " retries or timeout.");
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    onError.accept("Thread interrupted: " + e.getMessage());
                    return;
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}