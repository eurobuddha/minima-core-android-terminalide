# Graph Report - terminalide  (2026-09-03)

## Corpus Check
- 38 files · ~48,361 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 431 nodes · 895 edges · 38 communities (21 shown, 17 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 67 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `67992286`
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
- SelectionScrollView.java
- Terminal IDE
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- gen_help.py
- Offline help for every node command
- Bundle
- EditText
- JSONObject
- LinearLayout
- Override
- TextView
- JSONObject
- install.sh
- pre-commit
- Context
- Activity
- LinearLayout
- Override
- ScrollView
- TextView
- HistoryDB

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 46 edges
2. `TxnView` - 34 edges
3. `ScriptEditorActivity` - 28 edges
4. `ScriptsView` - 21 edges
5. `MainActivity` - 20 edges
6. `NodeApi` - 19 edges
7. `ReceiverDB` - 19 edges
8. `Cb` - 18 edges
9. `ScriptDB` - 17 edges
10. `TerminalAdapter` - 16 edges

## Surprising Connections (you probably didn't know these)
- `ScriptsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `MainActivity` --references--> `NodeApi`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/MainActivity.java → app/src/main/java/com/eurobuddha/terminalide/NodeApi.java
- `ScriptsView` --references--> `NodeApi`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/NodeApi.java

## Import Cycles
- None detected.

## Communities (38 total, 17 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.10
Nodes (11): android.app.Activity, android.widget.ScrollView, android.widget.TextView, TerminalView, CaretEditText, com.eurobuddha.terminalide.BaseView, HistoryDB, Item (+3 more)

### Community 1 - "ReceiverDB"
Cohesion: 0.07
Nodes (21): BaseView, Activity, View, Activity, Override, TextView, LogsView, Context (+13 more)

### Community 2 - "ScriptsView"
Cohesion: 0.11
Nodes (12): Context, Cursor, Override, SQLiteDatabase, Script, ScriptDB, Activity, Override (+4 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.11
Nodes (14): android.os.Bundle, android.widget.EditText, android.widget.LinearLayout, androidx.appcompat.app.AppCompatActivity, ScriptEditorActivity, com.eurobuddha.terminalide.NodeApi, HorizontalScrollView, JSONObject (+6 more)

### Community 4 - "MainActivity"
Cohesion: 0.09
Nodes (17): ActivityResultLauncher, BroadcastReceiver, Bundle, Intent, Override, TextView, MainActivity, Override (+9 more)

### Community 5 - "TxnView"
Cohesion: 0.11
Nodes (12): Cb, Context, JSONObject, NodeApi, PairingListener, Activity, EditText, TextView (+4 more)

### Community 6 - "Cmd"
Cohesion: 0.18
Nodes (7): Cmd, CommandRegistry, Param, Item, Context, Result, Suggest

### Community 7 - ".forCommand"
Cohesion: 0.22
Nodes (6): android.content.Context, HelpStore, Context, JSONObject, Doc, ParamDocs

### Community 8 - "SessionExport"
Cohesion: 0.26
Nodes (5): Activity, Context, Intent, SessionExport, Uri

### Community 9 - "KissHighlighter"
Cohesion: 0.20
Nodes (9): EditText, Override, Pattern, KissHighlighter, Pattern, OutputFormatter, Editable, SpannableStringBuilder (+1 more)

### Community 10 - "CaretEditText"
Cohesion: 0.27
Nodes (6): CaretEditText, AttributeSet, Context, Override, OnCaretMoved, AppCompatEditText

### Community 11 - "Lint"
Cohesion: 0.36
Nodes (4): Override, Pattern, Lint, LintEngine

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 17 - "SelectionScrollView.java"
Cohesion: 0.31
Nodes (5): AttributeSet, Context, Override, SelectionScrollView, MotionEvent

### Community 18 - "Terminal IDE"
Cohesion: 0.40
Nodes (4): Build, Features, License, Terminal IDE

### Community 19 - "User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly."
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

### Community 20 - "gen_help.py"
Cohesion: 0.19
Nodes (21): add_action_values(), add_examples(), any_marked(), build_all(), build_page(), fix_title(), headers(), insert_params() (+13 more)

### Community 37 - "HistoryDB"
Cohesion: 0.27
Nodes (5): HistoryDB, Context, Override, SQLiteDatabase, SQLiteOpenHelper

## Knowledge Gaps
- **8 isolated node(s):** `Regenerate after a node upgrade`, `install.sh script`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Versioning guardrail — every code change ships with a version bump`, `KissVm` (+3 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TerminalView` connect `TerminalView` to `ScriptEditorActivity`, `MainActivity`, `.forCommand`?**
  _High betweenness centrality (0.281) - this node is a cross-community bridge._
- **Why does `ScriptsView` connect `ScriptsView` to `ReceiverDB`, `MainActivity`, `TxnView`?**
  _High betweenness centrality (0.107) - this node is a cross-community bridge._
- **Why does `TxnView` connect `TxnView` to `ReceiverDB`, `MainActivity`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **What connects `Regenerate after a node upgrade`, `install.sh script`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.` to the rest of the system?**
  _8 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.09936575052854123 - nodes in this community are weakly interconnected._
- **Should `ReceiverDB` be split into smaller, more focused modules?**
  _Cohesion score 0.07358156028368794 - nodes in this community are weakly interconnected._
- **Should `ScriptsView` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._