package rs.onako2.redirectplayers;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.DisconnectPacket;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import org.geysermc.geyser.api.GeyserApi;
import org.slf4j.Logger;

import java.util.Objects;

public class ListenerPlayerJoin {

    private final Logger logger;

    public ListenerPlayerJoin(Logger logger) {
        this.logger = logger;
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void onPlayerJoin(PostLoginEvent event) {
        if (event.getPlayer() instanceof ConnectedPlayer player) {
            try {
                // Access the Netty pipeline
                player.getConnection().getChannel().pipeline().addBefore(
                        "handler", "packet_interceptor", new ChannelDuplexHandler() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                // We do nice stuff here :)
                                if (msg instanceof DisconnectPacket disconnectPacket) {
                                    if (player.translateMessage(Component.translatable("velocity.kick.shutdown")).equals(disconnectPacket.getReason().getComponent())) {
                                        final GeyserApi geyserApi = GeyserApi.api();
                                        final String hostBedrock = Config.getConfig("bedrock-host");
                                        final String portBedrock = Config.getConfig("bedrock-port");
                                        if (hostBedrock == null || portBedrock == null) {
                                            logger.error("REDIRECTING BEDROCK PLAYERS FAILED! Missing options: bedrock-host and bedrock-port. Please fix this!");
                                        }
                                        if (hostBedrock != null && portBedrock != null && geyserApi.isBedrockPlayer(player.getUniqueId())) {
                                            try {
                                                Objects.requireNonNull(geyserApi.connectionByUuid(player.getUniqueId())).transfer(hostBedrock, Integer.parseInt(portBedrock));
                                            } catch (NumberFormatException e) {
                                                logger.error("Port is a non-int value! Please fix this!", e);
                                            }
                                        }

                                        return;
                                    }
                                }
                                super.write(ctx, msg, promise);
                            }
                        });
            } catch (Exception e) {
                logger.error("Failed to register packet listener for {}", player.getUsername(), e);
            }
        }
    }
}
