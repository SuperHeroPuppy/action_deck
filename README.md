# Action Deck

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
  "cards": [
    "action_deck:generic_gold_spade_ace"
  ]
}
```

Decks are metadata and starter lists. Deck stacks can still contain cards from any namespace or deck.

## Commands

```text
/actiondeck card give <card_id>
/actiondeck deck give <deck_id>
/card give <card_id>
```

## License

This project is licensed under the MIT License.
