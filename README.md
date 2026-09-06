# Magic Egg

A Fabric mod that adds a Magic Egg - a throwable item that converts mobs into their spawn eggs.

## Screenshots

![Magic Egg](img.png)
![Magic Egg Usage](img2.png)
![Magic Egg Recipe](img3.png)

## Features

**Magic Egg**: A throwable projectile that instantly converts entities into spawn eggs
- Throw it at any mob to remove them and drop their spawn egg
- Works on any entity that has a spawn egg in vanilla Minecraft
- Does NOT work on players (for obvious reasons)
- Stacks up to 16
- If it misses (hits a block instead of a mob), there's a small chance it spawns something instead: usually a chicken, occasionally a random mob, and rarely a chicken on fire (a "lava chicken")
- Grants advancements for your first capture and for producing a lava chicken

## Learning It

The recipe is four lapis, four ender pearls and an egg, in a particular arrangement, and nobody is going to arrive at that by experiment.

So with [village-quests](https://github.com/fatlard1993/village-quests) installed, a cleric who trusts you will describe it and ask you to bring the pieces. Nothing is taken: you gathered exactly one magic egg's worth of materials, and the payoff is being able to make one on the walk home.

Then they ask you to throw it at something and come back and tell them, because knowing a recipe is not the same as knowing what a thing does. That second half only appears for someone already taught and not yet shown, so the two read as one conversation continued.

The integration is optional and guarded: without village-quests the mod behaves exactly as before.

## Crafting Recipe

Surround an egg with alternating lapis lazuli and ender pearls:

```
L E L
E G E
L E L
```

| Symbol | Item |
|--------|------|
| L | Lapis Lazuli |
| E | Ender Pearl |
| G | Egg |

## Pandorical

Magic Egg registers its own assets through Pandorical's content sync, and uses Pandorical's `thrown_item` entity renderer to display the flying egg projectile on Pandorical-enabled clients.

**The Pandorical mod must be installed client-side** to see the thrown egg rendered in flight.

## Development

Installing and building are in [DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT, see [LICENSE](LICENSE).
