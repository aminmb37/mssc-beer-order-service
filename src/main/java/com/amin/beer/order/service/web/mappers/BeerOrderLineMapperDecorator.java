package com.amin.beer.order.service.web.mappers;

import com.amin.beer.order.service.domain.BeerOrderLine;
import com.amin.beer.order.service.services.beer.BeerService;
import com.amin.beer.order.service.web.model.BeerDto;
import com.amin.beer.order.service.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
    private BeerService beerService;

    private BeerOrderLineMapper beerOrderLineMapper;


    @Autowired
    public void setBeerService(BeerService beerService)
    {
        this.beerService = beerService;
    }


    @Autowired
    @Qualifier("delegate")
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper)
    {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }


    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line)
    {
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        Optional<BeerDto> beerDtoOptional = beerService.getBeerByUpc(line.getUpc());
        beerDtoOptional.ifPresent(beerDto -> {
                beerOrderLineDto.setBeerId(beerDto.getId());
                beerOrderLineDto.setBeerName(beerDto.getBeerName());
                beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
                beerOrderLineDto.setPrice(beerDto.getPrice());
            }
        );
        return beerOrderLineDto;
    }
}
