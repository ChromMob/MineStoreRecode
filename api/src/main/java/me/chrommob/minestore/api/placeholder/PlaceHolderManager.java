package me.chrommob.minestore.api.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class PlaceHolderManager {
    private static PlaceHolderManager instance;
    private final Map<String, BiFunction<String, String, String>> placeHolders = new HashMap<>();

    public static PlaceHolderManager getInstance() {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
        return instance;
    }

    /**
     * Registers a placeholder function.
     *
     * @param name      The name of the placeholder. (Can also be a regex)
     * @param function  Consumes the player name and the placeholder name (which can be different from the @param name because of regex) and returns the value of the placeholder.
     */
    public void registerPlaceHolder(String name, BiFunction<String, String, String> function) {
        placeHolders.put(name, function);
    }

    public Map<String, BiFunction<String, String, String>> getPlaceHolders() {
        return placeHolders;
    }

    public String getResult(String name, String params) {
        if (placeHolders.containsKey(params)) {
            return placeHolders.get(params).apply(name, params);
        } else {
            String regexTest = placeHolders.keySet().stream()
                    .filter(params::matches)
                    .findFirst()
                    .orElse(null);
            if (regexTest != null) {
                return placeHolders.get(regexTest).apply(name, params);
            }
        }
        return "";
    }
}
