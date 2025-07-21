package com.chatspot.chatapp.entity.user;

import com.chatspot.chatapp.common.BaseAuditing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "users")
@Entity
public class User extends BaseAuditing {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    private LocalDateTime lastSeen;
    private String profilePicture;
}
