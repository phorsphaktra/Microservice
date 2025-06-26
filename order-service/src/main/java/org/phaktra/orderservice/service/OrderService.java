package org.phaktra.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.phaktra.orderservice.dto.OrderLineItemDto;
import org.phaktra.orderservice.dto.OrderRequest;
import org.phaktra.orderservice.model.Order;
import org.phaktra.orderservice.model.OrderLineItem;
import org.phaktra.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> orderLineItems =  orderRequest.getOrderLineItemDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemList(orderLineItems);
        orderRepository.save(order);

    }

    private OrderLineItem mapToDto(OrderLineItemDto orderLineItemDto) {
        OrderLineItem orderLineItems =  new OrderLineItem();
        orderLineItems.setId(orderLineItemDto.getId());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        return orderLineItems;
    }
}
