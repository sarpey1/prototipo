# Dream Board – Co-Creation prototype

Golden Goose "Dream Makers" Co-Creation concept, built for a company user test.
React 19 + Vite 8 + Tailwind 4.

```
pnpm install
pnpm dev     # local preview
pnpm build   # production build
```

- `src/conversation.ts` – scripted chat, strings (EN/IT), concept-sheet brief
- `src/adaptive.ts` – Claude-powered chat layer (artifact `sample` capability); falls back to the scripted chat
- `src/storyCardFile.ts` – concept sheet renderer (canvas, Figma 948:831)
- `src/Checkout.tsx` – prototype checkout (no payment)
- `src/data/golden-goose-cocreation-catalog.csv` – the 26 Dream Maker works with direction and technique/motif/mood tags
