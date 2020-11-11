package com.amin.brewery.model.events;

import com.amin.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AllocateOrderRequest {
    private BeerOrderDto beerOrderDto;
}
