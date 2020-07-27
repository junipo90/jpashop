package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initDb1();
        initService.initDb2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void initDb1() {
            Member member = createMember("UserA", "서울", "거리1", "우편번호1");
            em.persist(member);

            Book book1 = new Book();
            createBook(book1, "JPA1 BOOK", 10000, "jpa1 저자", "isbn1");
            em.persist(book1);

            Book book2 = new Book();
            createBook(book2, "JPA2 BOOK", 20000, "jpa2 저자", "isbn2");
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);

        }

        public void initDb2() {
            Member member = createMember("UserB", "부산", "거리2", "우편번호2");
            em.persist(member);

            Book book1 = new Book();
            createBook(book1, "SPRING1 BOOK", 20000, "SPRING1 저자", "isbn1");
            em.persist(book1);

            Book book2 = new Book();
            createBook(book2, "SPRING2 BOOK", 40000, "SPRING2 저자", "isbn2");
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);

        }

        private void createBook(Book book, String name, int price, String author, String isbn) {
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(100);
            book.setAuthor(author);
            book.setIsbn(isbn);
        }

        private Member createMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }
    }
}
