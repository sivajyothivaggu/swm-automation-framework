package com.swm.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to read JSON data from files into Java objects using Jackson.
 *
 * This class provides methods to:
 * - Read a JSON file into a Map<String, Object>.
 * - Read a JSON file into a specified POJO type.
 * - Optional-returning variants that return Optional.empty() on failure (no exception).
 *
 * All public APIs perform validation on the provided file path, log errors, and use
 * try-with-resources to ensure streams are closed.
 */
public final class JsonDataReader {

    private static final Logger LOGGER = Logger.getLogger(JsonDataReader.class.getName());
    private static final ObjectMapper object_mapper = new ObjectMapper();

    private JsonDataReader() {
        // Utility class - prevent instantiation
    }

    /**
     * Read a JSON file and deserialize it into a Map<String, Object>.
     *
     * @param file_path the path to the JSON file
     * @return a Map representation of the JSON file
     * @throws IOException if an IO error occurs while reading or parsing the file
     * @throws IllegalArgumentException if file_path is null or empty
     */
    public static Map<String, Object> readJsonFile(String file_path) throws IOException {
        if (Objects.isNull(file_path) || file_path.trim().isEmpty()) {
            throw new IllegalArgumentException("file_path must not be null or empty");
        }

        final Path path = Paths.get(file_path);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + file_path);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + file_path);
        }

        try (InputStream input_stream = Files.newInputStream(path)) {
            return object_mapper.readValue(input_stream, new TypeReference<Map<String, Object>>() {});
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read or parse JSON file: " + file_path, ex);
            throw ex;
        } catch (RuntimeException ex) {
            // Catch other runtime exceptions (e.g. Jackson mapping issues) to ensure logging
            LOGGER.log(Level.SEVERE, "Unexpected error while reading JSON file: " + file_path, ex);
            throw ex;
        }
    }

    /**
     * Read a JSON file and deserialize it into the provided POJO class.
     *
     * @param file_path the path to the JSON file
     * @param class_type the target class to deserialize into
     * @param <T> the target type
     * @return an instance of T populated from the JSON file
     * @throws IOException if an IO error occurs while reading or parsing the file
     * @throws IllegalArgumentException if file_path or class_type is null or invalid
     */
    public static <T> T readJsonFile(String file_path, Class<T> class_type) throws IOException {
        if (Objects.isNull(file_path) || file_path.trim().isEmpty()) {
            throw new IllegalArgumentException("file_path must not be null or empty");
        }
        if (Objects.isNull(class_type)) {
            throw new IllegalArgumentException("class_type must not be null");
        }

        final Path path = Paths.get(file_path);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + file_path);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + file_path);
        }

        try (InputStream input_stream = Files.newInputStream(path)) {
            return object_mapper.readValue(input_stream, class_type);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read or parse JSON file: " + file_path + " into " + class_type.getName(), ex);
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while reading JSON file: " + file_path + " into " + class_type.getName(), ex);
            throw ex;
        }
    }

    /**
     * Read a JSON file and deserialize it into a Map<String, Object>, returning an Optional.
     * This variant will return Optional.empty() on failure and will NOT throw an exception.
     *
     * @param file_path the path to the JSON file
     * @return Optional containing the Map if successful, or Optional.empty() on error
     */
    public static Optional<Map<String, Object>> readJsonFileOptional(String file_path) {
        try {
            return Optional.ofNullable(readJsonFile(file_path));
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Optional read failed for file: " + file_path, ex);
            return Optional.empty();
        }
    }

    /**
     * Read a JSON file and deserialize it into the specified POJO type, returning an Optional.
     * This variant will return Optional.empty() on failure and will NOT throw an exception.
     *
     * @param file_path the path to the JSON file
     * @param class_type the target class to deserialize into
     * @param <T> the target type
     * @return Optional containing the deserialized object if successful, or Optional.empty() on error
     */
    public static <T> Optional<T> readJsonFileOptional(String file_path, Class<T> class_type) {
        try {
            return Optional.ofNullable(readJsonFile(file_path, class_type));
        } catch (IOException | IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Optional read failed for file: " + file_path + " into " + (class_type == null ? "null" : class_type.getName()), ex);
            return Optional.empty();
        }
    }
}