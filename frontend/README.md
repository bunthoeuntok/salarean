# Salarean SMS Frontend

Next.js 14 frontend application for the Salarean Student Management System.

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript 5.x
- **Styling**: Tailwind CSS + shadcn/ui
- **State Management**: Zustand
- **Data Fetching**: TanStack Query
- **Forms**: react-hook-form + Zod
- **HTTP Client**: Axios

## Prerequisites

- Node.js 20+
- pnpm 9+

## Getting Started

### Install dependencies

```bash
pnpm install
```

### Environment setup

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Run development server

```bash
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## Available Scripts

| Command | Description |
|---------|-------------|
| `pnpm dev` | Start development server |
| `pnpm build` | Build for production |
| `pnpm start` | Start production server |
| `pnpm lint` | Run ESLint |
| `pnpm format` | Format code with Prettier |

## Docker

### Build image

```bash
docker build -t sms-frontend .
```

### Run with Docker Compose

From the project root:

```bash
docker-compose up frontend
```

## Project Structure

```
src/
├── app/                    # Next.js App Router pages
│   ├── (auth)/            # Auth routes (sign-in, sign-up, etc.)
│   ├── (authenticated)/   # Protected routes
│   └── layout.tsx         # Root layout
├── assets/                # Static assets (logo, etc.)
├── components/            # Shared components
│   ├── layout/           # Layout components
│   └── ui/               # shadcn/ui components
├── features/              # Feature modules
│   ├── auth/             # Authentication feature
│   └── errors/           # Error pages
├── hooks/                 # Custom React hooks
├── lib/                   # Utilities and helpers
│   ├── i18n/             # Internationalization
│   └── api.ts            # Axios instance
├── services/              # API service functions
├── store/                 # Zustand stores
└── types/                 # TypeScript types
```

## Features

- Teacher authentication (sign in, sign up, sign out)
- Password reset flow
- Session persistence with automatic token refresh
- Bilingual support (English/Khmer)
- Dark mode support
- Responsive design
