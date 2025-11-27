package com.flowbit.pokedex.dto;

import java.util.List;

public record PokemonSummary(
        int id,
        String name,
        int height,
        int weight,
        int baseExperience,
        int order,
        String sprite,
        List<String> types,
        List<String> heldItems,
        List<PokemonAbility> abilities,
        List<PokemonStat> stats,
        List<String> moves
) {
}

