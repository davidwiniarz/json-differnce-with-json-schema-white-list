package com.davidwiniarz;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.System.out;

@SpringBootApplication
public class App {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);

        File file = new File("/src/main/resources/org.json");
        var jsonStringOrg = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        File file2 = new File("src/main/resources/schema.json");
        var jsonStringSchema = FileUtils.readFileToString(file2, StandardCharsets.UTF_8);

        JSONObject jsonOrg = new JSONObject(jsonStringOrg);
        JSONObject jsonSchema = new JSONObject(jsonStringSchema);

        MapDifference<String, Object> diff = Maps.difference(jsonSchema.toMap(), jsonOrg.toMap());
        var map = jsonOrg.toMap();
        removeKeys(map, diff.entriesOnlyOnRight().keySet());
        Gson gson = new Gson();
        //sort by key
        LinkedHashMap<String, Object> objMap = new LinkedHashMap<>();
        map
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .forEachOrdered(x -> objMap.put(x.getKey(), x.getValue()));

        out.println(gson.toJson(objMap));
    }


    public static void removeKeys(Map<String, Object> levels, Set<String> keys) {
        if (levels == null
            || levels.size() == 0) {
            return;
        }

        for (String k : keys) {
            try {
                levels.remove(k);
            } catch (Exception e) {
                out.println("!!!!! Cannot remove this key:" + keys + " as it does not exists.");
            }
        }

        for (String key : levels.keySet()) {
            if (levels.get(key) instanceof Map) {
                removeKeys((Map<String, Object>) levels.get(key), keys);
            }
            if (levels.get(key) instanceof ArrayList) {
                removeKeyInArray((ArrayList<Map<String, Object>>) levels.get(key), keys);
            }
        }
    }

    private static void removeKeyInArray(ArrayList<Map<String, Object>> levels, Set<String> keys) {
        if (levels == null
            || levels.size() == 0) {
            return;
        }

        for (String k : keys) {
            try {
                levels.remove(k);
            } catch (Exception e) {
                out.println("!!!!! Cannot remove this key:" + keys + " as it does not exists.");
            }
        }
        for (Map key : levels) {
            if (key != null) {
                removeKeys(key, keys);
            }
        }

    }

}
