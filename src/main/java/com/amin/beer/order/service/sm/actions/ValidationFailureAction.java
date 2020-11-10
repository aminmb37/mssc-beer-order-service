package com.amin.beer.order.service.sm.actions;

import com.amin.beer.order.service.domain.BeerOrderEventEnum;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

    }
}
