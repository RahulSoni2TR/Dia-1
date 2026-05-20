import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/rates': 'http://localhost:8080',
      '/get-security-question': 'http://localhost:8080',
      '/process-login': 'http://localhost:8080',
      '/register': 'http://localhost:8080',
      '/reset-password': 'http://localhost:8080',
      '/verify-security-answer': 'http://localhost:8080',
      '/reset-ns-password': 'http://localhost:8080',
      '/modify-product': 'http://localhost:8080',
      '/login': 'http://localhost:8080',
      '/logout': 'http://localhost:8080',
      '/public-rates': 'http://localhost:8080'
    }
  }
});
