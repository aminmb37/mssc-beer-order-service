package com.amin.beer.order.service.services;

import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {
    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final StateMachineInterceptor<BeerOrderStatusEnum, BeerOrderEventEnum> beerOrderStateMachineInterceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
        Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId()).build();
        stateMachine.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine =
                stateMachineFactory.getStateMachine(beerOrder.getId());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor().doWithAllRegions(stateMachineAccess -> {
            stateMachineAccess.addStateMachineInterceptor(beerOrderStateMachineInterceptor);
            stateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(
                    beerOrder.getOrderStatus(), null, null, null));
        });
        stateMachine.start();
        return stateMachine;
    }
}
