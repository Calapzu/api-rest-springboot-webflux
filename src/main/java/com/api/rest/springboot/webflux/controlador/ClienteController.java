package com.api.rest.springboot.webflux.controlador;

import com.api.rest.springboot.webflux.documentos.Cliente;
import com.api.rest.springboot.webflux.servicios.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
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
                .map(c -> ResponseEntity.created(URI.create("api/clientes".concat(c.getId())))
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


}
