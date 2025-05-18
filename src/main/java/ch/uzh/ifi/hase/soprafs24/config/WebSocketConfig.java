package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Value("${WEBSOCKET_ALLOWED_ORIGINS:http://localhost:3000,https://sopra-fs25-group-11-client.vercel.app}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple memory-based message broker to send messages to clients
        // The client will subscribe to these destinations to receive messages
        config.enableSimpleBroker("/topic");

        // Set the application destination prefix
        // Messages sent from clients to the server will be prefixed with this
        config.setApplicationDestinationPrefixes("/app");

        logger.info("WebSocket message broker configured with topics and application prefix");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the STOMP endpoints
        // This allows clients to connect to the WebSocket server
        String[] origins = allowedOrigins.split(",");
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS()
                .setWebSocketEnabled(true)
                .setSupressCors(false); // Ensure CORS is properly handled

        logger.info("WebSocket STOMP endpoints registered");
        logger.info("Allowed origins: {}", String.join(", ", origins));
    }
}