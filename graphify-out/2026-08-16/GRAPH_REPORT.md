# Graph Report - terminalide  (2026-08-11)

## Corpus Check
- 32 files · ~29,030 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 388 nodes · 840 edges · 28 communities (20 shown, 8 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 69 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d8da9a36`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TerminalView
- ReceiverDB
- ScriptsView
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
- NodeApi
- BaseView
- Bundle
- EditText
- JSONObject
- LinearLayout
- Override
- TextView

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 46 edges
2. `TxnView` - 34 edges
3. `ScriptEditorActivity` - 28 edges
4. `NodeApi` - 22 edges
5. `ScriptsView` - 21 edges
6. `MainActivity` - 20 edges
7. `ReceiverDB` - 19 edges
8. `Cb` - 18 edges
9. `ScriptDB` - 17 edges
10. `BaseView` - 16 edges

## Surprising Connections (you probably didn't know these)
- `ScriptsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `LogsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/LogsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/terminal/TerminalView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java

## Import Cycles
- None detected.

## Communities (28 total, 8 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.08
Nodes (15): HistoryDB, Context, Override, SQLiteDatabase, Pattern, OutputFormatter, Activity, JSONObject (+7 more)

### Community 1 - "ReceiverDB"
Cohesion: 0.11
Nodes (16): Activity, Override, TextView, LogsView, Context, Intent, Override, MinimaNotifyReceiver (+8 more)

### Community 2 - "ScriptsView"
Cohesion: 0.11
Nodes (12): Context, Cursor, Override, SQLiteDatabase, Script, ScriptDB, Activity, Override (+4 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.11
Nodes (15): android.os.Bundle, android.widget.EditText, android.widget.LinearLayout, android.widget.TextView, androidx.appcompat.app.AppCompatActivity, ScriptEditorActivity, com.eurobuddha.terminalide.NodeApi, HorizontalScrollView (+7 more)

### Community 4 - "MainActivity"
Cohesion: 0.10
Nodes (17): ActivityResultLauncher, BroadcastReceiver, Bundle, Intent, Override, TextView, MainActivity, Override (+9 more)

### Community 5 - "TxnView"
Cohesion: 0.13
Nodes (7): Cb, Activity, EditText, TextView, TxnView, OnClickListener, Script

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
Cohesion: 0.30
Nodes (6): EditText, Override, Pattern, KissHighlighter, Editable, TextWatcher

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

### Community 20 - "NodeApi"
Cohesion: 0.19
Nodes (6): Context, JSONObject, NodeApi, PairingListener, Handler, MinimaAPI

### Community 21 - "BaseView"
Cohesion: 0.36
Nodes (3): BaseView, Activity, View

## Knowledge Gaps
- **5 isolated node(s):** `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Features`, `Build`, `License`
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TerminalView` connect `TerminalView` to `MainActivity`, `Cmd`, `CaretEditText`, `SelectionScrollView`, `NodeApi`, `BaseView`?**
  _High betweenness centrality (0.375) - this node is a cross-community bridge._
- **Why does `NodeApi` connect `NodeApi` to `TerminalView`, `ScriptsView`, `ScriptEditorActivity`, `MainActivity`, `TxnView`?**
  _High betweenness centrality (0.148) - this node is a cross-community bridge._
- **Why does `BaseView` connect `BaseView` to `TerminalView`, `ReceiverDB`, `ScriptsView`, `MainActivity`, `TxnView`?**
  _High betweenness centrality (0.132) - this node is a cross-community bridge._
- **What connects `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Features` to the rest of the system?**
  _5 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.0750151240169389 - nodes in this community are weakly interconnected._
- **Should `ReceiverDB` be split into smaller, more focused modules?**
  _Cohesion score 0.10756302521008404 - nodes in this community are weakly interconnected._
- **Should `ScriptsView` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._