import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo (npm run dev) Vite reenvía /api al backend y /auth a Keycloak,
// imitando lo que hace Nginx en producción (mismo origen => sin problemas de CORS).
export default defineConfig({
  plugins: [react()],
  build: { chunkSizeWarningLimit: 1000 },
  server: {
    port: 5173,
    proxy: {
      '/api': process.env.DEV_BACKEND_URL || 'http://localhost:8090',
      '/auth': process.env.DEV_KEYCLOAK_URL || 'http://localhost:9090',
    },
  },
})
