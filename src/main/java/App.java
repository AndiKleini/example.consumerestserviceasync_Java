import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {

    public static final String HTTPS_API_CHUCKNORRIS_IO_JOKES_RANDOM = "https://api.chucknorris.io/jokes/random";

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        CompletableFuture<ChuckNorrisFact> yieldFact = getChuckNorrisFact();
        System.out.println("Waiting for fact");
        while(!yieldFact.isDone()) {
            System.out.print(".");
            Thread.sleep(250);
        }
        System.out.println("Fact received :" + yieldFact.get().getValue());
    }

    private static CompletableFuture<ChuckNorrisFact> getChuckNorrisFact() throws URISyntaxException, JsonProcessingException {
        String url = HTTPS_API_CHUCKNORRIS_IO_JOKES_RANDOM;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url)).build();
        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(
                        stringHttpResponse -> {
                            try {
                                simulateLatency();
                                return parseResponse(stringHttpResponse.body());
                            } catch (JsonProcessingException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        });
    }

    private static void simulateLatency() throws InterruptedException {
        Thread.sleep(5000);
    }

    private static ChuckNorrisFact parseResponse(String toParse) throws JsonProcessingException {
        ChuckNorrisFact response;
        var objectMapper = new ObjectMapper();
        objectMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(
                    DeserializationContext ctxt,
                    JsonParser p,
                    JsonDeserializer<?> deserializer,
                    Object beanOrClass,
                    String propertyName) throws IOException {
                if(beanOrClass.getClass().equals(ChuckNorrisFact.class)) {
                    p.skipChildren();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return objectMapper.readValue(toParse, ChuckNorrisFact.class);
    }
}
