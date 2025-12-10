# Ticket System UI

Modern web interface for the Ticket System, built with Next.js 14, shadcn/ui, and TailwindCSS.

## Features

- **Modern UI**: Built with shadcn/ui components and TailwindCSS
- **Dark/Light Theme**: Full theme support with system preference detection
- **Responsive Design**: Works on desktop and mobile devices
- **Type-Safe**: Written in TypeScript with full type coverage

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **UI Components**: shadcn/ui (Radix UI primitives)
- **Styling**: TailwindCSS
- **Icons**: Lucide React
- **State Management**: React Query (TanStack Query)
- **Forms**: React Hook Form + Zod validation
- **Theme**: next-themes

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
# Navigate to UI directory
cd ui

# Install dependencies
npm install

# Copy environment file
cp .env.local.example .env.local

# Start development server
npm run dev
```

The application will be available at [http://localhost:3000](http://localhost:3000).

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | BFF API base URL | `http://localhost:8080` |

## Project Structure

```
ui/
├── src/
│   ├── app/                    # Next.js App Router pages
│   │   ├── events/            # Events management
│   │   ├── partners/          # Partners management
│   │   ├── users/             # Users management
│   │   ├── tickets/           # Tickets overview
│   │   ├── settings/          # Application settings
│   │   ├── layout.tsx         # Root layout
│   │   ├── page.tsx           # Dashboard
│   │   └── globals.css        # Global styles
│   ├── components/
│   │   ├── layout/            # Layout components (Sidebar, Header)
│   │   ├── ui/                # shadcn/ui components
│   │   ├── theme-provider.tsx # Theme context provider
│   │   └── theme-toggle.tsx   # Theme switcher component
│   └── lib/
│       ├── api.ts             # API client
│       └── utils.ts           # Utility functions
├── public/                     # Static assets
├── tailwind.config.ts         # Tailwind configuration
├── next.config.js             # Next.js configuration
└── package.json
```

## Pages

| Route | Description |
|-------|-------------|
| `/` | Dashboard with overview statistics |
| `/events` | Events management (CRUD, publish, cancel) |
| `/partners` | Partners management (approve, reject, suspend) |
| `/users` | User management |
| `/tickets` | Ticket types overview |
| `/settings` | Application settings and theme configuration |

## Theme Customization

The application supports three theme modes:
- **Light**: Clean light theme
- **Dark**: Modern dark theme (default)
- **System**: Follows OS preference

Theme can be changed via the Settings page or the theme toggle in the header.

### Color Scheme

The default color scheme uses a violet/purple accent. The theme variables are defined in `globals.css` and can be customized:

```css
:root {
  --primary: 262.1 83.3% 57.8%;
  /* ... other variables */
}

.dark {
  --primary: 263.4 70% 50.4%;
  /* ... other variables */
}
```

## API Integration

The UI connects to the BFF (Backend for Frontend) service. Ensure the BFF is running before starting the UI:

```bash
# Start BFF (from project root)
cd bff
gradle run
```

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run start` | Start production server |
| `npm run lint` | Run ESLint |

## Contributing

1. Follow the existing code style
2. Use TypeScript for all new files
3. Add proper types for API responses
4. Test on both light and dark themes
