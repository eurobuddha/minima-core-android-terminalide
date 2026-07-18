# Terminal IDE

**Professional tooling for the Minima protocol — a native Android companion app for Minima Core.**

> ⚠ **In development — use at your own risk.** All apps in this ecosystem are experimental and
> provided AS-IS. Nothing here is financial advice. Real funds can be at risk.

Terminal IDE talks to the [Minima Core](https://github.com/spartacusrex-minima/minima-core-android)
node app over the native MinimaAPI IPC — install both, then enable **Terminal IDE** in
*Minima Core → Apps*.

## Features

**Terminal** — a full node command line, done properly:
- Autocomplete suggestion chips for all ~100 node commands + per-command `param:` hints
- Persistent command history (▲/▼, long-press for the list) and named favorites
- Colorized, pretty-printed JSON output with per-command timing; long-press any block to copy
- Guard rails against the known node-killers (unbounded `coins`, oversized `history` pages)

**Scripts** — a KISS VM smart-contract IDE:
- Script library with proven starter templates (timelock, hashlock, multisig, commit-reveal,
  phase-transition covenant)
- Live syntax highlighting and a char counter with the ~1200-char on-chain danger line
- Static lints for the silent killers: underscored variable names, `@BLKNUM`,
  `NUMBER(SHA3(...))` overflow, unbalanced blocks
- **Check** = parse + script address via the node's own VM (`runscript`)
- **Run** = execute offline with your own `state` / `prevstate` / `globals` / `signatures`
  and inspect the full trace and end variables — real contract testing, no funds at risk
- **Deploy** = `newscript` with trackall control

**Txn** — a guided manual-UTXO transaction workbench:
- create → coin picker → outputs → state → sign → basics → post, in the proven order
- Live `txncheck` balance summary with an explicit BURN warning when inputs ≠ outputs
- Export/import (hex) for multi-node signing flows; delete guard so abandoned txns
  don't lock coins

**Logs** — Minima Core log events, kept for one week.

## Build

Requires JDK 21 (pinned via `gradle.properties` to the Android Studio JBR).

```bash
./gradlew assembleDebug
```

## License

MIT © 2026 eurobuddha — see [LICENSE](LICENSE). Bundles the Apache-2.0 `minimaapi.aar`
from Minima Core (see [NOTICE](NOTICE)).
