package com.alonso.salesapp.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
public class IngressDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idIngressDetail; // ID SIMPLE (Igual que SaleDetail)

    @ManyToOne
    @JoinColumn(name = "id_ingress", nullable = false, foreignKey = @ForeignKey(name = "FK_Detail_Ingress"))
    private Ingress ingress; // EL PADRE

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false, foreignKey = @ForeignKey(name = "FK_IngressDetail_Product"))
    private Product product; // EL PRODUCTO

    @Column(nullable = false)
    private short quantity = 0;

    @Column(columnDefinition = "decimal(6,2)", nullable = false)
    private double cost; // OJO: Aquí es COSTO, no precio de venta
}