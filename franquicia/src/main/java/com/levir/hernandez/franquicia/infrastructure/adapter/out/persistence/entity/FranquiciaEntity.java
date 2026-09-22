package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "franquicias")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class FranquiciaEntity
{
    @Id @Column(name = "franquicia_pk")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    public FranquiciaEntity(UUID id) {this.id = id;}
}
