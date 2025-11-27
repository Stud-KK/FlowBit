package com.flowbit.pokedex.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowbit.pokedex.exception.PokemonNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class PokemonClient {

    private final WebClient webClient;

    public PokemonClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://pokeapi.co/api/v2")
                .build();
    }

    public JsonNode fetchPokemon(String name) {
        try {
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/pokemon/{name}")
                            .build(name.toLowerCase()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new PokemonNotFoundException(name)))
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new RuntimeException("PokéAPI server error. Please try again later.")))
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new PokemonNotFoundException(name);
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                throw new PokemonNotFoundException(name);
            }
            throw new RuntimeException("Failed to fetch Pokémon data: " + ex.getMessage(), ex);
        }
    }
}

