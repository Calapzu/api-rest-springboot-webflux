package com.api.rest.springboot.webflux.servicios;

import com.api.rest.springboot.webflux.documentos.Cliente;
import com.api.rest.springboot.webflux.repositorios.ClienteDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class ClienteServiceImpl implements ClienteService{

    @Autowired
    private ClienteDAO clienteDAO;

    @Override
    public Flux<Cliente> findAll() {
        return clienteDAO.findAll();
    }

    @Override
    public Mono<Cliente> findById(String id) {
        return clienteDAO.findById(id);
    }

    @Override
    public Mono<Cliente> save(Cliente cliente) {
        return clienteDAO.save(cliente);
    }

    @Override
    public Mono<Void> delete(Cliente cliente) {
        return clienteDAO.delete(cliente);
    }
}
