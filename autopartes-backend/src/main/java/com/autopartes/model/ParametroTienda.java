package com.autopartes.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="parametro_tienda") @Getter @Setter @NoArgsConstructor
public class ParametroTienda {
 @Id private Long id;
 @Column(nullable=false,length=150) private String nombre;
 @Column(nullable=false,length=200) private String local;
 @Column(nullable=false,length=30) private String telefono;
 @Column(nullable=false,length=15) private String whatsapp;
 @Column(nullable=false,length=300) private String direccion;
 @Column(length=254) private String correoContacto;
 @Column(length=300) private String horario;
 @Column(nullable=false,precision=10,scale=7) private BigDecimal latitud;
 @Column(nullable=false,precision=10,scale=7) private BigDecimal longitud;
 @Column(length=2048) private String mapsUrl;
 @Version private Long version;
}
