package vertxServer;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Server-side module.
 * <p>
 * Provide endpoint for clients of shared source using proxy-like approach.
 */
public class App extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String MQTT_TOPIC = "random-data";
    private static final Integer PORT = 9090;
    private static final Integer INTERVAL = 100;

    private MqttServer server;

    /*
     * "Proxy" for shared source
     */
    private Integer randomData;

    @Override
    public void start() {
        server = MqttServer.create(this.vertx);

        /*
         * Source volatility
         */
        vertx.setPeriodic(INTERVAL, h -> randomData = new Random().nextInt(1000));

        server.endpointHandler(endpoint -> {

            LOGGER.info(">> Client connection request: " + endpoint.clientIdentifier());

            endpoint.accept(false);

            vertx.setPeriodic(INTERVAL, h -> {
                if (endpoint.isConnected()) {
                    endpoint.publish(MQTT_TOPIC,
                            Buffer.buffer(String.valueOf(randomData)),
                            MqttQoS.AT_LEAST_ONCE,
                            false,
                            false
                    );
                }
            });

            endpoint.subscribeHandler(subscribe -> {
                List<MqttQoS> QoSTypes = new ArrayList<>();
                for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                    QoSTypes.add(s.qualityOfService());
                }

                endpoint.subscribeAcknowledge(subscribe.messageId(), QoSTypes);
            });

            endpoint.unsubscribeHandler(unsubscribe -> {
                for (String topic : unsubscribe.topics()) {
                    LOGGER.info(">> Client request to unsubscribe from: " + topic);
                }
                endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
            });

            endpoint.disconnectHandler(h -> LOGGER.info(">> Client has been disconnected"))
                    .closeHandler(h -> LOGGER.info(" >> Connection is closed"));

        }).listen(PORT, ar -> {
            if (ar.succeeded()) {
                LOGGER.info(">> Port " + PORT + " is open");
            } else {
                LOGGER.error(">> Unable to open port: " + ar.cause());
                stop();
            }
        }).exceptionHandler(Throwable::printStackTrace);
    }

    @Override
    public void stop() {
        server.close((h) -> LOGGER.info(">> Stopping Server verticle"));
    }
}
