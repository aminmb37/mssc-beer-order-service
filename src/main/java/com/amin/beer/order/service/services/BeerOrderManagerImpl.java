package com.amin.beer.order.service.services;

import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.repositories.BeerOrderRepository;
import com.amin.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderManagerImpl implements BeerOrderManager {
    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final StateMachineInterceptor<BeerOrderStatusEnum, BeerOrderEventEnum> beerOrderStateChangeInterceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Override
    @Transactional
    public void processValidationResult(UUID beerOrderId, boolean isValid) {
        log.debug("Process validation result for beerOrderId: " + beerOrderId + " Valid? " + isValid);
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if (isValid) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
                Optional<BeerOrder> validatedOrderOptional = beerOrderRepository.findById(beerOrderId);
                if (validatedOrderOptional.isPresent()) {
                    BeerOrder validatedOrder = validatedOrderOptional.get();
                    sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
                }
            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("Order not found. Id: " + beerOrderId));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            updateAllocatedQuantity(beerOrderDto);
        }, () -> log.error("Order not found. Id: " + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            updateAllocatedQuantity(beerOrderDto);
        }, () -> log.error("Order not found. Id: " + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder ->
                        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED),
                () -> log.error("Order not found. Id: " + beerOrderDto.getId()));
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
        Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString()).build();
        stateMachine.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine =
                stateMachineFactory.getStateMachine(beerOrder.getId());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor().doWithAllRegions(stateMachineAccess -> {
            stateMachineAccess.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
            stateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(
                    beerOrder.getOrderStatus(), null, null, null));
        });
        stateMachine.start();
        return stateMachine;
    }

    private void updateAllocatedQuantity(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(
                    beerOrderLine -> beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                        if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                            beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                        }
                    }));
            beerOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("Order not found. Id: " + beerOrderDto.getId()));
    }
}
