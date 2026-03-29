package com.gameguessr.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User")
class UserTest {

    @Test
    @DisplayName("builder — creates user with all fields")
    void builder_createsUserWithAllFields() {
        UUID id = UUID.randomUUID();

        User user = User.builder()
                .id(id)
                .username("testuser")
                .password("encodedPassword")
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("withUsername — creates new user with updated username")
    void withUsername_createsNewUserWithUpdatedUsername() {
        User original = User.builder()
                .id(UUID.randomUUID())
                .username("original")
                .password("password")
                .build();

        User updated = original.withUsername("newusername");

        assertThat(updated.getUsername()).isEqualTo("newusername");
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getPassword()).isEqualTo(original.getPassword());
    }

    @Test
    @DisplayName("withPassword — creates new user with updated password")
    void withPassword_createsNewUserWithUpdatedPassword() {
        User original = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("oldpassword")
                .build();

        User updated = original.withPassword("newpassword");

        assertThat(updated.getPassword()).isEqualTo("newpassword");
        assertThat(updated.getUsername()).isEqualTo(original.getUsername());
        assertThat(updated.getId()).isEqualTo(original.getId());
    }
}