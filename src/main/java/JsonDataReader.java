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
 * <p>This class provides methods to:
 * - Read a JSON file into a Map<String, Object>.
 * - Read a JSON file into a specified POJO type.
 * - Optional-returning variants that return Optional.empty() on failure (no exception).
 *
 * <p>All public APIs perform validation on the provided file path and other arguments,
 * log errors, and use try-with-resources to ensure streams are closed.
 *
 * <p>Usage examples:
 * <pre>
 *   Map<String, Object> map = JsonDataReader.readJsonFile("/tmp/data.json");
 *   Optional&lt;Map&lt;String, Object&gt;&gt; opt = JsonDataReader.readJsonFileOptional("/tmp/data.json");
 *   MyPojo pojo = JsonDataReader.readJsonFile("/tmp/data.json", MyPojo.class);
 *   Optional&lt;MyPojo&gt; pojoOpt = JsonDataReader.readJsonFileOptional("/tmp/data.json", MyPojo.class);
 * </pre>
 */
public final class JsonDataReader {

    /**
     * Logger for diagnostic and error messages.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonDataReader.class.getName());

    /**
     * Shared ObjectMapper instance. Jackson's ObjectMapper is thread-safe for readValue operations.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Private constructor to prevent instantiation of utility class.
    private JsonDataReader() {
        // no-op
    }

    /**
     * Read a JSON file and deserialize it into a Map<String, Object>.
     *
     * @param filePath the path to the JSON file (must not be null or empty)
     * @return a Map representation of the JSON file
     * @throws IOException              if an IO error occurs while reading or parsing the file
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static Map<String, Object> readJsonFile(String filePath) throws IOException {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be null or empty");
        }

        final Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + filePath);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            Map<String, Object> result = OBJECT_MAPPER.readValue(inputStream, new TypeReference<Map<String, Object>>() {
            });
            LOGGER.log(Level.FINE, "Successfully read JSON file into Map: {0}", filePath);
            return result;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read or parse JSON file: " + filePath, ex);
            throw ex;
        } catch (RuntimeException ex) {
            // Catch other runtime exceptions (e.g. Jackson mapping issues) to ensure logging
            LOGGER.log(Level.SEVERE, "Unexpected error while reading JSON file: " + filePath, ex);
            throw ex;
        }
    }

    /**
     * Read a JSON file and deserialize it into the provided POJO class.
     *
     * @param filePath  the path to the JSON file (must not be null or empty)
     * @param classType the target class to deserialize into (must not be null)
     * @param <T>       the target type
     * @return an instance of T populated from the JSON file
     * @throws IOException              if an IO error occurs while reading or parsing the file
     * @throws IllegalArgumentException if filePath or classType is null or invalid
     */
    public static <T> T readJsonFile(String filePath, Class<T> classType) throws IOException {
        if (Objects.isNull(filePath) || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be null or empty");
        }
        if (Objects.isNull(classType)) {
            throw new IllegalArgumentException("classType must not be null");
        }

        final Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + filePath);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            T result = OBJECT_MAPPER.readValue(inputStream, classType);
            LOGGER.log(Level.FINE, "Successfully read JSON file into {0}: {1}", new Object[]{classType.getName(), filePath});
            return result;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read or parse JSON file: " + filePath + " into " + classType.getName(), ex);
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while reading JSON file: " + filePath + " into " + classType.getName(), ex);
            throw ex;
        }
    }

    /**
     * Read a JSON file and deserialize it into a Map<String, Object>, returning an Optional.
     * This variant will return Optional.empty() on failure and will NOT throw an exception.
     *
     * @param filePath the path to the JSON file
     * @return Optional containing the Map if successful, or Optional.empty() on error
     */
    public static Optional<Map<String, Object>> readJsonFileOptional(String filePath) {
        try {
            Map<String, Object> map = readJsonFile(filePath);
            return Optional.ofNullable(map);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Invalid argument provided for optional read: " + filePath, ex);
            return Optional.empty();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Optional read failed for file: " + filePath, ex);
            return Optional.empty();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Unexpected error during optional read for file: " + filePath, ex);
            return Optional.empty();
        }
    }

    /**
     * Read a JSON file and deserialize it into the provided POJO class, returning an Optional.
     * This variant will return Optional.empty() on failure and will NOT throw an exception.
     *
     * @param filePath  the path to the JSON file
     * @param classType the target class to deserialize into
     * @param <T>       the target type
     * @return Optional containing the deserialized object if successful, or Optional.empty() on error
     */
    public static <T> Optional<T> readJsonFileOptional(String filePath, Class<T> classType) {
        try {
            T value = readJsonFile(filePath, classType);
            return Optional.ofNullable(value);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Invalid argument provided for optional read into " + (Objects.isNull(classType) ? "null" : classType.getName()) + ": " + filePath, ex);
            return Optional.empty();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Optional read failed for file: " + filePath + " into " + (Objects.isNull(classType) ? "null" : classType.getName()), ex);
            return Optional.empty();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Unexpected error during optional read for file: " + filePath + " into " + (Objects.isNull(classType) ? "null" : classType.getName()), ex);
            return Optional.empty();
        }
    }
}