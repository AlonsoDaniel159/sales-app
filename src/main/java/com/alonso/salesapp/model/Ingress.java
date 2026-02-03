package com.alonso.salesapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Ingress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idIngress;

    @ManyToOne
    @JoinColumn(name = "id_provider", nullable = false, foreignKey = @ForeignKey(name = "FK_Ingress_Provider"))
    private Provider provider; // COMPRA A PROVEEDOR

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false, foreignKey = @ForeignKey(name = "FK_Ingress_User"))
    private User user; // USUARIO QUE REGISTRA

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(length = 20, nullable = false)
    private String serialNumber; // NRO FACTURA FÍSICA

    @Column(columnDefinition = "decimal(6,2)", nullable = false)
    private double total;

    @Column(columnDefinition = "decimal(6,2)", nullable = false)
    private double tax;

    @OneToMany(mappedBy = "ingress", cascade = CascadeType.ALL)
    private List<IngressDetail> details;
}