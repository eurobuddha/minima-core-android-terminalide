# Graph Report - terminalide  (2026-10-21)

## Corpus Check
- 43 files · ~50,822 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 448 nodes · 1093 edges · 22 communities (18 shown, 4 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 118 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f725a5e8`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TerminalView
- ScriptsView
- ReceiverDB
- ScriptEditorActivity
- MainActivity
- TxnView
- Cmd
- android.content.Context
- NodeApi
- android.widget.TextView
- TerminalRenderingTest
- gradlew
- KissVm.java
- Terminal IDE
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- gen_help.py
- Offline help for every node command
- install.sh
- pre-commit

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 52 edges
2. `TxnView` - 34 edges
3. `ScriptEditorActivity` - 28 edges
4. `NodeApi` - 27 edges
5. `ScriptsView` - 21 edges
6. `ReceiverDB` - 21 edges
7. `MainActivity` - 20 edges
8. `Cb` - 19 edges
9. `ScriptDB` - 19 edges
10. `TerminalRenderingTest` - 19 edges

## Surprising Connections (you probably didn't know these)
- `ScriptsView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/ide/ScriptsView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/terminal/TerminalView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `LogsViewTest` --references--> `LogsView`  [EXTRACTED]
  app/src/test/java/com/eurobuddha/terminalide/LogsViewTest.java → app/src/main/java/com/eurobuddha/terminalide/LogsView.java

## Import Cycles
- None detected.

## Communities (22 total, 4 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.07
Nodes (11): android.content.res.Configuration, android.database.sqlite.SQLiteOpenHelper, HistoryDB, Override, TerminalLabel, TextView, ThreadPoolExecutor, TerminalView (+3 more)

### Community 1 - "ScriptsView"
Cohesion: 0.15
Nodes (6): android.widget.ListView, Script, ScriptDB, Override, ScriptsView, Templates

### Community 2 - "ReceiverDB"
Cohesion: 0.09
Nodes (14): android.database.sqlite.SQLiteDatabase, Override, Override, Override, Override, ReceiverDB, Override, After (+6 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.12
Nodes (8): Override, Lint, LintEngine, JSONObject, Override, TextView, ScriptEditorActivity, HorizontalScrollView

### Community 4 - "MainActivity"
Cohesion: 0.07
Nodes (21): android.content.BroadcastReceiver, android.content.Intent, android.net.Uri, android.os.Bundle, android.view.Menu, android.view.MenuItem, android.view.ViewGroup, androidx.activity.result.ActivityResultLauncher (+13 more)

### Community 5 - "TxnView"
Cohesion: 0.19
Nodes (4): Cb, EditText, TxnView, OnClickListener

### Community 6 - "Cmd"
Cohesion: 0.18
Nodes (6): Cmd, CommandRegistry, Param, Item, Result, Suggest

### Community 7 - "android.content.Context"
Cohesion: 0.09
Nodes (16): android.content.Context, android.database.Cursor, android.util.AttributeSet, android.view.MotionEvent, androidx.appcompat.widget.AppCompatEditText, JSONObject, CaretEditText, Override (+8 more)

### Community 8 - "NodeApi"
Cohesion: 0.24
Nodes (5): android.os.Handler, NodeApi, PairingListener, com.eurobuddha.minimaapi.MinimaAPI, MinimaAPI

### Community 9 - "android.widget.TextView"
Cohesion: 0.10
Nodes (19): android.app.Activity, android.text.Editable, android.text.SpannableStringBuilder, android.text.TextWatcher, android.view.View, android.widget.EditText, android.widget.LinearLayout, android.widget.ScrollView (+11 more)

### Community 10 - "TerminalRenderingTest"
Cohesion: 0.14
Nodes (8): Override, TerminalOutputTest, TerminalRenderingTest, org.junit.After, org.junit.Before, org.junit.runner.RunWith, org.junit.Test, org.robolectric.annotation.Config

### Community 12 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 18 - "Terminal IDE"
Cohesion: 0.40
Nodes (4): Build, Features, License, Terminal IDE

### Community 19 - "User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly."
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

### Community 20 - "gen_help.py"
Cohesion: 0.19
Nodes (21): add_action_values(), add_examples(), any_marked(), build_all(), build_page(), fix_title(), headers(), insert_params() (+13 more)

## Knowledge Gaps
- **8 isolated node(s):** `install.sh script`, `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.`, `Versioning guardrail — every code change ships with a version bump`, `Features` (+3 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TerminalView` connect `TerminalView` to `MainActivity`, `TxnView`, `Cmd`, `android.content.Context`, `NodeApi`, `android.widget.TextView`, `TerminalRenderingTest`?**
  _High betweenness centrality (0.218) - this node is a cross-community bridge._
- **Why does `NodeApi` connect `NodeApi` to `TerminalView`, `ScriptsView`, `ScriptEditorActivity`, `MainActivity`, `TxnView`, `android.content.Context`, `android.widget.TextView`, `TerminalRenderingTest`?**
  _High betweenness centrality (0.166) - this node is a cross-community bridge._
- **Why does `ScriptEditorActivity` connect `ScriptEditorActivity` to `NodeApi`, `android.widget.TextView`, `MainActivity`, `ScriptsView`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **What connects `install.sh script`, `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.` to the rest of the system?**
  _8 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.07256894049346879 - nodes in this community are weakly interconnected._
- **Should `ScriptsView` be split into smaller, more focused modules?**
  _Cohesion score 0.14814814814814814 - nodes in this community are weakly interconnected._
- **Should `ReceiverDB` be split into smaller, more focused modules?**
  _Cohesion score 0.08773784355179703 - nodes in this community are weakly interconnected._