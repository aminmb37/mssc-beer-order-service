package com.amin.beer.order.service.sm.actions;

import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = stateContext.getMessage().getHeaders()
                .get(BeerOrderManagerImpl.ORDER_ID_HEADER, String.class);
        log.error("Compensating Transaction..... Validation Failed: " + beerOrderId);
    }
}
