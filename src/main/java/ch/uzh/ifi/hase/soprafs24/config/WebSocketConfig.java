package ch.uzh.ifi.hase.soprafs24.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js")
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(true); // Enable session cookies for SockJS fallback

        logger.info("WebSocket STOMP endpoints registered");
        logger.info("Allowed origins: {}", String.join(", ", origins));
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024) // 64KB
                .setSendBufferSizeLimit(512 * 1024) // 512KB
                .setSendTimeLimit(20000); // 20 seconds

        logger.info("WebSocket transport configured with size and time limits");
    }
}