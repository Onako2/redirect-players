package rs.onako2.redirectplayers;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerCloseEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

public class ListenerCloseListener {

    private final ProxyServer server;

    public ListenerCloseListener(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onListenerClose(ListenerCloseEvent event) {
        ProxyServer server = this.server;
        Collection<Player> players = server.getAllPlayers();
        InetSocketAddress address = new InetSocketAddress(Objects.requireNonNull(Config.getConfig("host")), Integer.parseInt(Objects.requireNonNull(Config.getConfig("port"))));
        for (Player player : players) {
            player.transferToHost(address);
            System.out.println("Redirecting player " + player.getUsername() + " to " + Config.getConfig("redirect-server"));
        }
    }
}
