package Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class will serve like a database, wherre we config a json to store data, have the simple
 * methods to serialize(convert objects into json) and deserialize(convert json to array of objects)
 *
 * Easy Way to Store data and controll login and register
 */
public class JsonFileHelper {
    private final String diretoryName;

    private final Gson jsonHelper;

    public JsonFileHelper(String diretoryName) throws IOException {
        this.diretoryName = diretoryName;
        Files.createDirectories(Paths.get(diretoryName));
        this.jsonHelper = configGson();
    }


    /**
     * Config Gson output, including nulls values and white spaces
     * @return a instance of a Gson with new configs
     */
    private Gson configGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.serializeNulls();

        return gsonBuilder.create();
    }


    /**
     * Convert object to json and insert him in file
     * @param filename name of file to being store data of object
     * @param obj object to be serialized
     * @param <T> generic type of object
     * @throws IOException
     */
    public synchronized <T> void serialize(String filename, T obj) throws IOException {
        try (Writer writer = new FileWriter(diretoryName + filename + ".json")) {
            writer.write(jsonHelper.toJson(obj));
        }
    }

    /**
     * Read the json and convert to array of objects
     * @param filename name of file where is json stored
     * @param type type class of object being converted
     * @return array of objets of type @type
     * @param <T> generic type
     * @throws IOException
     */
    public synchronized <T> List<T> deserializeArray(String filename, Class<T[]> type) throws IOException {
        if (!new File(diretoryName + filename + ".json").exists()) return new ArrayList<>();

        String jsonString = new String(Files.readAllBytes(Paths.get(diretoryName + filename + ".json")));

        if (jsonString.isEmpty())
            return new ArrayList<>();

        return Arrays.asList(jsonHelper.fromJson(jsonString, type));
    }


}
