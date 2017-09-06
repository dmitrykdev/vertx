package vertxClient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

/**
 * Client-side module.
 * <p>
 * Client interact with HOST, receive messages and display in LOG
 * IMPORTANT: In case of non localhost deployment change HOST to proper address
 */
public class App extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String HOST = "127.0.0.1";
    private static final String MQTT_TOPIC = "random-data";
    private static final Integer WAIT = 10000;
    private static final Integer PORT = 9090;
    private static final Integer AT_LEAST_ONCE = 1;

    private MqttClient client;

    @Override
    public void start() {
        MqttClientOptions options = new MqttClientOptions();
        options.setHost(HOST)
                .setPort(PORT);

        client = MqttClient.create(vertx, options);

        client.publishHandler(message ->
                LOGGER.info("Message received: " + message.payload()
                        + " | " + message.topicName() + " | " + message.qosLevel()));

        client.subscribeCompleteHandler(h ->
                vertx.setTimer(WAIT, l -> client.unsubscribe(MQTT_TOPIC)));

        client.unsubscribeCompleteHandler(h -> {
            LOGGER.info(">> UNSUBACK received from server");
            stop();
        });

        client.connect(ch -> {
            if (ch.succeeded()) {
                LOGGER.info(">> Connection established");
                client.subscribe(MQTT_TOPIC, AT_LEAST_ONCE, h -> {
                    if (h.succeeded()) {
                        LOGGER.info(">> Subscribed on " + MQTT_TOPIC);
                    } else {
                        LOGGER.error(">> Unable to subscribe on " + MQTT_TOPIC, h.cause());
                    }
                });
            } else {
                LOGGER.error("Unable to establish connection", ch.cause());
            }
        });
    }

    @Override
    public void stop() {
        client.disconnect(h -> LOGGER.info(">> Disconnected"));
    }
}
