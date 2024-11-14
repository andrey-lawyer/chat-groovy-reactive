package com.chat.handler

import com.chat.repository.UserRepository
import com.chat.service.MessageService
import com.chat.service.UserService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;


@Component
class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final UserService userService;
    private final Sinks.Many<String> messageSink = Sinks.many().multicast().onBackpressureBuffer();

   ChatWebSocketHandler(UserRepository userRepository, MessageService messageService,UserService userService) {
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    Mono<Void> handle(WebSocketSession session) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> processSession(session, securityContext))
                .then();
    }

    private Mono<Void> processSession(WebSocketSession session, SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = userService.getUserRole(authentication);
            boolean isAdmin = "ROLE_ADMIN" == role;

            logger.info("Authenticated user: " + username + ", role: " + role);

            Flux<String> history = messageService.loadMessageHistory();
            Flux<String> messageFlux = messageSink.asFlux();
            Flux<String> receive = processIncomingMessages(session, username, isAdmin);

            return session.send(Flux.merge(history, messageFlux, receive).map(session::textMessage));
        } else {
            logger.warn("User is not authenticated.");
            return Mono.error(new RuntimeException("User is not authenticated"));
        }
    }

    private Flux<String> processIncomingMessages(WebSocketSession session, String username, boolean isAdmin) {
        return session.receive()
                .map(msg -> msg.getPayloadAsText())
                .flatMap(content -> {
                    Map<String, Object> messageData = messageService.parseMessage(content);
                    if (messageData == null) return Mono.empty();

                    if (messageData.containsKey("deleteId")) {
                        return messageService.handleDeleteMessage(messageData.get("deleteId").toString(), username, isAdmin)
                                .flatMap(messageJson -> {
                                    if (!messageJson.startsWith("Error")) {
                                        broadcastMessage(messageJson);
                                        return Mono.empty();
                                    } else {
                                        return Mono.just(messageJson);
                                    }
                                });
                    } else if (messageData.containsKey("text")) {
                        return messageService.handleSaveMessage(messageData.get("text").toString(), username)
                                .doOnSuccess(this::broadcastMessage)
                                .then(Mono.empty());
                    } else {
                        logger.warn("Unknown command received");
                        return Mono.empty();
                    }
                }) as Flux<String>;
    }



    private void broadcastMessage(String messageJson) {
        messageSink.tryEmitNext(messageJson);
    }
}
