package com.api.rest.springboot.webflux.repositorios;

import com.api.rest.springboot.webflux.documentos.Cliente;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ClienteDAO extends ReactiveMongoRepository<Cliente, String> {
}
