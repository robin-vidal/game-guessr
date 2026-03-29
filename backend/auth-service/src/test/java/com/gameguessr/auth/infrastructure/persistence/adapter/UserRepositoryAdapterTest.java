package com.gameguessr.auth.infrastructure.persistence.adapter;

import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.infrastructure.persistence.entity.UserEntity;
import com.gameguessr.auth.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepositoryAdapter")
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "encodedPassword";
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("findByUsername — returns mapped user when found")
    void findByUsername_found_returnsUser() {
        UserEntity entity = UserEntity.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        when(jpaRepository.findByUsername(USERNAME)).thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByUsername(USERNAME);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(USERNAME);
        assertThat(result.get().getPassword()).isEqualTo(PASSWORD);
        assertThat(result.get().getId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("findByUsername — returns empty when not found")
    void findByUsername_notFound_returnsEmpty() {
        when(jpaRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByUsername(USERNAME);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save — persists user and returns mapped domain")
    void save_persistsAndReturnsDomain() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        when(jpaRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adapter.save(user);

        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        verify(jpaRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("save — maps domain to entity correctly")
    void save_mapsDomainToEntity() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        when(jpaRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(user);

        verify(jpaRepository).save(argThat(entity -> 
                entity.getId().equals(USER_ID) &&
                entity.getUsername().equals(USERNAME) &&
                entity.getPassword().equals(PASSWORD)
        ));
    }

    @Test
    @DisplayName("existsByUsername — delegates to jpaRepository")
    void existsByUsername_delegates() {
        when(jpaRepository.existsByUsername(USERNAME)).thenReturn(true);

        boolean result = adapter.existsByUsername(USERNAME);

        assertThat(result).isTrue();
        verify(jpaRepository).existsByUsername(USERNAME);
    }

    @Test
    @DisplayName("existsByUsername — returns false when not exists")
    void existsByUsername_notExists_returnsFalse() {
        when(jpaRepository.existsByUsername(USERNAME)).thenReturn(false);

        boolean result = adapter.existsByUsername(USERNAME);

        assertThat(result).isFalse();
    }
}