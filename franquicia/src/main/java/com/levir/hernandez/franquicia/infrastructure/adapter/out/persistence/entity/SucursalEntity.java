package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "sucursales")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SucursalEntity
{
    @Id @Column(name = "sucursal_pk")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "franquicia_fk", nullable = false)
    private FranquiciaEntity franquicia;

    public SucursalEntity(UUID id) {this.id = id;}
}
