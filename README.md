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
- Grants advancements for your first capture and for producing a lava chicken

**Ender Egg**: the same throw, except it keeps the animal instead of the species

A magic egg thrown at a named, saddled, particularly coloured horse gives you back a horse. An ender egg gives you back *that* horse.

- An eye of ender in the recipe, in place of the pearl at the top
- The spawn egg it drops carries the mob's own save data, so placing it puts the same mob back: variant, custom name, saddle, tamed-by, health, inventory, everything it was carrying
- The dropped egg is named after the mob, so a shulker box of them is readable at a glance
- Where it stood and who it was are deliberately not kept. The mob arrives where you put it, and gets a new identity, because the eggs stack and two mobs cannot share one
- Otherwise identical: same throw, same stack size, same behaviour on a miss

### Missing

A throw that hits a block instead of a mob resolves in that order:

- **One time in four the egg doesn't break at all.** It lands as an item, to be picked up and thrown again
- Of the ones that do break, one in four hatches something:
  - 6 in 8: **chickens** - anywhere from one to ninety-nine of them, mixed species and mixed ages, rolled per bird. Most flocks are a handful; roughly one miss in sixteen can run away with itself
  - 1 in 8: a random mob
  - 1 in 8: one chicken on fire (a "lava chicken")

## Learning It

The recipe is four lapis, four ender pearls and an egg, in a particular arrangement, and nobody is going to arrive at that by experiment.

So with [village-quests](https://github.com/fatlard1993/village-quests) installed, a cleric who trusts you will describe it and ask you to bring the pieces. Nothing is taken: you gathered exactly one magic egg's worth of materials, and the payoff is being able to make one on the walk home.

Then they ask you to throw it at something and come back and tell them, because knowing a recipe is not the same as knowing what a thing does. That second half only appears for someone already taught and not yet shown, so the two read as one conversation continued.

The ender egg is not part of that conversation. It is a variation on a recipe you have already been taught, and finding it is left to you.

The integration is optional and guarded: without village-quests the mod behaves exactly as before.

## Crafting Recipes

A magic egg is an egg surrounded by alternating lapis lazuli and ender pearls. An ender egg is the same working with an eye of ender where the top pearl goes.

```
 Magic Egg      Ender Egg

  L E L          L Y L
  E G E          E G E
  L E L          L E L
```

| Symbol | Item |
|--------|------|
| L | Lapis Lazuli |
| E | Ender Pearl |
| Y | Eye of Ender |
| G | Egg (white, blue or brown) |

## Pandorical

Pandorical is required on the server. Magic Egg registers its own assets through Pandorical's content sync, and uses Pandorical's `thrown_item` entity renderer to display the flying egg projectile on Pandorical-enabled clients.

**The Pandorical mod must be installed client-side** to see the thrown egg rendered in flight.

## Development

Installing and building are in [DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT, see [LICENSE](LICENSE).
