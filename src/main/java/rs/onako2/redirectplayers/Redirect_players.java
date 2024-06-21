package rs.onako2.redirectplayers;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "redirect-players",
        name = "redirect-players",
        version = BuildConstants.VERSION,
        description = "Redirect players when the Velocity server shuts down",
        authors = {"Onako2"}
)
public class Redirect_players {
    private final Logger logger;
    private ProxyServer server;
    private final Path dataDirectory;

    @Inject
    public Redirect_players(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        Config.dataDirectory = dataDirectory;
        Config.logger = logger;
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Path configPath = dataDirectory.resolve("config.yml");

        logger.info(configPath.toString());

        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to create directory " + dataDirectory, e);
                return;
            }
        }

        //put config.yml from resources to dataDirectory
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    logger.error("Could not find config.yml in resources");
                    return;
                }
                Files.copy(in, configPath);
                logger.info("Copied config.yml to " + dataDirectory);
            } catch (IOException e) {
                logger.error("Failed to copy config.yml to " + dataDirectory, e);
            }
        }


        server.getEventManager().register(this, new ListenerCloseListener(server));
        logger.info("Redirect-players plugin loaded!");
    }
}
