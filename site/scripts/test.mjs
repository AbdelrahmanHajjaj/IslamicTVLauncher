import { createServer } from 'vite';

// Use the preview's existing TypeScript/JSON loader; no separate test framework.
process.env.TZ = 'Europe/London';
const server = await createServer({ configFile: false, logLevel: 'error', server: { middlewareMode: true }, ssr: {external: ['@islamic-tv/core']} });
try {
  await server.ssrLoadModule('/tests/methods.ts');
  await server.ssrLoadModule('/tests/text-format.ts');
  await server.ssrLoadModule('/tests/offsets.ts');
  await server.ssrLoadModule('/tests/calendar.ts');
  await server.ssrLoadModule('/tests/home.ts');
  await server.ssrLoadModule('/tests/appearance-cycle.ts');
} finally {
  await server.close();
}
