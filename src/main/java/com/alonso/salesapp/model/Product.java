package com.alonso.salesapp.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idProduct;

    // RELACIÓN: Muchos productos tienen una categoría.
    // Usamos FK_Product_Category para que la llave foránea tenga nombre decente en BD.
    @ManyToOne
    @JoinColumn(name = "id_category", nullable = false, foreignKey = @ForeignKey(name = "FK_Product_Category"))
    private Category category;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 150, nullable = false)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private Integer stock;

    @Column
    private String imageUrl;

    @Column
    private String imagePublicId;

    @Column(nullable = false)
    private boolean enabled = true;
}
