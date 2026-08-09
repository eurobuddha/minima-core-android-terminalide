# Graph Report - terminalide  (2026-08-01)

## Corpus Check
- 32 files · ~28,920 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 374 nodes · 842 edges · 20 communities (18 shown, 2 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 69 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0989b698`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TerminalView
- ReceiverDB
- NodeApi
- ScriptEditorActivity
- MainActivity
- TxnView
- Cmd
- .forCommand
- SessionExport
- KissHighlighter
- CaretEditText
- Lint
- gradlew
- KissVm.java
- SelectionScrollView
- Terminal IDE
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 46 edges
2. `TxnView` - 34 edges
3. `ScriptEditorActivity` - 28 edges
4. `NodeApi` - 25 edges
5. `ScriptsView` - 21 edges
6. `MainActivity` - 20 edges
7. `ScriptDB` - 19 edges
8. `ReceiverDB` - 19 edges
9. `Cb` - 18 edges
10. `BaseView` - 16 edges

## Surprising Connections (you probably didn't know these)
- `ScriptsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/terminal/TerminalView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `MainActivity` --references--> `NodeApi`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/MainActivity.java → app/src/main/java/com/eurobuddha/terminalide/NodeApi.java

## Import Cycles
- None detected.

## Communities (20 total, 2 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.09
Nodes (11): HistoryDB, Context, Override, SQLiteDatabase, Activity, LinearLayout, Override, ScrollView (+3 more)

### Community 1 - "ReceiverDB"
Cohesion: 0.08
Nodes (19): BaseView, Activity, View, Activity, Override, TextView, LogsView, Context (+11 more)

### Community 2 - "NodeApi"
Cohesion: 0.07
Nodes (18): Context, Cursor, Override, SQLiteDatabase, Script, ScriptDB, Activity, Override (+10 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.13
Nodes (9): Bundle, EditText, JSONObject, LinearLayout, Override, TextView, ScriptEditorActivity, Cb (+1 more)

### Community 4 - "MainActivity"
Cohesion: 0.09
Nodes (17): ActivityResultLauncher, BroadcastReceiver, Bundle, Intent, Override, TextView, MainActivity, Override (+9 more)

### Community 5 - "TxnView"
Cohesion: 0.21
Nodes (5): Activity, EditText, TextView, TxnView, OnClickListener

### Community 6 - "Cmd"
Cohesion: 0.17
Nodes (7): Cmd, CommandRegistry, Param, Item, Context, Result, Suggest

### Community 7 - ".forCommand"
Cohesion: 0.25
Nodes (6): HelpStore, Context, JSONObject, Doc, Context, ParamDocs

### Community 8 - "SessionExport"
Cohesion: 0.26
Nodes (5): Activity, Context, Intent, SessionExport, Uri

### Community 9 - "KissHighlighter"
Cohesion: 0.16
Nodes (10): EditText, Override, Pattern, KissHighlighter, Pattern, OutputFormatter, JSONObject, Editable (+2 more)

### Community 10 - "CaretEditText"
Cohesion: 0.27
Nodes (6): CaretEditText, AttributeSet, Context, Override, OnCaretMoved, AppCompatEditText

### Community 11 - "Lint"
Cohesion: 0.36
Nodes (4): Override, Pattern, Lint, LintEngine

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 17 - "SelectionScrollView"
Cohesion: 0.29
Nodes (6): AttributeSet, Context, Override, SelectionScrollView, MotionEvent, ScrollView

### Community 18 - "Terminal IDE"
Cohesion: 0.40
Nodes (4): Build, Features, License, Terminal IDE

## Knowledge Gaps
- **5 isolated node(s):** `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Features`, `Build`, `License`
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TerminalView` connect `TerminalView` to `ReceiverDB`, `NodeApi`, `MainActivity`, `Cmd`, `KissHighlighter`, `CaretEditText`, `SelectionScrollView`?**
  _High betweenness centrality (0.411) - this node is a cross-community bridge._
- **Why does `NodeApi` connect `NodeApi` to `TerminalView`, `ScriptEditorActivity`, `MainActivity`, `TxnView`?**
  _High betweenness centrality (0.235) - this node is a cross-community bridge._
- **Why does `BaseView` connect `ReceiverDB` to `TerminalView`, `NodeApi`, `MainActivity`, `TxnView`?**
  _High betweenness centrality (0.136) - this node is a cross-community bridge._
- **What connects `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Features` to the rest of the system?**
  _5 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.08928571428571429 - nodes in this community are weakly interconnected._
- **Should `ReceiverDB` be split into smaller, more focused modules?**
  _Cohesion score 0.08350951374207188 - nodes in this community are weakly interconnected._
- **Should `NodeApi` be split into smaller, more focused modules?**
  _Cohesion score 0.07450980392156863 - nodes in this community are weakly interconnected._