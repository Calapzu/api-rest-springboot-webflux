package com.api.rest.springboot.webflux.controlador;

import com.api.rest.springboot.webflux.documentos.Cliente;
import com.api.rest.springboot.webflux.servicios.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @Value("${config.uploads.path}")
    private String path;

    @PostMapping("/registrarClienteConFoto")
    public Mono<ResponseEntity<Cliente>> registrarClienteConFoto(Cliente cliente, @RequestPart FilePart file) {
        cliente.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("//", ""));

        return file.transferTo(new File(path + cliente.getFoto()))
                .then(service.save(cliente))
                .map(c -> ResponseEntity.created(URI.create("api/clientes/".concat(c.getId())))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(c));

    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Cliente>> subirFoto(@PathVariable String id, @RequestPart FilePart file) {
        return service.findById(id).flatMap(c -> {
                    c.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("//", "")
                    );

                    return file.transferTo(new File(path + c.getFoto())).then(service.save(c));

                }).map(c -> ResponseEntity.ok(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Cliente>>> listarClientes() {
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(service.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> verDetallesDelCliente(@PathVariable String id) {
        return service.findById(id).map(c -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(c)
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> guardarCliente(@Valid @RequestBody Mono<Cliente> monoCliente) {
        Map<String, Object> respuesta = new HashMap<>();

        return monoCliente.flatMap(cliente -> {
            return service.save(cliente)
                    .map(c -> {
                        respuesta.put("cliente", c);
                        respuesta.put("mensaje", "cliente guardado con exito");
                        respuesta.put("timestamp", new Date());
                        return ResponseEntity.created(URI.create("api/clientes/".concat(c.getId())))
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .body(respuesta);
                    });
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class)
                    .flatMap(e -> Mono.just(e.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    .map(fieldError -> "El campo : " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> {
                        respuesta.put("errors", list);
                        respuesta.put("timestamp", new Date());
                        respuesta.put("status", HttpStatus.BAD_REQUEST.value());

                        return Mono.just(ResponseEntity.badRequest().body(respuesta));
                    });
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Cliente>> editarCliente(@RequestBody Cliente cliente, @PathVariable String id) {
        return service.findById(id).flatMap(c -> {
            c.setNombre(cliente.getNombre());
            c.setApellido(cliente.getApellido());
            c.setEdad(cliente.getEdad());
            c.setSueldo(cliente.getSueldo());

            return service.save(c);
        }).map(c -> ResponseEntity.created(URI.create("/api/clientes/".concat(c.getId())))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(c)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminarCliente(@PathVariable String id) {
        return service.findById(id).flatMap(c -> {
            return service.delete(c).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

}
