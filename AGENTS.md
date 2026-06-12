# AGENTS.md

## Project Overview

**beicon** is a ClojureScript library wrapping RxJS for reactive streams. Published to Clojars as `funcool/beicon2`.

## Commands

```bash
# Install dependencies
pnpm install

# Run tests (compile + execute)
pnpm test

# Watch tests
pnpm test:watch

# Compile tests only
pnpm test:compile

# Run compiled tests
pnpm test:run

# Build JAR
clojure -T:build jar

# Clean build artifacts
clojure -T:build clean
```

## Architecture

- **Source**: `src/beicon/v2/`
  - `core.clj` - Clojure macros (push!, error!, end!, comp)
  - `core.cljs` - Main API wrapping RxJS Observable/Subject/etc
  - `operators.cljs` - RxJS operators (map, filter, etc)
- **Tests**: `test/beicon/tests/v2_test.cljs`
- **Test pattern**: `^beicon.tests.*-test$` (shadow-cljs)

## Key Facts

- ClojureScript-only library (no JVM runtime code)
- Uses shadow-cljs for compilation and testing
- Wraps RxJS 8.0.0-alpha.14
- Tests run on Node.js via shadow-cljs `:node-test` target
- Package manager: pnpm (see `packageManager` in package.json)
- Kaocha config exists in `tests.edn` but shadow-cljs is the actual test runner

## Gotchas

- The `tools.clj` script is legacy; prefer `pnpm test` commands
- Macros in `core.clj` are required by `core.cljs` via `:require-macros`
