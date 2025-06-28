package rs.onako2.redirectplayers;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.geysermc.geyser.api.GeyserApi;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

public class ListenerCloseListener {

    private final ProxyServer server;
    private final Logger logger;

    public ListenerCloseListener(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void onListenerClose(ListenerCloseEvent event) {
        logger.info("Redirecting players to other server!");
        ProxyServer server = this.server;
        Collection<Player> players = server.getAllPlayers();
        final InetSocketAddress address = new InetSocketAddress(Objects.requireNonNull(Config.getConfig("host")), Integer.parseInt(Objects.requireNonNull(Config.getConfig("port"))));
        boolean isGeyserInstalled;
        try {
            GeyserApi.api().platformType();
            isGeyserInstalled = true;
        } catch (NoClassDefFoundError e) {
            isGeyserInstalled = false;
        }
        for (Player player : players) {
            if (!isGeyserInstalled || !GeyserApi.api().isBedrockPlayer(player.getUniqueId())) {
                player.transferToHost(address);
            }
        }
    }
}
