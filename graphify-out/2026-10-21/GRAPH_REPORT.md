# Graph Report - apks/terminalide  (2026-09-04)

## Corpus Check
- 38 files · ~48,361 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 382 nodes · 930 edges · 21 communities (17 shown, 4 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 74 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `67992286`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TerminalView
- ScriptsView
- ScriptDB
- ScriptEditorActivity
- MainActivity
- TxnView
- Cmd
- android.content.Context
- NodeApi
- ScriptEditorActivity.java
- gradlew
- KissVm.java
- Terminal IDE
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- gen_help.py
- Offline help for every node command
- install.sh
- pre-commit

## God Nodes (most connected - your core abstractions)
1. `TerminalView` - 46 edges
2. `TxnView` - 35 edges
3. `ScriptEditorActivity` - 28 edges
4. `NodeApi` - 26 edges
5. `MainActivity` - 21 edges
6. `ScriptsView` - 21 edges
7. `ScriptDB` - 19 edges
8. `ReceiverDB` - 19 edges
9. `Cb` - 18 edges
10. `BaseView` - 16 edges

## Surprising Connections (you probably didn't know these)
- `TerminalView` --inherits--> `BaseView`  [EXTRACTED]
  apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/terminal/TerminalView.java → apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TerminalAdapter` --references--> `BaseView`  [EXTRACTED]
  apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/TerminalAdapter.java → apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `TxnView` --inherits--> `BaseView`  [EXTRACTED]
  apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/txn/TxnView.java → apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/BaseView.java
- `LogsView` --references--> `ReceiverDB`  [EXTRACTED]
  apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/LogsView.java → apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/receiver/ReceiverDB.java
- `MainActivity` --references--> `NodeApi`  [EXTRACTED]
  apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/MainActivity.java → apks/terminalide/app/src/main/java/com/eurobuddha/terminalide/NodeApi.java

## Import Cycles
- None detected.

## Communities (21 total, 4 thin omitted)

### Community 0 - "TerminalView"
Cohesion: 0.13
Nodes (3): Override, TextView, TerminalView

### Community 1 - "ScriptsView"
Cohesion: 0.09
Nodes (14): android.app.Activity, android.view.View, android.widget.ListView, android.widget.TextView, BaseView, Override, ScriptsView, Templates (+6 more)

### Community 2 - "ScriptDB"
Cohesion: 0.09
Nodes (14): android.content.BroadcastReceiver, android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Override, Script, ScriptDB, Override (+6 more)

### Community 3 - "ScriptEditorActivity"
Cohesion: 0.18
Nodes (5): JSONObject, Override, TextView, ScriptEditorActivity, HorizontalScrollView

### Community 4 - "MainActivity"
Cohesion: 0.07
Nodes (19): android.content.Intent, android.net.Uri, android.os.Bundle, android.view.Menu, android.view.MenuItem, android.view.ViewGroup, androidx.activity.result.ActivityResultLauncher, androidx.appcompat.app.AppCompatActivity (+11 more)

### Community 5 - "TxnView"
Cohesion: 0.20
Nodes (4): Cb, EditText, TxnView, OnClickListener

### Community 6 - "Cmd"
Cohesion: 0.18
Nodes (6): Cmd, CommandRegistry, Param, Item, Result, Suggest

### Community 7 - "android.content.Context"
Cohesion: 0.10
Nodes (15): android.content.Context, android.util.AttributeSet, android.view.MotionEvent, android.widget.ScrollView, androidx.appcompat.widget.AppCompatEditText, CaretEditText, Override, OnCaretMoved (+7 more)

### Community 8 - "NodeApi"
Cohesion: 0.22
Nodes (5): android.os.Handler, NodeApi, PairingListener, MinimaAPI, org.minimarex.minimaapi.MinimaAPI

### Community 9 - "ScriptEditorActivity.java"
Cohesion: 0.13
Nodes (13): android.text.Editable, android.text.SpannableStringBuilder, android.text.TextWatcher, android.widget.EditText, android.widget.LinearLayout, Override, KissHighlighter, Override (+5 more)

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

- **Why does `TerminalView` connect `TerminalView` to `ScriptsView`, `ScriptDB`, `MainActivity`, `Cmd`, `android.content.Context`, `NodeApi`, `ScriptEditorActivity.java`?**
  _High betweenness centrality (0.196) - this node is a cross-community bridge._
- **Why does `NodeApi` connect `NodeApi` to `TerminalView`, `ScriptsView`, `ScriptEditorActivity`, `MainActivity`, `TxnView`, `android.content.Context`, `ScriptEditorActivity.java`?**
  _High betweenness centrality (0.162) - this node is a cross-community bridge._
- **Why does `ScriptEditorActivity` connect `ScriptEditorActivity` to `ScriptsView`, `ScriptDB`, `MainActivity`, `NodeApi`, `ScriptEditorActivity.java`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **What connects `install.sh script`, `KissVm`, `RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.` to the rest of the system?**
  _8 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TerminalView` be split into smaller, more focused modules?**
  _Cohesion score 0.12701612903225806 - nodes in this community are weakly interconnected._
- **Should `ScriptsView` be split into smaller, more focused modules?**
  _Cohesion score 0.08985200845665962 - nodes in this community are weakly interconnected._
- **Should `ScriptDB` be split into smaller, more focused modules?**
  _Cohesion score 0.08859357696567 - nodes in this community are weakly interconnected._