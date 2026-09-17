package com.kurniadhi.mcp.basic.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Bean
    public DataSource dataSource() throws IOException {
        // 1. Extract the native binary out of the JAR to the local machine file system
        File nativeExtension = extractExtensionToDisk();

        // 2. Wire up HikariCP manually
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.addDataSourceProperty("enable_load_extension", "true");

        // Normalize file paths so Windows backslashes won't break the SQL string literal
        String sanitizedPath = nativeExtension.getAbsolutePath().replace("\\", "/");

        // 3. Force every connection in the pool to load the vector extension immediately on spin-up
//        config.setConnectionInitSql("SELECT load_extension('" + sanitizedPath + "');");
        config.setConnectionInitSql(
                "SELECT load_extension('" + sanitizedPath + "', 'sqlite3_vector_init'); " +
                        "SELECT vector_init('documents', 'embed', 'type=FLOAT32,dimension=384,distance=cosine');"
        );

        return new HikariDataSource(config);
    }

    private File extractExtensionToDisk() throws IOException {
        // Detect OS type to pull the correct compiled library
        String os = System.getProperty("os.name").toLowerCase();
        String filename = os.contains("mac") ? "libsqlite_vector.dylib"
                : os.contains("win") ? "sqlite_vector.dll" : "libsqlite_vector.so";

        // Define a safe workspace subdirectory under user home to hold the binary runtime
        Path targetFolder = Paths.get(System.getProperty("user.home"), ".app_runtime", "extensions");
        Files.createDirectories(targetFolder);
        File targetFile = targetFolder.resolve(filename).toFile();

        // Copy the stream from classpath resources to disk if it doesn't exist yet
        if (!targetFile.exists()) {
            try (InputStream is = getClass().getResourceAsStream("/sqlite-vec/" + filename)) {
                if (is == null) {
                    throw new FileNotFoundException("Could not find native asset /sqlite-vec/" + filename + " inside the JAR.");
                }
                Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return targetFile;
    }
}
