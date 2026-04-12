package com.example.template.testing.mockito;

import com.example.template.testing.model.User;
import com.example.template.testing.repository.UserRepository;
import com.example.template.testing.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Demonstrates Mockito features: mocking, stubbing, verification,
 * argument captors, BDD style, and spy usage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito Feature Showcase")
class MockitoFeaturesTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static final User ALICE = new User(1L, "Alice", "alice@example.com", LocalDate.now());

    // ── Basic Stubbing ─────────────────────────────────────────────────

    @Test
    @DisplayName("Stub repository to return a user")
    void stubbingReturnValue() {
        when(repository.findById(1L)).thenReturn(Optional.of(ALICE));

        User result = service.getById(1L);

        assertThat(result).isEqualTo(ALICE);
    }

    @Test
    @DisplayName("Stub with any() matcher")
    void stubbingWithMatchers() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(99L, u.name(), u.email(), u.createdAt());
        });

        User result = service.register("Bob", "bob@example.com");

        assertThat(result.id()).isEqualTo(99L);
    }

    // ── Verification ───────────────────────────────────────────────────

    @Test
    @DisplayName("Verify repository interactions")
    void verifyInteractions() {
        when(repository.findByEmail("c@d.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(ALICE);

        service.register("Carol", "c@d.com");

        verify(repository).findByEmail("c@d.com");
        verify(repository).save(any(User.class));
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Verify never called")
    void verifyNeverCalled() {
        when(repository.findByEmail("alice@example.com")).thenReturn(Optional.of(ALICE));

        assertThatThrownBy(() -> service.register("Alice", "alice@example.com"))
                .isInstanceOf(IllegalStateException.class);

        verify(repository, never()).save(any());
    }

    // ── Argument Captor ────────────────────────────────────────────────

    @Test
    @DisplayName("Capture and inspect arguments passed to mock")
    void argumentCaptor() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(ALICE);

        service.register("Dave", "dave@example.com");

        verify(repository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertThat(captured.name()).isEqualTo("Dave");
        assertThat(captured.email()).isEqualTo("dave@example.com");
    }

    // ── BDD Style (given/when/then) ────────────────────────────────────

    @Test
    @DisplayName("BDD-style test with given/when/then")
    void bddStyle() {
        // given
        given(repository.findAll()).willReturn(List.of(ALICE));

        // when
        List<User> users = service.listAll();

        // then
        then(repository).should().findAll();
        assertThat(users).hasSize(1).containsExactly(ALICE);
    }

    // ── Spy (partial mock) ─────────────────────────────────────────────

    @Test
    @DisplayName("Spy wraps a real object, allowing selective stubbing")
    void spyExample() {
        var realList = new java.util.ArrayList<>(List.of("a", "b", "c"));
        var spiedList = spy(realList);

        // Real method is called
        assertThat(spiedList.get(0)).isEqualTo("a");

        // Override specific behavior
        doReturn("MOCKED").when(spiedList).get(1);
        assertThat(spiedList.get(1)).isEqualTo("MOCKED");
        assertThat(spiedList.size()).isEqualTo(3); // real size
    }

    // ── Exception Stubbing ─────────────────────────────────────────────

    @Test
    @DisplayName("Stub to throw exception")
    void stubbingException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("999");
    }
}
