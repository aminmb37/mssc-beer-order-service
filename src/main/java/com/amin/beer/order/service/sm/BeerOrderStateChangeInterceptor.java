package com.amin.beer.order.service.sm;

import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.repositories.BeerOrderRepository;
import com.amin.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderStateChangeInterceptor extends
        StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final BeerOrderRepository beerOrderRepository;

    @Override
    @Transactional
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                               Message<BeerOrderEventEnum> message,
                               Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                               StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
        log.debug("Pre-State Change");
        Optional.ofNullable(message).flatMap(beerOrderEventEnumMessage ->
                Optional.ofNullable(beerOrderEventEnumMessage.getHeaders()
                        .get(BeerOrderManagerImpl.ORDER_ID_HEADER, String.class))).ifPresent(orderId -> {
            log.debug("Saving state for order id: " + orderId + " Status: " + state.getId());
            BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(orderId));
            beerOrder.setOrderStatus(state.getId());
            beerOrderRepository.saveAndFlush(beerOrder);
        });
    }
}
