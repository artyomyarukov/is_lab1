package com.yarukov.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class WebSocketNotificationAspect {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @Pointcut("@annotation(com.yarukov.aop.NotifyClients)")
    public void notifyClientsPointcut() {}


    @AfterReturning("notifyClientsPointcut()")
    public void sendWebSocketUpdate() {
        try {
            messagingTemplate.convertAndSend("/topic/cities", "UPDATED");
        } catch (Exception ignored) {
        }
    }
}