package com.chat.config

import com.chat.handler.ChatWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
@EnableWebFlux
class WebSocketConfig implements WebFluxConfigurer {


    @Bean
    HandlerMapping webSocketMapping(ChatWebSocketHandler chatWebSocketHandler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping()
        mapping.setUrlMap([ "/api/ws/chat": chatWebSocketHandler ])
//        mapping.setOrder(10)
        return mapping
    }
}



