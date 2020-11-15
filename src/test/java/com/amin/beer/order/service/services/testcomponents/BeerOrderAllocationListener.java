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
        boolean pendingInventory = allocateOrderRequest.getBeerOrderDto().getCustomerRef() != null
                && allocateOrderRequest.getBeerOrderDto().getCustomerRef().equals("partial-allocation");
        boolean allocationError = allocateOrderRequest.getBeerOrderDto().getCustomerRef() != null
                && allocateOrderRequest.getBeerOrderDto().getCustomerRef().equals("fail-allocation");
        boolean sendResponse = allocateOrderRequest.getBeerOrderDto().getCustomerRef() == null
                || !allocateOrderRequest.getBeerOrderDto().getCustomerRef().equals("dont-allocate");
        if (sendResponse) {
            if (!allocationError) {
                allocateOrderRequest.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if (pendingInventory) {
                        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
                    } else {
                        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
                    }
                });
            }
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResult.builder().beerOrderDto(allocateOrderRequest.getBeerOrderDto())
                            .pendingInventory(pendingInventory).allocationError(allocationError).build());
        }
    }
}
