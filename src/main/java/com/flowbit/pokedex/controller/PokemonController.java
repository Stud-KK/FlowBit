package com.flowbit.pokedex.controller;

import com.flowbit.pokedex.dto.PokemonSummary;
import com.flowbit.pokedex.service.PokemonService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/pokemon")
    public PokemonSummary getPokemon(
            @RequestParam("name")
            @NotBlank(message = "name is required")
            @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "name must be alphanumeric or dash")
            String name
    ) {
        return pokemonService.findPokemon(name);
    }
}

