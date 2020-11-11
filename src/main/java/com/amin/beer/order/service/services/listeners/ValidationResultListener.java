package com.amin.beer.order.service.services.listeners;

import com.amin.beer.order.service.config.JmsConfig;
import com.amin.beer.order.service.services.BeerOrderManager;
import com.amin.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResult validateOrderResult) {
        UUID beerOrderId = validateOrderResult.getOrderId();
        log.debug("Validation result for order Id: " + beerOrderId);
        beerOrderManager.processValidationResult(beerOrderId, validateOrderResult.getIsValid());
    }
}
