# Offline help for every node command

`app/src/main/assets/help.json` is what the terminal shows for `help command:<x>`, for the
bare `help` listing, in the long-press help dialog and as the per-parameter descriptions in
the autocomplete dropdown. It is **generated** — never edit it by hand.

The node's own help is incomplete: `vault` never mentions `password:`, `confirm:`,
`numkeys:` or the `testphrase` / `resetkeys` actions, `peers` omits `url:`/`file:`/`max:`,
about twenty commands (`sendfrom`, `createfrom`, `signfrom`, `txnauto`, `txnmmr`, …) have no
full page at all, and every `txn*` page starts with a stray tab (`"\txnlist"`). So the app
ships a completed version:

| file | role |
|---|---|
| `Dump.java` | dumps name, brief, fullhelp and `getValidParams()` of every command from a node jar |
| `node-help.json` | that raw dump — the baseline (currently `core/minima-core`, jar/minima.jar) |
| `gen_help.py` | `SUPPLEMENT` = every missing parameter / action value / page, written from the node source, plus the generator |

`gen_help.py` also cross-checks `CommandRegistry.java` against the node's valid-param lists
(the node rejects any parameter not in that list, so they must match exactly) and refuses to
write `help.json` while any accepted parameter or action value is still undocumented.

## Regenerate after a node upgrade

```sh
cd tools/help
JAR=../../../../core/minima-core/jar/minima.jar
javac -cp "$JAR" Dump.java && java -cp "$JAR:." Dump | python3 -c 'import json,sys;json.dump(json.load(sys.stdin),open("node-help.json","w"),indent=1,sort_keys=True)'
python3 gen_help.py            # fix CommandRegistry / SUPPLEMENT until it passes
rm -f Dump.class
```
