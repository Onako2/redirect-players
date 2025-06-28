package rs.onako2.redirectplayers;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Plugin(
        id = "redirect-players",
        name = "redirect-players",
        version = BuildConstants.VERSION,
        description = "Redirect players when the Velocity server shuts down",
        authors = {"Onako2"}
)
public class RedirectPlayers {
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    @Inject
    public RedirectPlayers(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        Config.dataDirectory = dataDirectory;
        Config.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting redirect-players");
        Path configPath = dataDirectory.resolve("config.yml");

        logger.debug(configPath.toString());

        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to create directory {}", dataDirectory, e);
                return;
            }
        }

        // put config.yml from resources to dataDirectory
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    logger.error("Could not find config.yml in resources");
                    return;
                }
                Files.copy(in, configPath);
                logger.info("Copied config.yml to {}", dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to copy config.yml to {}", dataDirectory, e);
            }
        }

        // migrate config in case config isn't updated yet
        // 1.0 to 1.1 migration
        try {
            String configString = Files.readString(configPath);
            String versionString = Config.getConfig("version");
            if (versionString == null || versionString.isEmpty()) {
                versionString = "0";
            }
            int version = Integer.parseInt(versionString);
            if (version == 0) {
                configString = configString +
                        """
                                
                                # Bedrock Config (In case you are using Geyser)
                                bedrock-host: bedrock.example.com
                                bedrock-port: 19132
                                
                                # DO NOT CHANGE THIS. YOUR CONFIG MIGHT BREAK.
                                version: 1
                                """;
                Files.writeString(configPath, configString);
                version = 1;
            }
            if (!Objects.equals(versionString, Integer.toString(version))) {
                logger.info("Successful migration of config from version {} to {}", versionString, version);
            }
        } catch (IOException e) {
            logger.error("Failed to read config.yml from {}. The config might be incomplete because migrations couldn't be completed", configPath, e);
        }

        // show all options
        try {
            logger.info(Files.readString(configPath));
        } catch (IOException e) {
            logger.error("Failed to read config.yml from {}", configPath, e);
        }
        server.getEventManager().register(this, new ListenerCloseListener(server, logger));
        server.getEventManager().register(this, new ListenerPlayerJoin(logger));

        int pluginId = 26299;
        Metrics metrics = metricsFactory.make(this, pluginId);

        logger.info("Redirect-players plugin loaded!");
    }
}
