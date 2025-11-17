import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const cdnURL = env.VITE_CDN_URL;

  return {
    plugins: [react()],
    resolve: {
      extensions: ['.ts', '.js', '.mjs', '.json', '.tsx'],
    },
    esbuild: {
      loader: 'tsx',
      include: /\.(ts|tsx|js|mjs)$/,
    },
    /* Multi-page app configuration */
    build: {
      rollupOptions: {
        input: {
          main: resolve(__dirname, 'index.html'),
          'service-ko': resolve(__dirname, 'service-ko.html'),
          'switch-to-io': resolve(__dirname, 'switch-to-io.html'),
        },
      },
    },
    /**
     * https://vite.dev/guide/build.html#advanced-base-options
     */
    experimental: {
      renderBuiltUrl(filename: string | URL) {
        // We only want to prepend the CDN URL during the 'build' command
        // and only if the VITE_CDN_URL is set.
        if (command === 'build' && cdnURL) {
          // hostType can be 'js', 'css', or 'asset'
          return new URL(filename, cdnURL).href;
        } else {
          // Otherwise, (e.g., in 'serve' mode), use relative paths.
          return { relative: true };
        }
      },
    },
  };
});
