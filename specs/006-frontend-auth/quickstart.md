# Quickstart: Frontend Authentication Integration

**Feature**: 006-frontend-auth
**Date**: 2025-11-25

## Prerequisites

- Node.js 20+ (LTS recommended)
- pnpm 9+ (package manager)
- Backend services running (auth-service, api-gateway, postgres, redis)
- Docker and Docker Compose (for backend)

## Quick Start

### 1. Start Backend Services

```bash
# From repository root
docker-compose up -d postgres-auth redis auth-service api-gateway eureka-server
```

Verify services are running:
```bash
docker-compose ps
# Check auth-service health
curl http://localhost:8080/api/auth/actuator/health
```

### 2. Create Frontend Project

```bash
# Create frontend directory
mkdir -p frontend
cd frontend

# Initialize with Vite + React + TypeScript
pnpm create vite@latest . --template react-swc-ts

# Install dependencies
pnpm install

# Install shadcn/ui and required packages
pnpm add @radix-ui/react-slot class-variance-authority clsx tailwind-merge lucide-react
pnpm add tailwindcss @tailwindcss/vite tw-animate-css
pnpm add @tanstack/react-router @tanstack/react-query axios zod react-hook-form @hookform/resolvers
pnpm add zustand sonner

# Dev dependencies
pnpm add -D @types/node @tanstack/router-plugin @tanstack/react-query-devtools @tanstack/react-router-devtools
```

### 3. Initialize shadcn/ui

```bash
pnpm dlx shadcn@latest init
```

Select options:
- Style: **New York**
- Base color: **Slate**
- CSS variables: **Yes**

Add required components:
```bash
pnpm dlx shadcn@latest add button card form input label separator toast sonner
```

### 4. Configure Vite

```typescript
// vite.config.ts
import path from 'path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'
import { TanStackRouterVite } from '@tanstack/router-plugin/vite'

export default defineConfig({
  plugins: [
    TanStackRouterVite({
      target: 'react',
      autoCodeSplitting: true,
    }),
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### 5. Configure TypeScript

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 6. Create Initial File Structure

```bash
# Create directory structure
mkdir -p src/{components/{ui,layout},features/{auth/{sign-in,sign-up,forgot-password,reset-password},dashboard},hooks,lib/{i18n,validations},routes/{_authenticated,\(auth\)},services,stores,types,styles,context}

# Create placeholder files
touch src/stores/auth-store.ts
touch src/services/auth.service.ts
touch src/lib/api-client.ts
touch src/lib/i18n/translations.ts
touch src/lib/validations/auth.schema.ts
touch src/types/auth.types.ts
touch src/types/api.types.ts
```

### 7. Run Development Server

```bash
pnpm dev
```

Open http://localhost:5173 in your browser.

## Development Workflow

### File-Based Routing

TanStack Router uses file-based routing. Create route files in `src/routes/`:

```
src/routes/
├── __root.tsx           # Root layout
├── (auth)/              # Public auth routes (parentheses = route group)
│   ├── sign-in.tsx
│   ├── sign-up.tsx
│   ├── forgot-password.tsx
│   └── reset-password.tsx
├── _authenticated/      # Protected routes (underscore = layout route)
│   ├── route.tsx        # Auth guard
│   └── index.tsx        # Dashboard
└── index.tsx            # Home redirect
```

After creating route files, run:
```bash
pnpm exec tsr generate
```

Or configure automatic generation in vite.config.ts (already included above).

### Running Tests

```bash
# Unit tests
pnpm test

# E2E tests (requires Playwright)
pnpm exec playwright test
```

### Building for Production

```bash
pnpm build
pnpm preview  # Preview production build
```

## Environment Variables

Create `.env` file:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080

# Development
VITE_DEV_MODE=true
```

Access in code:
```typescript
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
```

## Common Tasks

### Adding a New shadcn/ui Component

```bash
pnpm dlx shadcn@latest add <component-name>
```

### Creating a New Route

1. Create file in `src/routes/` following naming convention
2. Run `pnpm exec tsr generate` (or automatic if configured)
3. Route types are auto-generated in `src/routeTree.gen.ts`

### Adding Translations

Edit `src/lib/i18n/translations.ts`:
```typescript
export const translations = {
  en: {
    NEW_ERROR_CODE: 'English message',
  },
  km: {
    NEW_ERROR_CODE: 'Khmer message',
  },
}
```

## Troubleshooting

### CORS Errors

Ensure backend CORS is configured:
```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
config.setAllowCredentials(true);
```

### Cookie Not Being Set

1. Check `withCredentials: true` in axios config
2. Verify `SameSite` and `Secure` cookie flags
3. For local development, use `Secure: false`

### Route Types Not Updating

```bash
# Regenerate route tree
pnpm exec tsr generate
```

### TanStack Query Cache Issues

```typescript
// Clear all query cache
queryClient.clear()

// Invalidate specific query
queryClient.invalidateQueries({ queryKey: ['auth', 'me'] })
```

## Reference Links

- [TanStack Router Docs](https://tanstack.com/router/latest)
- [TanStack Query Docs](https://tanstack.com/query/latest)
- [shadcn/ui Docs](https://ui.shadcn.com)
- [Zustand Docs](https://docs.pmnd.rs/zustand)
- [Zod Docs](https://zod.dev)

## Next Steps

1. Review `data-model.md` for TypeScript types
2. Review `contracts/endpoints.md` for API integration
3. Implement auth store in `src/stores/auth-store.ts`
4. Create API service in `src/services/auth.service.ts`
5. Build sign-in form following shadcn-admin patterns
