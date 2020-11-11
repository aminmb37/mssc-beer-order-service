package com.amin.beer.order.service.services.listeners;

import com.amin.beer.order.service.config.JmsConfig;
import com.amin.beer.order.service.services.BeerOrderManager;
import com.amin.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderAllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult allocateOrderResult) {
        if (allocateOrderResult.getAllocationError()) {
            beerOrderManager.beerOrderAllocationFailed(allocateOrderResult.getBeerOrderDto());
        } else if (allocateOrderResult.getPendingInventory()) {
            beerOrderManager.beerOrderAllocationPendingInventory(allocateOrderResult.getBeerOrderDto());
        } else {
            beerOrderManager.beerOrderAllocationPassed(allocateOrderResult.getBeerOrderDto());
        }
    }
}
