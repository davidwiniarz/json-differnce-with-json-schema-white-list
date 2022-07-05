package com.davidwiniarz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.System.*;


@SpringBootApplication
public class App {

    public static final String TYPE_ADD = "new";
    public static final String TYPE_EDIT = "update";
    public static final String TYPE_DELETE = "delete";

    private static void processMap(Map<String, String> collection, Map<String, Object> paramMapValues, Map<String, Object> paramMap, String type) {
        try {
            if (paramMapValues.get("type").equals(type)) {
                collection.put(paramMap.get("name").toString(), paramMapValues.get("value") != null ? paramMapValues.get("value").toString() : "");
            }
        } catch (NullPointerException e) {
            out.println("NullPointer happened with message: " + e.getMessage());
        }
    }


    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);


        MultiValueMap<String, String> requestQueryMap = new LinkedMultiValueMap<>();
        requestQueryMap.put("accountsId", Collections.singletonList("valuePredefinedWithAccountsId"));
        requestQueryMap.put("param2", Collections.singletonList("valuePredefinedWithParam2"));

        String config = """
                {
                  "clientId": "xxxx",
                  "headers": {
                    "param1": {
                      "type": "delete",
                      "value": ""
                    },
                    "param2": {
                      "type": "update",
                      "value": "newValue"
                    }
                  },
                  "query": {
                    "accountsId": {
                      "type": "delete"
                    },
                    "param2": {
                      "type": "update",
                      "value": "newValue"
                    },
                    "param3": {
                      "type": "new",
                      "value": "newValue"
                    },
                     "param5": {
                      "type": "new",
                      "value": "newValue5"
                    }
                  }
                }
                """;

        String config2 = """
                {
                  "clientId": "xxxx",
                  "headers": [
                    {
                      "name": "param1",
                      "type": "delete",
                      "value": ""
                    },
                    {
                      "name": "param2",
                      "type": "update",
                      "value": "newValue"
                    },
                    {
                      "name": "param3",
                      "type": "new",
                      "value": "newValue"
                    }
                  ],
                  "query": [
                    {
                      "name": "accountsId",
                      "type": "delete",
                      "value": ""
                    },
                 
                    {
                      "name": "param2",
                      "type": "update",
                      "value": "newValue12312312312312"
                    },
                     {
                      "name": "param2555",
                      "type": "update",
                      "value": "newValue12312312312312"
                    },
                    {
                      "name": "param3",
                      "type": "new",
                      "value": "newValue!!!!!"
                    },
                    {
                      "name": "param5",
                      "type": "new",
                      "value": "newValue5!!!!!"
                    }
                  ]
                }
                """;

        var jsonConfig = new JSONObject(config2);
        Map<String, Object> configMap = jsonConfig.toMap();
        var queryConfigMap = (List<HashMap<String, Object>>) configMap.get("query");

        Map<String, String> collectionToAdd = new HashMap<>();
        Map<String, String> collectionToEdit = new HashMap<>();
        Map<String, String> collectionToDelete = new HashMap<>();
        queryConfigMap
                .forEach(paramMap -> {
                            processMap(collectionToAdd, paramMap, paramMap, TYPE_ADD);
                            processMap(collectionToDelete, paramMap, paramMap, TYPE_DELETE);
                            processMap(collectionToEdit, paramMap, paramMap, TYPE_EDIT);
                        }
                );

        out.println("NEW: " + collectionToAdd);
        out.println("EDIT: " + collectionToEdit);
        out.println("DELETE: " + collectionToDelete);


//        var requestQueryMapResult = requestQueryMap.entrySet().stream()
//                .filter(entry -> !collectionToDelete.containsKey(entry.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        ObjectMapper mapper = new ObjectMapper();
        PreProcessingConfig root = mapper.readValue(config2, PreProcessingConfig.class);

        Map<String, List<String>> collectionToAdd2 = new HashMap<>();
        Map<String, List<String>> collectionToEdit2 = new HashMap<>();
        Map<String, List<String>> collectionToDelete2 = new HashMap<>();

        root.getQuery()
                .forEach(query -> {
                    switch (query.getType()) {
                        case TYPE_ADD -> collectionToAdd2.put(query.getName(), Collections.singletonList(query.getValue()));
                        case TYPE_DELETE -> collectionToDelete2.put(query.getName(), Collections.singletonList(query.getValue()));
                        case TYPE_EDIT -> collectionToEdit2.put(query.getName(), Collections.singletonList(query.getValue()));
                        default -> out.println("None of config list can be processed as syntax is wrong.");
                    }
                });
        out.println("NEW: " + collectionToAdd2);
        out.println("EDIT: " + collectionToEdit2);
        out.println("DELETE: " + collectionToDelete2);
        out.println("---------------------");


        //DELETE
        var requestQueryMapResult2 = requestQueryMap.entrySet().stream()
                .filter(entry -> !collectionToDelete2.containsKey(entry.getKey()))
                .peek(entry -> {
                    if (collectionToEdit2.containsKey(entry.getKey())) {
                        collectionToEdit2.entrySet().stream().forEach(
                                element -> {
                                    if (element.getKey().equals(entry.getKey()))
                                        entry.setValue(element.getValue());
                                }
                        );
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        //EDIT
        var collectionToEditFiltered = collectionToEdit2.entrySet().stream()
                .filter(entry -> requestQueryMapResult2.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        out.println("COLLECTION TO EDIT FILTERED: " + collectionToEditFiltered);

        //ADD
        requestQueryMapResult2.putAll(collectionToAdd2);

//        out.println(requestQueryMapResult);
        out.println(requestQueryMapResult2);


//        var queryConfig = (MultiValueMap) configMap.get("query");
//        out.println(queryConfig.toSingleValueMap());
//        out.println(queryConfig.toString());
//        out.println(queryConfig.keySet());
//        out.println(queryConfig.values());
//        out.println(queryConfig.entrySet());


//        out.println(PD.search("NONE2"));
//        out.println("________");
//        out.println(PD.search("NONE"));
//        out.println("________");
//        out.println(PD.search("nodata"));
//        out.println("________");
//        out.println(PD.search("no data"));
//        out.println("________");
//        out.println(PD.search("No data"));
//        out.println("________");
//        out.println(PD.search("IND"));
//
//        File file = new File("/src/main/resources/org.json");
//        var jsonStringOrg = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
//        File file2 = new File("src/main/resources/schema.json");
//        var jsonStringSchema = FileUtils.readFileToString(file2, StandardCharsets.UTF_8);
//
//        JSONObject jsonOrg = new JSONObject(jsonStringOrg);
//        JSONObject jsonSchema = new JSONObject(jsonStringSchema);
//
//        MapDifference<String, Object> diff = Maps.difference(jsonSchema.toMap(), jsonOrg.toMap());
//        var map = jsonOrg.toMap();
//        removeKeys(map, diff.entriesOnlyOnRight().keySet());
//        Gson gson = new Gson();
//        //sort by key
//        LinkedHashMap<String, Object> objMap = new LinkedHashMap<>();
//        map
//                .entrySet()
//                .stream()
//                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
//                .forEachOrdered(x -> objMap.put(x.getKey(), x.getValue()));
//
//        out.println(gson.toJson(objMap));


    }

    @Getter
    @AllArgsConstructor
    public enum PD {

        NONE("None of your business"),
        IND("Individial"),
        UNKNOWN("No data");

        private String term;

        public static String search(String displayString) {
            try {
                return PD.valueOf(displayString).getTerm();
            } catch (Exception e) {
                out.println("Exception");
                return PD.NONE.getTerm();
            }
        }
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
