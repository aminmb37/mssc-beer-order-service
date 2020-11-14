package com.amin.beer.order.service.services.testcomponents;

import com.amin.beer.order.service.config.JmsConfig;
import com.amin.brewery.model.events.AllocateOrderRequest;
import com.amin.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderAllocationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message<AllocateOrderRequest> message) {
        AllocateOrderRequest allocateOrderRequest = message.getPayload();
        allocateOrderRequest.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto ->
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity()));
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResult.builder().beerOrderDto(allocateOrderRequest.getBeerOrderDto())
                        .pendingInventory(false).allocationError(false).build());
    }
}
