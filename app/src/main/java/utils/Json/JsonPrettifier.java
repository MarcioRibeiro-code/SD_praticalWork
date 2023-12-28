package utils.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonPrettifier {

    public static String prettifyJsonString(String jsonString) {
        try {
            // Parse the JSON string to a JsonElement
            JsonElement jsonElement = JsonParser.parseString(jsonString);

            // Create a Gson object with pretty printing enabled
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Convert the JsonElement back to a formatted JSON string
            String formattedJson = gson.toJson(jsonElement);

            return formattedJson;
        } catch (Exception e) {
            e.printStackTrace();
            return jsonString;
        }
    }
}
