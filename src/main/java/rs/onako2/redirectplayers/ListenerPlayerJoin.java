package rs.onako2.redirectplayers;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.DisconnectPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import org.geysermc.geyser.api.GeyserApi;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Objects;

public class ListenerPlayerJoin {

    private final Logger logger;

    public ListenerPlayerJoin(Logger logger) {
        this.logger = logger;
    }

    private static String host;
    private static int port = -1;
    private static String hostBedrock;
    private static String portBedrock;

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
                                        boolean isGeyserInstalled;
                                        GeyserApi geyserApi = null;
                                        try {
                                            geyserApi = GeyserApi.api();
                                            isGeyserInstalled = true;
                                        } catch (NoClassDefFoundError e) {
                                            isGeyserInstalled = false;
                                        }
                                        if (hostBedrock == null || portBedrock == null) {
                                            hostBedrock = Config.getConfig("bedrock-host");
                                            portBedrock = Config.getConfig("bedrock-port");
                                        }
                                        if (host == null || port > -1) {
                                            try {
                                                host = Config.getConfig("host");
                                                port = Integer.parseInt(Objects.requireNonNull(Config.getConfig("port")));
                                            } catch (Exception e) {
                                                logger.error("Port not found in the config!", e);
                                            }
                                        }
                                        try {
                                            if (isGeyserInstalled && geyserApi.isBedrockPlayer(player.getUniqueId())) {
                                                boolean mayTransferBedrock = true;
                                                if (hostBedrock == null || portBedrock == null) {
                                                    logger.error("REDIRECTING BEDROCK PLAYERS FAILED! Missing options: bedrock-host or (and maybe and) bedrock-port. Please fix this!");
                                                    mayTransferBedrock = false;
                                                }
                                                if (mayTransferBedrock) {
                                                    logger.info("Transfer Bedrock");
                                                    Objects.requireNonNull(geyserApi.connectionByUuid(player.getUniqueId())).transfer(hostBedrock, Integer.parseInt(portBedrock));
                                                } else {
                                                    disconnectPacket.setReason(
                                                            new ComponentHolder(player.getProtocolVersion(), Component.text("IMPORTANT: TELL THE ADMIN TO FIX THE REDIRECT-PLAYERS CONFIG!!\nThe config is currently broken."))
                                                    );
                                                    super.write(ctx, disconnectPacket, promise);
                                                }
                                            } else {
                                                if (hostBedrock == null || portBedrock == null) {
                                                    disconnectPacket.setReason(
                                                            new ComponentHolder(player.getProtocolVersion(), Component.text("IMPORTANT: TELL THE ADMIN TO FIX THE REDIRECT-PLAYERS CONFIG!!\nThe config is currently broken."))
                                                    );
                                                    super.write(ctx, disconnectPacket, promise);
                                                } else {
                                                    player.transferToHost(
                                                            new InetSocketAddress(host, port)
                                                    );
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error("An error occurred!", e);
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
