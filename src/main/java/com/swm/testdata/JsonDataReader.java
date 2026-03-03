package com.swm.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class JsonDataReader {
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static Map<String, Object> readJsonFile(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), Map.class);
    }
    
    public static <T> T readJsonFile(String filePath, Class<T> clazz) throws IOException {
        return mapper.readValue(new File(filePath), clazz);
    }
}
