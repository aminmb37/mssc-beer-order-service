package com.amin.beer.order.service.services.testcomponents;

import com.amin.beer.order.service.config.JmsConfig;
import com.amin.brewery.model.events.ValidateOrderRequest;
import com.amin.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<ValidateOrderRequest> message) {
        ValidateOrderRequest validateOrderRequest = message.getPayload();
        boolean isValid = validateOrderRequest.getBeerOrderDto().getCustomerRef() == null
                || !validateOrderRequest.getBeerOrderDto().getCustomerRef().equals("fail-validation");
        boolean sendResponse = validateOrderRequest.getBeerOrderDto().getCustomerRef() == null
                || !validateOrderRequest.getBeerOrderDto().getCustomerRef().equals("dont-validate");
        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE, ValidateOrderResult
                    .builder().isValid(isValid).orderId(validateOrderRequest.getBeerOrderDto().getId()).build());
        }
    }
}
