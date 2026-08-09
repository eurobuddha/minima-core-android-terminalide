# Graph Report - terminalide  (2026-08-01)

## Corpus Check
- 32 files · ~28,920 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 375 nodes · 809 edges · 23 communities (18 shown, 5 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 50 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `09ce588e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TerminalView
- ReceiverDB
- ScriptsView
- ScriptEditorActivity
- TerminalAdapter
- TxnView
- Cmd
- .forCommand
- MainActivity
- KissHighlighter
- CaretEditText
- Lint
- gradlew
- KissVm.java
- BaseView
- HistoryDB
- BroadcastReceiver
- Bundle
- JSONObject
- LinearLayout

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 45 edges
2. `TxnView` - 34 edges
3. `ScriptEditorActivity` - 28 edges
4. `ScriptsView` - 21 edges
5. `MainActivity` - 20 edges
6. `NodeApi` - 20 edges
7. `ScriptDB` - 19 edges
8. `ReceiverDB` - 19 edges
9. `Cb` - 16 edges
10. `BaseView` - 14 edges

## Surprising Connections (you probably didn't know these)
- `TerminalView` --references--> `SelectionScrollView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/terminal/TerminalView.java → app/src/main/java/com/eurobuddha/terminalide/terminal/SelectionScrollView.java
- `ScriptsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `LogsView` --references--> `ReceiverDB`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/LogsView.java → app/src/main/java/com/eurobuddha/terminalide/receiver/ReceiverDB.java

## Import Cycles
- None detected.

## Communities (23 total, 5 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.09
Nodes (12): Activity, NodeApi, Override, ScrollView, TextView, TerminalView, BaseView, CaretEditText (+4 more)

### Community 1 - "ReceiverDB"
Cohesion: 0.15
Nodes (11): Context, Override, MinimaNotifyReceiver, Context, Cursor, JSONObject, Override, SQLiteDatabase (+3 more)

### Community 2 - "ScriptsView"
Cohesion: 0.11
Nodes (13): Context, Cursor, Override, SQLiteDatabase, Script, ScriptDB, Activity, Override (+5 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.08
Nodes (17): Bundle, EditText, JSONObject, LinearLayout, Override, TextView, ScriptEditorActivity, Cb (+9 more)

### Community 4 - "TerminalAdapter"
Cohesion: 0.17
Nodes (5): Override, View, TerminalAdapter, PagerAdapter, ViewGroup

### Community 5 - "TxnView"
Cohesion: 0.21
Nodes (5): Activity, EditText, TextView, TxnView, OnClickListener

### Community 6 - "Cmd"
Cohesion: 0.18
Nodes (7): Cmd, CommandRegistry, Param, Item, Context, Result, Suggest

### Community 7 - ".forCommand"
Cohesion: 0.25
Nodes (6): HelpStore, Context, JSONObject, Doc, Context, ParamDocs

### Community 8 - "MainActivity"
Cohesion: 0.11
Nodes (18): ActivityResultLauncher, Intent, NodeApi, Override, TextView, MainActivity, Activity, Context (+10 more)

### Community 9 - "KissHighlighter"
Cohesion: 0.19
Nodes (9): EditText, Override, Pattern, KissHighlighter, Pattern, OutputFormatter, Editable, SpannableStringBuilder (+1 more)

### Community 10 - "CaretEditText"
Cohesion: 0.15
Nodes (10): CaretEditText, Context, Override, OnCaretMoved, Context, Override, SelectionScrollView, AppCompatEditText (+2 more)

### Community 11 - "Lint"
Cohesion: 0.36
Nodes (4): Override, Pattern, Lint, LintEngine

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 17 - "BaseView"
Cohesion: 0.18
Nodes (7): BaseView, Activity, View, Activity, Override, TextView, LogsView

### Community 18 - "HistoryDB"
Cohesion: 0.24
Nodes (4): HistoryDB, Context, Override, SQLiteDatabase

## Knowledge Gaps
- **1 isolated node(s):** `KissVm`
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TerminalView` connect `TerminalView` to `CaretEditText`, `TerminalAdapter`?**
  _High betweenness centrality (0.203) - this node is a cross-community bridge._
- **Why does `ScriptsView` connect `ScriptsView` to `BaseView`, `ScriptEditorActivity`, `TerminalAdapter`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **Why does `TxnView` connect `TxnView` to `BaseView`, `ScriptEditorActivity`, `TerminalAdapter`?**
  _High betweenness centrality (0.132) - this node is a cross-community bridge._
- **What connects `KissVm` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.09468599033816426 - nodes in this community are weakly interconnected._
- **Should `ScriptsView` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._
- **Should `ScriptEditorActivity` be split into smaller, more focused modules?**
  _Cohesion score 0.083710407239819 - nodes in this community are weakly interconnected._