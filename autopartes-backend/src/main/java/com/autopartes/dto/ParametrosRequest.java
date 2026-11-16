package com.autopartes.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ParametrosRequest(
 @NotBlank(message="El nombre comercial es obligatorio") @Size(min=2,max=150,message="El nombre debe tener entre 2 y 150 caracteres") @Pattern(regexp="[^\\p{Cntrl}]*",message="El nombre no admite caracteres de control") String nombre,
 @NotBlank(message="El establecimiento es obligatorio") @Size(min=2,max=200,message="El establecimiento debe tener entre 2 y 200 caracteres") @Pattern(regexp="[^\\p{Cntrl}]*",message="El establecimiento no admite caracteres de control") String local,
 @NotBlank(message="El teléfono es obligatorio") @Size(max=30,message="El teléfono no puede superar 30 caracteres") @Pattern(regexp="(?=(?:\\D*\\d){7,15}\\D*$)\\+?[0-9 ()-]+",message="El teléfono debe contener entre 7 y 15 dígitos y solo signos de teléfono") String telefono,
 @NotBlank(message="WhatsApp es obligatorio") @Pattern(regexp="[1-9][0-9]{6,14}",message="WhatsApp debe incluir código de país y entre 7 y 15 dígitos, sin + ni espacios") String whatsapp,
 @NotBlank(message="La dirección es obligatoria") @Size(min=5,max=300,message="La dirección debe tener entre 5 y 300 caracteres") @Pattern(regexp="[^\\p{Cntrl}]*",message="La dirección no admite caracteres de control") String direccion,
 @Email(message="Ingresa un correo válido") @Pattern(regexp="(?:[^\\s@]+@[^\\s@]+\\.[^\\s@]+)?",message="El correo debe incluir un dominio completo") @Size(max=254,message="El correo no puede superar 254 caracteres") String correoContacto,
 @Pattern(regexp="(?:[^\\p{Cntrl}]{3,300})?",message="El horario debe tener entre 3 y 300 caracteres, sin caracteres de control") String horario,
 @NotNull(message="La latitud es obligatoria") @DecimalMin(value="-90",message="La latitud mínima es -90") @DecimalMax(value="90",message="La latitud máxima es 90") @Digits(integer=3,fraction=7,message="Usa hasta 7 decimales para la latitud") BigDecimal latitud,
 @NotNull(message="La longitud es obligatoria") @DecimalMin(value="-180",message="La longitud mínima es -180") @DecimalMax(value="180",message="La longitud máxima es 180") @Digits(integer=3,fraction=7,message="Usa hasta 7 decimales para la longitud") BigDecimal longitud,
 @Size(max=2048,message="El enlace del mapa no puede superar 2048 caracteres") @Pattern(regexp="(?:https://(?:(?:www\\.)?google\\.com/maps(?:[/?#][^\\s]*)?|maps\\.google\\.com(?:[/?#][^\\s]*)?|maps\\.app\\.goo\\.gl/[^\\s/][^\\s]*|goo\\.gl/maps/[^\\s]+))?",message="Usa un enlace HTTPS de Google Maps, no la página principal de Google") String mapsUrl,
 @NotNull(message="Recarga los parámetros antes de guardar") @PositiveOrZero(message="La versión no es válida") Long version
) {
 public ParametrosRequest {
  nombre=normalizar(nombre);local=normalizar(local);telefono=normalizar(telefono);whatsapp=normalizar(whatsapp);direccion=normalizar(direccion);correoContacto=normalizar(correoContacto);horario=normalizar(horario);mapsUrl=normalizar(mapsUrl);
 }
 private static String normalizar(String texto){return texto==null?"":texto.trim();}
}
