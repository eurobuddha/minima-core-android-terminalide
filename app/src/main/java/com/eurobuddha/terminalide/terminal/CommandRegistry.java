package com.eurobuddha.terminalide.terminal;

import java.util.ArrayList;
import java.util.List;

/**
 * The node's command registry (minimaCore fork v1.1.2.3, CommandRunner.ALL_COMMANDS)
 * with param hints for autocomplete. {name, "param: param: ..."} — params are hints,
 * not validation; the node is the authority.
 */
public class CommandRegistry {

    public static final String[][] COMMANDS = {
        {"help", "command:"},
        {"status", "clean:"},
        {"block", ""},
        {"balance", "address: tokenid: confirmations:"},
        {"coins", "relevant: sendable: coinid: amount: address: tokenid: depth: order:"},
        {"tokens", "tokenid: action: data:"},
        {"tokencreate", "name: amount: decimals: script: state: signtoken: webvalidate: burn:"},
        {"tokenvalidate", "tokenid:"},
        {"createtokenfrom", "fromaddress: name: amount: decimals: script: state: burn:"},
        {"getaddress", ""},
        {"newaddress", ""},
        {"checkaddress", "address:"},
        {"keys", "action: publickey:"},
        {"vault", "action: seed: phrase:"},
        {"send", "address: amount: tokenid: state: split: burn: mine: password:"},
        {"sendpoll", "action: uid: address: amount: tokenid: burn:"},
        {"sendnosign", "address: amount: tokenid: file:"},
        {"sendsign", "file: password:"},
        {"sendpost", "file:"},
        {"sendview", "file:"},
        {"sendfrom", "fromaddress: address: amount: script: privatekey: keyuses: tokenid: state: split: burn:"},
        {"createfrom", "fromaddress: address: amount: tokenid: script: burn:"},
        {"signfrom", "data: privatekey:"},
        {"postfrom", "data: mine: mmr:"},
        {"constructfrom", ""},
        {"rawfrom", ""},
        {"rawtxnfrom", ""},
        {"multisig", "action: id: amount: publickeys: required: address: file: password:"},
        {"multisigread", ""},
        {"burn", "amount:"},
        {"consolidate", "tokenid: coinage: maxcoins: maxsigs: burn: dryrun: password:"},
        {"consolidatefrom", "fromaddress: tokenid: coinage: maxcoins:"},
        {"coinnotify", "action: address:"},
        {"cointrack", "enable: coinid:"},
        {"coincheck", "data:"},
        {"coinimport", "data: track:"},
        {"coinexport", "coinid:"},
        {"txncreate", "id:"},
        {"txndelete", "id:"},
        {"txnlist", "id:"},
        {"txnview", "id:"},
        {"txnclear", ""},
        {"txninput", "id: coinid: coindata: floating: address: amount: tokenid: scriptmmr:"},
        {"txnoutput", "id: amount: address: tokenid: storestate:"},
        {"txnstate", "id: port: value:"},
        {"txnscript", "id: scripts: auto:"},
        {"txnsign", "id: publickey: txnpostauto: txnpostburn: txnpostmine: txndelete: password:"},
        {"txnbasics", "id:"},
        {"txncheck", "id:"},
        {"txnpost", "id: auto: burn: mine: txndelete:"},
        {"txnexport", "id: file:"},
        {"txnimport", "id: file: data:"},
        {"txnauto", "id: amount: address: tokenid: sign: burn: mmrscript:"},
        {"txnaddamount", "id: amount:"},
        {"txnlock", "id:"},
        {"txncoinlock", "id:"},
        {"txnmmr", "id:"},
        {"txnmine", "id: data:"},
        {"txnminepost", "id:"},
        {"runscript", "script: state: prevstate: globals: signatures: extrascripts:"},
        {"newscript", "script: trackall: clean:"},
        {"removescript", "address:"},
        {"scripts", "address:"},
        {"sign", "data: publickey:"},
        {"verify", "data: publickey: signature:"},
        {"hash", "data: type:"},
        {"hashtest", ""},
        {"random", "size: type:"},
        {"seedrandom", "seed: modifier:"},
        {"maths", "calculate: logs:"},
        {"convert", "from: to: data:"},
        {"mmrcreate", "nodes:"},
        {"mmrproof", "data: proof: root:"},
        {"printmmr", ""},
        {"megammr", "action: file:"},
        {"megammrsync", "action: host: phrase: anyseed: anyphrase: keys: keyuses: file:"},
        {"txpow", "txpowid: block: address: onlyaddress: max:"},
        {"history", "action: max: offset: relevant: startblock: endblock:"},
        {"scanchain", "depth:"},
        {"archive", "action: host: file: phrase: anyphrase: keys: keyuses: address: statecheck: logs:"},
        {"restoresync", "file: password: host: keyuses:"},
        {"restore", "file: password:"},
        {"backup", "file: password: auto: confirm:"},
        {"decryptbackup", "file: password:"},
        {"reset", "action: file: archivefile: phrase: keys: keyuses:"},
        {"network", "action:"},
        {"connect", "host:"},
        {"disconnect", "uid:"},
        {"peers", "action: peerslist: max:"},
        {"p2pstate", ""},
        {"ping", ""},
        {"message", "data: uid:"},
        {"webhooks", "action: hook: filter:"},
        {"rpc", "enable: password: ssl:"},
        {"slavenode", "enable: host:"},
        {"nodecount", ""},
        {"automine", "enable:"},
        {"mempool", ""},
        {"printtree", "depth: cascade:"},
        {"trace", "enable: filter:"},
        {"debugflag", "flag:"},
        {"logs", "scripts: mining:"},
        {"magic", ""},
        {"timemilli", ""},
        {"systemcheck", ""},
        {"healthcheck", ""},
        {"test", ""},
        {"benchmark", ""},
        {"tutorial", ""},
        {"whitepaper", "file:"},
        {"incentivecash", "uid:"},
        {"mysql", "host: database: user: password: action:"},
        {"mysqlcoins", "host: database: user: password: action:"},
        {"quit", "compact:"},
    };

    public static List<String> commandNames() {
        List<String> names = new ArrayList<>();
        for (String[] c : COMMANDS) names.add(c[0]);
        return names;
    }

    /** Param hint line for a command name, or null. */
    public static String paramsFor(String command) {
        for (String[] c : COMMANDS) {
            if (c[0].equals(command)) return c[1].isEmpty() ? null : c[0] + "  " + c[1];
        }
        return null;
    }

    /**
     * Known node-killers over the native Binder IPC (reply >1MB kills this app process
     * uncatchably and can crash the node). Returns a warning string, or null if safe.
     */
    public static String dangerWarning(String fullCommand) {
        String cmd = fullCommand.trim();
        String name = cmd.split("[ ;]")[0];
        if (name.equals("coins")) {
            boolean bounded = cmd.contains("depth:") || cmd.contains("coinid:") || cmd.contains("relevant:true")
                    || cmd.contains("sendable:true");
            if (!bounded) {
                return "Unbounded 'coins' on a busy address can build a giant reply that crashes "
                        + "the node AND this app (Binder ~1MB limit is uncatchable). Add depth: "
                        + "(start small, e.g. depth:4) or relevant:true.";
            }
        }
        if (name.equals("history")) {
            int max = -1;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("max:(\\d+)").matcher(cmd);
            if (m.find()) max = Integer.parseInt(m.group(1));
            if (max < 0 || max > 25) {
                return "'history' returns FULL txpow bodies (~14KB each; default max ~100) and can "
                        + "exceed the IPC limits — big replies crash the node/app. Use max:8 or "
                        + "less and page with offset:.";
            }
        }
        if (name.equals("printtree") && !cmd.contains("depth:")) {
            return "'printtree' without depth: can return a huge tree. Add depth: (e.g. depth:10).";
        }
        return null;
    }
}
