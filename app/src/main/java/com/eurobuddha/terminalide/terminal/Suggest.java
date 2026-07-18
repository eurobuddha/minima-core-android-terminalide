package com.eurobuddha.terminalide.terminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-depth autocomplete engine for the terminal input.
 *
 * Given the current input text, produces suggestion chips for whichever token the
 * user is typing:
 *   ""                 -> all commands
 *   "meg"              -> commands starting with meg
 *   "megammr "         -> that command's params (action: file:)
 *   "megammr ac"       -> params starting with ac
 *   "megammr action:"  -> the allowed VALUES of action (info export import)
 *   "megammr action:e" -> values starting with e
 *   "... action:export " -> the remaining unused params
 * Works at any depth in the line, and inside multi-command ; chains (the segment
 * after the last ';' is completed independently).
 */
public class Suggest {

    /**
     * One suggestion chip. Tapping splices `insert` into the input in place of the token
     * being completed: newText = text[0..tokenStart) + insert + text[caret..end).
     */
    public static class Item {
        public final String label;
        public final int tokenStart;   // where the completed token begins
        public final String insert;    // replacement for text[tokenStart..caret)
        Item(String label, int tokenStart, String insert) {
            this.label = label;
            this.tokenStart = tokenStart;
            this.insert = insert;
        }
    }

    public static class Result {
        public final List<Item> items = new ArrayList<>();
        public String paramHint = null;   // full usage line for the active command, or null
    }

    public static Result suggest(String text) {
        return suggest(text, text.length());
    }

    /**
     * Complete the token ending at `caret` (not the end of the string) so chips work
     * even when the user edits mid-line.
     */
    public static Result suggest(String text, int caret) {
        Result r = new Result();
        if (caret < 0 || caret > text.length()) caret = text.length();
        String upto = text.substring(0, caret);

        // Complete only the segment after the last ';' before the caret (multi-command chains).
        int segStart = upto.lastIndexOf(';') + 1;
        String seg = upto.substring(segStart);
        int lead = 0;
        while (lead < seg.length() && seg.charAt(lead) == ' ') lead++;
        String body = seg.substring(lead);

        int firstSpace = body.indexOf(' ');
        if (firstSpace < 0) {
            // Typing the command name itself. Token starts at segStart+lead.
            int tokStart = segStart + lead;
            for (CommandRegistry.Cmd c : CommandRegistry.all()) {
                if (c.name.startsWith(body) && !c.name.equals(body)) {
                    r.items.add(new Item(c.name, tokStart, c.name + " "));
                }
            }
            // Exact command fully typed (no trailing space yet): offer its params too.
            CommandRegistry.Cmd exact = CommandRegistry.get(body);
            if (exact != null) {
                r.paramHint = exact.usage();
                for (CommandRegistry.Param p : exact.params) {
                    // Append after the command word (caret) rather than replacing it.
                    r.items.add(new Item(p.name + ":", caret, " " + p.name + ":"));
                }
            }
            return r;
        }

        String cmdName = body.substring(0, firstSpace);
        CommandRegistry.Cmd cmd = CommandRegistry.get(cmdName);
        if (cmd == null) return r;
        r.paramHint = cmd.usage();

        // The token in progress = after the last space before the caret.
        int lastSpace = seg.lastIndexOf(' ');
        String token = seg.substring(lastSpace + 1);
        int tokStart = segStart + lastSpace + 1;

        int colon = token.indexOf(':');
        if (colon >= 0) {
            // Param value completion.
            String key = token.substring(0, colon);
            String partial = token.substring(colon + 1);
            for (String v : valuesFor(cmd, key)) {
                if (v.startsWith(partial) && !v.equals(partial)) {
                    r.items.add(new Item(v, tokStart, key + ":" + v + " "));
                }
            }
        } else {
            // Param name completion — skip params already present in this segment.
            for (CommandRegistry.Param p : cmd.params) {
                if (!p.name.startsWith(token)) continue;
                if (seg.contains(" " + p.name + ":")) continue;
                r.items.add(new Item(p.name + ":", tokStart, p.name + ":"));
            }
        }
        return r;
    }

    /** Applies a chip against the CURRENT text + caret (re-derived at click time). */
    public static String apply(String text, int caret, Item item) {
        if (caret < 0 || caret > text.length()) caret = text.length();
        int start = Math.min(item.tokenStart, caret);
        return text.substring(0, start) + item.insert + text.substring(caret);
    }

    /** New caret position after applying `item`. */
    public static int applyCaret(String text, int caret, Item item) {
        if (caret < 0 || caret > text.length()) caret = text.length();
        int start = Math.min(item.tokenStart, caret);
        return start + item.insert.length();
    }

    private static List<String> valuesFor(CommandRegistry.Cmd cmd, String key) {
        // help command:<x> completes with every command name.
        if (cmd.name.equals("help") && key.equals("command")) {
            List<String> names = new ArrayList<>();
            for (CommandRegistry.Cmd c : CommandRegistry.all()) names.add(c.name);
            return names;
        }
        for (CommandRegistry.Param p : cmd.params) {
            if (p.name.equals(key)) return p.values;
        }
        return new ArrayList<>();
    }
}
