package com.amin.beer.order.service.sm.actions;

import com.amin.beer.order.service.config.JmsConfig;
import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.repositories.BeerOrderRepository;
import com.amin.beer.order.service.services.BeerOrderManagerImpl;
import com.amin.beer.order.service.web.mappers.BeerOrderMapper;
import com.amin.brewery.model.events.ValidateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = stateContext.getMessage().getHeaders()
                .get(BeerOrderManagerImpl.ORDER_ID_HEADER, String.class);
        if (beerOrderId != null) {
            Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));
            beerOrderOptional.ifPresent(beerOrder -> {
                jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE,
                        ValidateOrderRequest.builder().beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder)).build());
                log.debug("Sent validation request to queue for order id: " + beerOrderId);
            });
        }
    }
}
