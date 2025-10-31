import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  resolve: {
    extensions: ['.ts', '.js', '.mjs', '.json', '.tsx'],
  },
  esbuild: {
    loader: 'tsx',
    include: /\.(ts|tsx|js|mjs)$/,
  },
  base: import.meta.env.VITE_URL_BASE || '/',
});
