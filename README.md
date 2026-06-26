# Action Deck 1.21.1

Action Deck is a Minecraft Fabric mod for data-driven collectible cards and mixed card stacks.

## Data-Driven Cards

Cards are loaded from data packs at:

```text
data/<namespace>/action_deck/cards/<card_id>/card.json
```

The folder name becomes the card identifier. For example:

```text
data/action_deck/action_deck/cards/generic_gold_spade_ace/card.json
```

loads as `action_deck:generic_gold_spade_ace`.

Example:

```json
{
  "name": "Ace of Spades",
  "description": "A sample data-driven card definition.",
  "deck": "action_deck:generic_gold",
  "rank": {
    "id": "ace",
    "display": "Ace"
  },
  "suit": {
    "id": "spades",
    "display": "Spades",
    "symbol": "♠"
  },
  "textures": {
    "front": "action_deck:item/card_generic_gold_spade_ace",
    "back": "action_deck:item/card_generic_gold_back"
  },
  "item_model": "action_deck:item/card"
}
```

Textures still live in `assets/<namespace>/textures/...`, using normal Minecraft resource-pack rules. The card data points to those textures.

## Data-Driven Decks

Deck definitions are loaded from:

```text
data/<namespace>/action_deck/decks/<deck_id>.json
```

Example:

```json
{
  "name": "Generic Gold",
  "default_back": "action_deck:item/card_generic_gold_back",
  "deck_pack": {
    "crafting_block": "minecraft:gold_block",
    "texture": "action_deck:item/generic_gold_pack"
  },
  "cards": [
    "action_deck:generic_gold_spade_ace"
  ]
}
```

Decks are metadata and starter lists. Deck stacks can still contain cards from any namespace or deck.

The optional `deck_pack` object enables a craftable pack for that deck. Place its configured
`crafting_block` in the center of a crafting table and surround it with eight pieces of paper.
Opening the resulting pack gives five random cards from the deck. Decks without `deck_pack`
do not have a pack recipe. The optional `texture` uses a standard resource identifier without
the `textures/` prefix or `.png` suffix; if omitted, the pack renders with the paper texture.

## Commands

```text
/actiondeck card give <card_id>
/actiondeck deck give <deck_id>
/card give <card_id>
```

## Multiplayer Compatibility

Action Deck syncs loaded card and deck definitions from the server to connected clients when players join and after data pack reloads. This keeps dedicated servers, clients, and compatibility layers such as Kilt using the same card metadata.

Clients still need the mod installed for the built-in card textures and models. Server data packs can add or change card/deck metadata, but any custom textures referenced by those definitions must also be available to clients through the mod or a matching resource pack.

## License

This project is licensed under the MIT License.
