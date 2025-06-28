package rs.onako2.redirectplayers;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Config {

    public static Logger logger;
    public static Path dataDirectory;

    static Yaml yaml = new Yaml();

    @Nullable
    public static String getConfig(String input) {
        Path configPath = dataDirectory.resolve("config.yml");

        try {
            String configString = Files.readString(configPath);
            Map<String, Object> obj = yaml.load(configString);
            Object output = obj.get(input);
            if (output == null) return null;
            return String.valueOf(output);
        } catch (IOException e) {
            logger.error("Failed to load config.yml from {}", dataDirectory, e);
            return null;
        }
    }
}
