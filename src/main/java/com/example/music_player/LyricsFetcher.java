package com.example.music_player;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class LyricsFetcher {
    private static final String API_URL = "https://api.lyrics.ovh/v1";

    public static String fetchLyrics(String artist , String title) {
        try{
            String requestUrl = API_URL + "/" + artist.replace(" ", "%20") + "/" + title.replace(" ", "%20");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.body());
            JsonObject jsonResponse = new JsonParser().parse(response.body()).getAsJsonObject();
            return jsonResponse.has("lyrics") ? jsonResponse.get("lyrics").getAsString() : "Sorry . Could not find lyrics !";

        }catch(Exception e)
        {
            e.printStackTrace();
            return "Error fetching lyrics.";
        }
    }

}
