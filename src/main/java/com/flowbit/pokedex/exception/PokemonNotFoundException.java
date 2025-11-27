package com.flowbit.pokedex.exception;

public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(String name) {
        super("Pokémon '" + name + "' was not found");
    }
}

