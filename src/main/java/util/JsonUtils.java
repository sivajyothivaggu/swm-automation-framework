package com.swm.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;

public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static <T> T readJson(String filePath, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(filePath), clazz);
    }
    
    public static String toJson(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}
