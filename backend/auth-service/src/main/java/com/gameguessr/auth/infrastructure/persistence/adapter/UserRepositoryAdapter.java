package com.gameguessr.auth.infrastructure.persistence.adapter;

import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.outbound.UserRepository;
import com.gameguessr.auth.infrastructure.persistence.entity.UserEntity;
import com.gameguessr.auth.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    private UserEntity toEntity(User user) {
        UserEntity.UserEntityBuilder builder = UserEntity.builder()
                .username(user.getUsername())
                .password(user.getPassword());
        
        if (user.getId() != null) {
            builder.id(user.getId());
        }
        
        return builder.build();
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .build();
    }
}