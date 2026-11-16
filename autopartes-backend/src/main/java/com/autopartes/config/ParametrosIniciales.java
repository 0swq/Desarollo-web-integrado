package com.autopartes.config;
import com.autopartes.service.ParametrosService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
@Configuration
public class ParametrosIniciales {
 @Bean ApplicationRunner inicializarParametros(ParametrosService service){return args->service.inicializar();}
}
