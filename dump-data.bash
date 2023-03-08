#!/usr/bin/env bash

curl 'https://beta.pokeapi.co/graphql/v1beta' \
     -H 'content-type: application/json' \
     -H 'accept: */*' \
     --compressed \
     --data-binary @- << EOF > public/dump.json
{
    "query": "query samplePokeAPIquery {\n  pokemon: pokemon_v2_pokemon {\n    types: pokemon_v2_pokemontypes {\n      type: pokemon_v2_type {\n        id\n      }\n    }\n    specy: pokemon_v2_pokemonspecy {\n      names: pokemon_v2_pokemonspeciesnames(where: {language_id: {_eq: 9}}) {\n        name\n      }\n      color: pokemon_v2_pokemoncolor {\n        id\n      }\n      is_mythical\n      is_legendary\n    }\n    id\n    sprites: pokemon_v2_pokemonsprites {\n      sprites\n    }\n  }\n  types: pokemon_v2_type {\n    id\n    names: pokemon_v2_typenames(where: {language_id: {_eq: 9}}) {\n      name\n    }\n  }\n  colors: pokemon_v2_pokemoncolor {\n    id\n    names: pokemon_v2_pokemoncolornames(where: {language_id: {_eq: 9}}) {\n      name\n    }\n  }\n}",
    "variables": null,
    "operationName":"samplePokeAPIquery"
}
EOF
