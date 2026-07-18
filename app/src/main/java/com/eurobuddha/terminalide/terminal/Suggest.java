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

    /** One suggestion chip: label shown; tapping replaces the whole input with newText. */
    public static class Item {
        public final String label;
        public final String newText;
        Item(String label, String newText) {
            this.label = label;
            this.newText = newText;
        }
    }

    public static class Result {
        public final List<Item> items = new ArrayList<>();
        public String paramHint = null;   // full usage line for the active command, or null
    }

    public static Result suggest(String text) {
        Result r = new Result();

        // Complete only the segment after the last ';' (multi-command chains).
        int segStart = text.lastIndexOf(';') + 1;
        String seg = text.substring(segStart);
        int lead = 0;
        while (lead < seg.length() && seg.charAt(lead) == ' ') lead++;
        String body = seg.substring(lead);
        String beforeBody = text.substring(0, segStart + lead);

        int firstSpace = body.indexOf(' ');
        if (firstSpace < 0) {
            // Typing the command name itself.
            for (CommandRegistry.Cmd c : CommandRegistry.all()) {
                if (c.name.startsWith(body) && !c.name.equals(body)) {
                    r.items.add(new Item(c.name, beforeBody + c.name + " "));
                }
            }
            // Exact command fully typed (no trailing space yet): offer its params as well.
            CommandRegistry.Cmd exact = CommandRegistry.get(body);
            if (exact != null) {
                r.paramHint = exact.usage();
                for (CommandRegistry.Param p : exact.params) {
                    r.items.add(new Item(p.name + ":", text + " " + p.name + ":"));
                }
            }
            return r;
        }

        String cmdName = body.substring(0, firstSpace);
        CommandRegistry.Cmd cmd = CommandRegistry.get(cmdName);
        if (cmd == null) return r;
        r.paramHint = cmd.usage();

        // The token in progress = after the last space of the segment.
        int lastSpace = seg.lastIndexOf(' ');
        String token = seg.substring(lastSpace + 1);
        String beforeToken = text.substring(0, segStart + lastSpace + 1);

        int colon = token.indexOf(':');
        if (colon >= 0) {
            // Param value completion.
            String key = token.substring(0, colon);
            String partial = token.substring(colon + 1);
            for (String v : valuesFor(cmd, key)) {
                if (v.startsWith(partial) && !v.equals(partial)) {
                    r.items.add(new Item(v, beforeToken + key + ":" + v + " "));
                }
            }
        } else {
            // Param name completion — skip params already present in this segment.
            for (CommandRegistry.Param p : cmd.params) {
                if (!p.name.startsWith(token)) continue;
                if (seg.contains(" " + p.name + ":")) continue;
                r.items.add(new Item(p.name + ":", beforeToken + p.name + ":"));
            }
        }
        return r;
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
