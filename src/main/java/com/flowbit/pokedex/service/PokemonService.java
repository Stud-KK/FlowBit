package com.flowbit.pokedex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowbit.pokedex.client.PokemonClient;
import com.flowbit.pokedex.dto.PokemonAbility;
import com.flowbit.pokedex.dto.PokemonStat;
import com.flowbit.pokedex.dto.PokemonSummary;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Validated
public class PokemonService {

    private static final int MOVE_DISPLAY_LIMIT = 6;
    private final PokemonClient pokemonClient;

    public PokemonService(PokemonClient pokemonClient) {
        this.pokemonClient = pokemonClient;
    }

    @Cacheable(cacheNames = "pokemon", key = "#name.toLowerCase()")
    public PokemonSummary findPokemon(@NotBlank(message = "name is required") String name) {
        JsonNode payload = pokemonClient.fetchPokemon(name);
        return map(payload);
    }

    private PokemonSummary map(JsonNode root) {
        int id = root.path("id").asInt();
        String name = root.path("name").asText();
        int height = root.path("height").asInt();
        int weight = root.path("weight").asInt();
        int baseExperience = root.path("base_experience").asInt();
        int order = root.path("order").asInt();
        String sprite = extractSprite(root);

        List<String> types = StreamSupport.stream(root.path("types").spliterator(), false)
                .map(node -> node.path("type").path("name").asText())
                .toList();

        List<String> heldItems = StreamSupport.stream(root.path("held_items").spliterator(), false)
                .map(node -> node.path("item").path("name").asText())
                .toList();

        List<PokemonAbility> abilities = StreamSupport.stream(root.path("abilities").spliterator(), false)
                .map(node -> new PokemonAbility(
                        node.path("ability").path("name").asText(),
                        node.path("is_hidden").asBoolean()))
                .toList();

        List<PokemonStat> stats = StreamSupport.stream(root.path("stats").spliterator(), false)
                .map(node -> new PokemonStat(
                        node.path("stat").path("name").asText(),
                        node.path("base_stat").asInt()))
                .toList();

        List<String> moves = StreamSupport.stream(root.path("moves").spliterator(), false)
                .limit(MOVE_DISPLAY_LIMIT)
                .map(node -> node.path("move").path("name").asText())
                .collect(Collectors.toList());

        return new PokemonSummary(
                id,
                name,
                height,
                weight,
                baseExperience,
                order,
                sprite,
                types,
                heldItems,
                abilities,
                stats,
                moves
        );
    }

    private String extractSprite(JsonNode root) {
        return Optional.ofNullable(root.path("sprites").path("other").path("official-artwork").path("front_default").textValue())
                .orElseGet(() -> root.path("sprites").path("front_default").asText(""));
    }
}

