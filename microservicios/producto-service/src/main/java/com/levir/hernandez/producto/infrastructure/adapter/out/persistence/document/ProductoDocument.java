package com.levir.hernandez.producto.infrastructure.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductoDocument
{
    @Id
    private String id;

    private String nombre;

    private Integer stock;

    @Indexed
    private String sucursalId;
}
