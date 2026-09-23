package com.levir.hernandez.franquicia.infrastructure.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "franquicias")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class FranquiciaDocument
{
    @Id
    private String id;

    private String nombre;
}
