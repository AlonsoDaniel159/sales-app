package com.alonso.salesapp.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Role {
    @Id
    @EqualsAndHashCode.Include
    private Integer idRole; // No autogenerado, los roles suelen ser fijos (1: ADMIN, 2: USER)

    @Column(length = 10, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;
}