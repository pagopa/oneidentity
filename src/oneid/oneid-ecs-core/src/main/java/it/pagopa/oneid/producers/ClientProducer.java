package it.pagopa.oneid.producers;

import it.pagopa.oneid.common.Client;
import it.pagopa.oneid.connector.ClientConnectorImpl;
import it.pagopa.oneid.exception.ClientNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;

import java.util.ArrayList;
import java.util.HashMap;

@ApplicationScoped
public class ClientProducer {

    @Inject
    ClientConnectorImpl clientConnectorImpl;

    @ApplicationScoped
    @Produces
    HashMap<String, Client> clientsMap() {
        HashMap<String, Client> map = new HashMap<>();
        ArrayList<Client> clients =
                clientConnectorImpl.findAll()
                        .orElseThrow(ClientNotFoundException::new); // TODO valutare se cambiare eccezione

        clients.forEach(client -> map.put(client.getClientId(), client));

        return map;
    }
}
