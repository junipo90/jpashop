package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders") // 이렇게 하면 안됨
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) { // 지연 로딩 프록시 강제 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        List<OrderDto> collect = all.stream().map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 1 : N 에서 데이터 뻥튀기 되는 걸 막기 위해 distinct 로 중복 제거
     * 하지만 이렇게 하면 페이징이 불가능 해진다. -> 메모리에서 페이징 처리 (매우 위험) 쓰면 안됨
     * 컬렉션 페치 조인은 1개만 가능, 2개 이상하면 데이터가 부정확하게 조회 될 수도 있다.
     */
    @GetMapping("api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();
        List<OrderDto> collect = all.stream().map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 쿼리 수가 1+N -> 1+1 으로 최적화, 조인보다 DB 데이터 전송량 최적화
     * XToOne 은 다 fetch 조인을 하고
     * 컬렉션 조회 (ToMany) 는 hibernate.default_batch_fetch_size 설정으로 인 쿼리로 한번에 땡겨올 수 있다.
     * 페이징 처리도 가능해졌다.
     * batch size 개별 설정은 @BatchSize 어노테이션으로
     */
    @GetMapping("api/v3.1/orders")
    public List<OrderDto> ordersV3_1(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit){
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> collect = all.stream().map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;
        private int price;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            price = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
