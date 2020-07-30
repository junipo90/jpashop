package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // 엔티티를 그대로 반환하면 안되고 연관 관계 대해 무한 루프 or 성능 저하
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        return orderRepository.findAll(new OrderSearch());
    }

    // 이렇게 해도 N + 1 문제 발생
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> collect = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4(){
        List<OrderSimpleQueryDto> orderDtos = orderSimpleQueryRepository.findOrderDtos();
        return new Result(orderDtos);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
