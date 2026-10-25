import { defineConfig } from "vite";

// The dev harness only (npm run dev): serves dev/ and the game sources with HMR, and proxies
// /api to the in-process game (DevServer.java). The shipped bundle is built by esbuild.
export default defineConfig({
  server: {
    port: 5173,
    proxy: { "/api": { target: `http://127.0.0.1:${process.env.DEV_API_PORT ?? "8787"}` } },
  },
});
