package rs.onako2.redirectplayers;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Config {

    @Inject
    private static Logger logger;
    public static Path dataDirectory;

    static Yaml yaml = new Yaml();

    static InputStream inputStream = null;

    public static String getConfig(String input) {


        Path configPath = dataDirectory.resolve("config.yml");

        try {
            inputStream = Files.newInputStream(configPath);
            Map<String, Object> obj = yaml.load(inputStream);
            Object output = obj.get(input);
            return String.valueOf(output);
        } catch (IOException e) {
            logger.error("Failed to load config.yml from " + dataDirectory, e);
            return null;
        } finally {
            // Close the InputStream in a finally block
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Failed to close InputStream", e);
                }
            }
        }
    }
}
