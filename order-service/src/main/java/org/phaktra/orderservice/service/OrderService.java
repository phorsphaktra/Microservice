package org.phaktra.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.phaktra.orderservice.dto.InventoryResponse;
import org.phaktra.orderservice.dto.OrderLineItemDto;
import org.phaktra.orderservice.dto.OrderRequest;
import org.phaktra.orderservice.model.Order;
import org.phaktra.orderservice.model.OrderLineItem;
import org.phaktra.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;


    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemList(orderLineItems);

        //Todo find skuCode form order line item
        List<String> skuCode = order.getOrderLineItemList().stream()
                .map(OrderLineItem::getSkuCode)
                .toList();

        //todo Call Inventory Service, and place order if product is in stock
        InventoryResponse[] inventoryResponsesArray = webClient.get()
                .uri("http://localhost:8082/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCode).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);

        if (allProductInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("Product is not in stock, please try again later!");
        }
    }

    private OrderLineItem mapToDto(OrderLineItemDto orderLineItemDto) {
        OrderLineItem orderLineItems = new OrderLineItem();
        orderLineItems.setId(orderLineItemDto.getId());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        return orderLineItems;
    }
}
