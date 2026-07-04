package com.example.template.database.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.template.database.entity.Order;
import com.example.template.database.entity.OrderItem;
import com.example.template.database.entity.OrderStatus;
import com.example.template.database.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link OrderService} with a Mockito-mocked {@link OrderRepository}. */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

  private static final String ALICE = "Alice";

  @Mock private OrderRepository orderRepository;

  @InjectMocks private OrderService service;

  @Captor private ArgumentCaptor<Order> orderCaptor;

  private static Order pendingOrder(String customerName) {
    var order = new Order();
    order.setCustomerName(customerName);
    return order;
  }

  // --- findById ---

  @Test
  @DisplayName("findById returns the order when it exists")
  void findByIdReturnsOrder() {
    var order = pendingOrder(ALICE);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThat(service.findById(1L)).isSameAs(order);
  }

  @Test
  @DisplayName("findById throws IllegalArgumentException when the order is missing")
  void findByIdThrowsWhenMissing() {
    when(orderRepository.findById(42L)).thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.findById(42L))
        .withMessage("Order not found: 42");
  }

  // --- findByIdWithItems ---

  @Test
  @DisplayName("findByIdWithItems delegates to the JOIN FETCH repository query")
  void findByIdWithItemsDelegates() {
    var order = pendingOrder(ALICE);
    when(orderRepository.findByIdWithItems(7L)).thenReturn(Optional.of(order));

    assertThat(service.findByIdWithItems(7L)).isSameAs(order);
    verify(orderRepository).findByIdWithItems(7L);
  }

  @Test
  @DisplayName("findByIdWithItems throws IllegalArgumentException when the order is missing")
  void findByIdWithItemsThrowsWhenMissing() {
    when(orderRepository.findByIdWithItems(42L)).thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.findByIdWithItems(42L))
        .withMessage("Order not found: 42");
  }

  // --- findByStatus ---

  @Test
  @DisplayName("findByStatus delegates to the derived repository query")
  void findByStatusDelegates() {
    var order = pendingOrder(ALICE);
    when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(order));

    assertThat(service.findByStatus(OrderStatus.PENDING)).containsExactly(order);
  }

  // --- createOrder ---

  @Test
  @DisplayName("createOrder attaches all items, computes the total, and saves")
  void createOrderAttachesItemsAndSaves() {
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    var widget = new OrderItem("Widget", 2, new BigDecimal("9.99"));
    var gadget = new OrderItem("Gadget", 1, new BigDecimal("5.50"));

    Order result = service.createOrder(ALICE, List.of(widget, gadget));

    verify(orderRepository).save(orderCaptor.capture());
    assertThat(orderCaptor.getValue()).isSameAs(result);
    assertThat(result.getCustomerName()).isEqualTo(ALICE);
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(result.getItems()).containsExactly(widget, gadget);
    assertThat(widget.getOrder()).isSameAs(result);
    assertThat(gadget.getOrder()).isSameAs(result);
    assertThat(result.getTotalAmount()).isEqualByComparingTo("25.48");
  }

  @Test
  @DisplayName("createOrder with no items saves an order with a zero total")
  void createOrderWithNoItemsHasZeroTotal() {
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    Order result = service.createOrder("Bob", List.of());

    assertThat(result.getItems()).isEmpty();
    assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  // --- confirmOrder ---

  @Test
  @DisplayName("confirmOrder transitions a PENDING order to CONFIRMED and saves it")
  void confirmOrderTransitionsPendingToConfirmed() {
    var order = pendingOrder(ALICE);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(order);

    Order result = service.confirmOrder(1L);

    assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("confirmOrder throws IllegalArgumentException for an unknown order")
  void confirmOrderThrowsWhenMissing() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.confirmOrder(99L))
        .withMessage("Order not found: 99");
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("confirmOrder throws IllegalStateException when the order is not PENDING")
  void confirmOrderThrowsWhenNotPending() {
    var order = pendingOrder(ALICE);
    order.setStatus(OrderStatus.SHIPPED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThatIllegalStateException()
        .isThrownBy(() -> service.confirmOrder(1L))
        .withMessage("Only PENDING orders can be confirmed");
    verify(orderRepository, never()).save(any());
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
  }

  // --- bulkUpdateStatus ---

  @Test
  @DisplayName("bulkUpdateStatus returns the repository's updated-row count")
  void bulkUpdateStatusReturnsCount() {
    when(orderRepository.bulkUpdateStatus(OrderStatus.PENDING, OrderStatus.CANCELLED))
        .thenReturn(3);

    assertThat(service.bulkUpdateStatus(OrderStatus.PENDING, OrderStatus.CANCELLED)).isEqualTo(3);
    verify(orderRepository).bulkUpdateStatus(OrderStatus.PENDING, OrderStatus.CANCELLED);
  }

  // --- calculateTotalRevenue ---

  @Test
  @DisplayName("calculateTotalRevenue sums totals of all DELIVERED orders")
  void calculateTotalRevenueSumsDeliveredTotals() {
    var first = pendingOrder(ALICE);
    first.addItem(new OrderItem("Widget", 2, new BigDecimal("10.00")));
    var second = pendingOrder("Bob");
    second.addItem(new OrderItem("Gadget", 1, new BigDecimal("5.50")));
    when(orderRepository.findByStatus(OrderStatus.DELIVERED)).thenReturn(List.of(first, second));

    assertThat(service.calculateTotalRevenue()).isEqualByComparingTo("25.50");
  }

  @Test
  @DisplayName("calculateTotalRevenue returns zero when nothing has been delivered")
  void calculateTotalRevenueReturnsZeroWhenEmpty() {
    when(orderRepository.findByStatus(OrderStatus.DELIVERED)).thenReturn(List.of());

    assertThat(service.calculateTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
