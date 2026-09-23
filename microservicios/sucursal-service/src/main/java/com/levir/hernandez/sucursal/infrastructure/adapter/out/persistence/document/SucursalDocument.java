package com.levir.hernandez.sucursal.infrastructure.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sucursales")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SucursalDocument
{
    @Id
    private String id;

    private String nombre;

    @Indexed
    private String franquiciaId;
}
