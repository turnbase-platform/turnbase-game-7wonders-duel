// Palette and sizes (docs/design/frontend-visual-design.md §1). Light is warm limestone, dark is
// basalt; the seat colours are per viewer, never per seat number.
export const TOKENS = `
:root {
  color-scheme: light dark;
  --ground: #ece4d2; --surface: #f6f0e2; --ink: #3b3226; --muted: #8a7f6b; --line: #cfc3a8;
  --me: #1f9aa8; --opp: #9a5b2a;
  --c-brown: #8b6914; --c-grey: #888888; --c-blue: #4a90d9; --c-green: #5aad5a;
  --c-yellow: #daa520; --c-red: #c73030; --c-purple: #7b4b94;
  --wonder: #a89468; --token: #6f8f6a;
  --radius: 3px;
  --serif: Georgia, "Times New Roman", serif;
  --sans: system-ui, -apple-system, "Segoe UI", sans-serif;
}
@media (prefers-color-scheme: dark) {
  :root {
    --ground: #1f1c18; --surface: #2a2620; --ink: #e8dfcc; --muted: #9b9078; --line: #453d31;
    --me: #3bb7c5; --opp: #c47f45;
    --wonder: #7d6e4c; --token: #4f6c4b;
  }
}
`;
