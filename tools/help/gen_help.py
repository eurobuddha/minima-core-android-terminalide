#!/usr/bin/env python3
"""
Build app/src/main/assets/help.json = the node's own help pages, completed.

The node's help (help command:x) is incomplete: many parameters the node accepts
(vault password:/confirm:/numkeys:, peers url:, sphincs signature:, ...) and several
action values (vault testphrase/resetkeys, network restart, archive importraw, ...)
are never mentioned, and ~20 commands have no full page at all. The app must not
mirror those gaps, so this script:

  1. loads node-help.json - the raw dump of every command from the node jar
     (name, brief help, fullhelp, valid params) made with Dump.java;
  2. checks the app's CommandRegistry param table against the node's valid params
     (the node REJECTS any param not in that list, so they must match exactly);
  3. applies SUPPLEMENT: hand-written sections for every undocumented param / action
     value, and full pages for commands that have none - all written from the node
     source (core/minima-core, see the README);
  4. regenerates the one-line brief from the complete param list;
  5. fails if any accepted param is still undocumented, then writes help.json.

Re-run after a node upgrade:
    javac -cp <minima.jar> Dump.java && java -cp <minima.jar>:. Dump > node-help.json
    python3 gen_help.py
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
APP = os.path.abspath(os.path.join(HERE, '..', '..'))
NODE_JSON = os.path.join(HERE, 'node-help.json')
REGISTRY = os.path.join(APP, 'app/src/main/java/com/eurobuddha/terminalide/terminal/CommandRegistry.java')
OUT = os.path.join(APP, 'app/src/main/assets/help.json')

# ----------------------------------------------------------------------------------
# SUPPLEMENT - everything the node's own help leaves out.
#
#   params:   {name: (required, "description")} - a section is added only if the page
#             has no "name:" header yet. Multi-line descriptions: one line per "\n".
#   actions:  {value: "description"} - appended to the action: block if absent.
#   extra:    {param: ["line", ...]} - lines appended to an existing param section.
#   page:     (intro, examples) - full page for a command that has no fullhelp.
#             Params come from `params`, required ones first.
#   examples: [..] - extra examples appended to an existing page.
#   copy_params_from: "cmd" - reuse that command's (completed) param sections.
#   brief:    override the description part of the one-line summary.
# ----------------------------------------------------------------------------------

KEYUSES = ("How many times this private key has already signed. Every signature must use a\n"
           "fresh leaf of the key tree, so increase this by one for each signature you make.")

SUPPLEMENT = {
    'quit': {'params': {
        'compact': (False, "true or false, default false. Compact the H2 databases while shutting down.\n"
                           "Slower to quit but reclaims disk space."),
    }},
    'status': {'params': {
        'debug': (False, "true or false, default false. Add debug detail: memory and per-file disk usage,\n"
                         "chain tree and cascade internals."),
        'complete': (False, "true or false, default false. Add the complete detail set: PoW mode (normal or low),\n"
                            "block weights and the size of every database file."),
    }},
    'coins': {'params': {
        'state': (False, "Only return coins whose state variables contain this value."),
        'simplestate': (False, "true or false, default false. Return each coin's state as a simple\n"
                               "{port:value} object instead of the full typed list."),
        'totalamount': (False, "Return just enough of the matching coins to cover this amount, chosen with the\n"
                               "same coin selection as send. In token units when tokenid is set."),
    }},
    'txpow': {'params': {
        'action': (False, "info : Show the TxPoW database details - stored days, SQL size and the\n"
                          "       on-chain DB size with its first and last block."),
        'inblock': (False, "A block number, or a block's txpowid (0x..). List the TxPoW IDs included in\n"
                           "that block, from the on-chain DB."),
    }, 'examples': ["txpow action:info", "txpow inblock:12345", "txpow inblock:0x000.."]},
    'network': {'actions': {
        'restart': "Restart the networking layer.",
        'loggingon': "Turn on full network logging (NIO and P2P).",
        'loggingoff': "Turn off full network logging.",
    }, 'examples': ["network action:restart", "network action:loggingon", "network action:loggingoff"]},
    'help': {'params': {
        'command': (False, "The command to show the full help page for."),
    }, 'page': ("Show help. With no parameters, list every command with a one-line summary.\n"
                "\n"
                "In a summary [] marks a required parameter and () an optional one.\n"
                "\n"
                "Chain multiple commands with ;",
                ["help", "help command:send", "status;balance"])},
    'send': {'params': {
        'action': (False, "Used internally when a queued sendpoll command is replayed as send.\n"
                          "Not needed for a normal send."),
        'uid': (False, "Used internally by sendpoll (the queued command's id). Not needed for a normal send."),
    }},
    'tokencreate': {'params': {
        'uselimits': (False, "true or false, default true. Enforce the safety limits: at most 16 decimals and a\n"
                             "total supply of at most 1 trillion. Set false to bypass them."),
    }},
    'debugflag': {'params': {
        'activate': (False, "true or false, default false. Turn the DEBUG flag on or off."),
        'var': (False, "Set the debug variable read by test code."),
    }, 'page': ("Developer command. Set the node's DEBUG flag and an optional debug variable used by\n"
                "test code.",
                ["debugflag activate:true", "debugflag activate:true var:mytest"])},
    'webhooks': {'params': {
        'enable': (False, "true or false. Use with action:errorlogs to turn logging of failed webhook calls\n"
                          "on or off."),
    }, 'actions': {
        'errorlogs': "Turn logging of webhook delivery errors on or off. Use with enable:true|false.",
    }, 'examples': ["webhooks action:errorlogs enable:true"]},
    'peers': {'params': {
        'max': (False, "Maximum number of peers to return with action:list or action:forcecheck.\n"
                       "Default 1000."),
        'file': (False, "Use with action:publish. The file to write the peers list to. Default peerslist.txt"),
        'url': (False, "Use with action:fetch. The URL to download a peers list from (as written by\n"
                       "action:publish). The peers in it are then added."),
    }, 'actions': {
        'forcecheck': "Force an immediate full check of all peers.",
        'publish': "Write your top 20 peers to a file so they can be shared.",
        'fetch': "Download a peers list from a URL and add those peers.",
    }, 'examples': ["peers action:list max:10", "peers action:forcecheck",
                    "peers action:publish file:peerslist.txt",
                    "peers action:fetch url:https://example.com/peerslist.txt"]},
    'sendpoll': {'copy_params_from': 'send', 'actions': {
        'add': "Queue a send command. The default.",
    }},
    'sendnosign': {'params': {
        'dryrun': (False, "true or false. Accepted for parity with send but has no effect here -\n"
                          "sendnosign never posts the transaction."),
    }},
    'sendfrom': {'params': {
        'fromaddress': (True, "The address to spend from (Mx.. or 0x..). Its coins are the inputs."),
        'address': (True, "The address to send to."),
        'amount': (True, "The amount to send."),
        'tokenid': (False, "The token to send. Default 0x00 (Minima)."),
        'script': (True, "The full KISS VM script of fromaddress, e.g. RETURN SIGNEDBY(0x..)"),
        'privatekey': (True, "The 0x private key seed that signs for fromaddress."),
        'keyuses': (True, KEYUSES),
        'mine': (False, "true or false, default true. Mine the transaction immediately."),
        'burn': (False, "The amount of Minima to burn with this transaction."),
        'state': (False, "JSON object of state variables to add to the output: {port:value,..}"),
        'split': (False, "Split the sent amount into this many equal output coins. Default 1."),
    }, 'page': ("Send Minima or tokens from one specific address, signing with a private key you supply.\n"
                "The coins at fromaddress are selected, and the transaction is built, signed and posted.\n"
                "\n"
                "Use this for addresses whose script and key you hold outside the node wallet.",
                ['sendfrom fromaddress:Mx.. address:Mx.. amount:10 script:"RETURN SIGNEDBY(0x..)" '
                 'privatekey:0x.. keyuses:5',
                 'sendfrom fromaddress:0x.. address:Mx.. amount:1 tokenid:0x.. '
                 'script:"RETURN SIGNEDBY(0x..)" privatekey:0x.. keyuses:6 mine:false'])},
    'createfrom': {'params': {
        'fromaddress': (True, "The address to spend from (Mx.. or 0x..). Its coins are the inputs."),
        'address': (True, "The address to send to."),
        'amount': (True, "The amount to send."),
        'tokenid': (False, "The token to send. Default 0x00 (Minima)."),
        'script': (True, "The full KISS VM script of fromaddress, e.g. RETURN SIGNEDBY(0x..)"),
        'burn': (False, "The amount of Minima to burn with this transaction."),
    }, 'page': ("Create an unsigned transaction that spends the coins at a specific address.\n"
                "\n"
                "The returned data is signed with signfrom and posted with postfrom.",
                ['createfrom fromaddress:Mx.. address:Mx.. amount:10 script:"RETURN SIGNEDBY(0x..)"'])},
    'signfrom': {'params': {
        'data': (True, "The hex transaction data returned by createfrom."),
        'id': (False, "Give the signed transaction this id."),
        'privatekey': (True, "The 0x private key seed to sign with."),
        'keyuses': (True, KEYUSES),
        'post': (False, "true or false, default false. Post the transaction immediately after signing."),
    }, 'page': ("Sign a transaction created with createfrom or constructfrom, using a private key you supply.\n"
                "\n"
                "Returns the signed transaction data for postfrom.",
                ['signfrom data:0x.. privatekey:0x.. keyuses:5',
                 'signfrom data:0x.. privatekey:0x.. keyuses:5 post:true'])},
    'postfrom': {'params': {
        'data': (True, "The signed hex transaction data returned by signfrom."),
        'mine': (False, "true or false, default false. Mine the transaction immediately."),
        'mmr': (False, "true or false, default false. Recalculate the MMR proofs of the inputs before\n"
                       "posting - use it if the transaction was created a while ago."),
    }, 'page': ("Post a transaction signed with signfrom.",
                ['postfrom data:0x..', 'postfrom data:0x.. mine:true mmr:true'])},
    'constructfrom': {'params': {
        'coinlist': (True, "Comma separated list of the coin ids to spend: coinid,coinid,.."),
        'script': (True, "The KISS VM script of the address the coins belong to."),
        'toaddress': (True, "The address to send to."),
        'toamount': (True, "The amount to send to toaddress."),
        'changeaddress': (True, "The address the change goes to."),
        'changeamount': (True, "The amount to return as change."),
        'tokenid': (False, "The token of the coins. Default 0x00 (Minima)."),
    }, 'page': ("Build an unsigned transaction from an explicit list of coins. Every listed coin is an\n"
                "input; one output goes to toaddress and one to changeaddress.\n"
                "\n"
                "Sign the result with signfrom and post it with postfrom.",
                ['constructfrom coinlist:0x..,0x.. script:"RETURN SIGNEDBY(0x..)" toaddress:Mx.. '
                 'toamount:10 changeaddress:Mx.. changeamount:2.5'])},
    'consolidatefrom': {'params': {
        'fromaddress': (True, "The address whose coins are consolidated (Mx.. or 0x..)."),
        'tokenid': (False, "The token to consolidate. Default 0x00 (Minima)."),
        'script': (True, "The full KISS VM script of fromaddress, e.g. RETURN SIGNEDBY(0x..)"),
        'privatekey': (True, "The 0x private key seed that signs for fromaddress."),
        'keyuses': (True, KEYUSES),
        'mine': (False, "true or false, default true. Mine the transaction immediately."),
        'burn': (False, "The amount of Minima to burn with this transaction."),
        'maxcoins': (False, "Maximum number of coins to consolidate in one transaction. Default 50."),
    }, 'page': ("Consolidate the coins held at a specific address into one coin, signing with a private\n"
                "key you supply.",
                ['consolidatefrom fromaddress:Mx.. script:"RETURN SIGNEDBY(0x..)" privatekey:0x.. keyuses:7',
                 'consolidatefrom fromaddress:Mx.. tokenid:0x.. script:"RETURN SIGNEDBY(0x..)" '
                 'privatekey:0x.. keyuses:8 maxcoins:20'])},
    'createtokenfrom': {'params': {
        'fromaddress': (True, "The address that funds the token creation (Mx.. or 0x..)."),
        'name': (True, "The token name - a plain string or a JSON object of token details."),
        'amount': (True, "The total supply of the token."),
        'privatekey': (True, "The 0x private key seed that signs for fromaddress."),
        'keyuses': (True, KEYUSES),
        'script': (True, "The full KISS VM script of fromaddress, e.g. RETURN SIGNEDBY(0x..)"),
        'decimals': (False, "The number of decimal places. Default 8."),
        'mine': (False, "true or false, default true. Mine the transaction immediately."),
    }, 'page': ("Create a token funded from a specific address, signing with a private key you supply.",
                ['createtokenfrom fromaddress:Mx.. name:mytoken amount:1000 script:"RETURN SIGNEDBY(0x..)" '
                 'privatekey:0x.. keyuses:9',
                 'createtokenfrom fromaddress:Mx.. name:{"name":"mytoken","url":"https://.."} amount:1000 '
                 'decimals:0 script:"RETURN SIGNEDBY(0x..)" privatekey:0x.. keyuses:10'])},
    'archive': {'actions': {
        'importold': "Import an archive backup made by an older node version (H2 format). Use with file:",
        'importraw': "Import a raw archive export (.raw.dat). Much faster than the H2 gzip import. Use with file:",
        'inspectraw': "Inspect a raw archive export - its cascade and block range - without importing it. Use with file:",
    }, 'examples': ["archive action:importraw file:archivebackup.raw.dat",
                    "archive action:inspectraw file:archivebackup.raw.dat"]},
    'history': {'params': {
        'startmilli': (False, "Use with action:size relevant:false. Count every TxPoW seen since this time\n"
                              "(milliseconds since the epoch). Default is 24 hours ago."),
    }, 'examples': ["history action:size relevant:false startmilli:1700000000000"]},
    'systemcheck': {'params': {
        'action': (False, "list : Show the message queue sizes of the main processors. The default.\n"
                          "details : Log the full details of one processor. Use with processor:"),
        'processor': (False, "Use with action:details. One of main, txpowprocessor, txpowminer, p2pmanager,\n"
                             "niomanager, notifymanager, senpollmanager, timerprocessor."),
    }, 'page': ("Developer command. Show the state of the node's internal message processors, or log\n"
                "the full details of one of them.",
                ["systemcheck", "systemcheck action:details processor:p2pmanager"])},
    'scanchain': {'params': {
        'depth': (False, "How many blocks to scan back. Default 16."),
        'offset': (False, "How many blocks below the tip to start from. Default 0."),
    }, 'page': ("Scan back through the chain from the tip and list every transaction found.",
                ["scanchain", "scanchain depth:100", "scanchain depth:50 offset:100"])},
    'multisig': {'params': {
        'tokenid': (False, "Use with action:create. The token of the multisig coin. Default 0x00 (Minima)."),
        'mine': (False, "true or false. Accepted for parity but not used - a multisig spend is always\n"
                        "posted with mine:true."),
    }},
    'multisigread': {'params': {
        'root': (False, "Accepted for parity with multisig. Not used by multisigread."),
        'required': (False, "Accepted for parity with multisig. Not used by multisigread."),
        'publickeys': (False, "Accepted for parity with multisig. Not used by multisigread."),
        'tokenid': (False, "Accepted for parity with multisig. Not used by multisigread."),
        'password': (False, "Accepted for parity with multisig. Not used by multisigread."),
    }},
    'checkaddress': {'params': {
        'address': (True, "The address to check, Mx.. or 0x.. A 0x address must be 66 characters long."),
    }, 'page': ("Check that an address is valid and show its 0x and Mx forms, and whether it is\n"
                "one of your own wallet addresses.",
                ["checkaddress address:Mx..", "checkaddress address:0x.."])},
    'sphincs': {'params': {
        'privatekey': (False, "The SPHINCS+ private key (0x..) to sign with, or to send from with action:transaction."),
        'publickey': (False, "The SPHINCS+ public key (0x..) to verify against."),
        'signature': (False, "Use with action:verify. The signature (0x..) to check, if not read from file:"),
        'amount': (False, "Use with action:transaction. The amount to send."),
        'address': (False, "Use with action:transaction. The address to send to."),
        'tokenid': (False, "Use with action:transaction. The token to send."),
        'mine': (False, "Use with action:transaction. true or false, default false - mine the\n"
                        "transaction immediately."),
    }, 'actions': {
        'test': "run a built-in SPHINCS+ sign and verify self test.",
    }, 'examples': ["sphincs action:test"]},
    'ping': {'params': {
        'host': (True, "The host:port of the Minima node to ping."),
    }},
    'mysql': {'params': {
        'txpowid': (False, "Use with action:findtxpow. The TxPoW id to look up."),
        'block': (False, "Use with action:findtxpow. Look up the block at this block number instead of a TxPoW id."),
        'statecheck': (False, "Use with action:addresscheck. Only include coins whose state variables contain this value."),
        'maxexport': (False, "Use with action:rawexport. Maximum number of blocks to export."),
        'startfix': (False, "Use with action:fixmissing. The block number to start checking from."),
        'endfix': (False, "Accepted but currently unused."),
    }, 'actions': {
        'size': "Show the number of blocks stored in the MySQL db.",
        'reset': "Import a raw .dat archive file (file:) into the MySQL db, then re-sync the node from it.",
        'fixmissing': "Scan a raw archive file (file:) and insert any blocks missing from the MySQL db. Use startfix: to skip ahead.",
    }, 'examples': ["mysql action:size", "mysql action:findtxpow block:12345",
                    "mysql action:addresscheck address:MxG08.. statecheck:0xFFEEDD..",
                    "mysql action:fixmissing file:archivebackup.raw.dat startfix:100000"]},
    'mysqlcoins': {'params': {
        'query': (False, "Use with action:search. A complete SQL query to run against the coins table."),
        'address': (False, "Use with action:search. Return the coins at this address, or with it in their state."),
        'spent': (False, "true or false. Use with action:search address:. Only spent, or only unspent, coins."),
        'limit': (False, "Use with action:search address:. Maximum number of rows to return."),
        'hidetoken': (False, "true or false, default false. Use with action:search. Omit the token details\n"
                             "from each coin returned."),
        'maxblocks': (False, "Use with action:update. Maximum number of blocks to process. Default unlimited."),
        'enable': (False, "true or false. Use with action:autobackup."),
    }, 'actions': {
        'autobackup': "Automatically keep the coins db updated. Use with enable:true|false.",
    }, 'examples': ["mysqlcoins action:autobackup enable:true",
                    "mysqlcoins action:search address:Mx87DE.. spent:false limit:10 hidetoken:true"]},
    'slavenode': {'params': {
        'enable': (False, "true or false. Turn slave node mode on or off. A restart is required."),
        'host': (False, "Use with enable:true. The host:port of the master node to connect to."),
    }},
    'jnitest': {'params': {
        'maxattempts': (False, "Number of hashes to attempt. Default 1000."),
        'testnonce': (False, "The nonce to start hashing from. Default 12345.54321"),
        'targetdifficulty': (False, "The target difficulty (0x..) a hash must beat. Default is the minimum TxPoW work."),
    }},
    'benchmark': {'params': {
        'hashes': (False, "Number of hashes to run. Default 1000."),
        'testnonce': (False, "The nonce to start hashing from. Default 12345.54321"),
        'targetdifficulty': (False, "The target difficulty (0x..) a hash must beat. Default is the minimum TxPoW work."),
        'maxattempts': (False, "Accepted but unused - use hashes:"),
    }},
    'jniminingtest': {'params': {
        'maxattempts': (False, "Number of hashes to attempt. Default 1000000."),
        'testnonce': (False, "The nonce to start hashing from. Default 12345.54321"),
        'targetdifficulty': (False, "The target difficulty (0x..) a hash must beat. Default is the minimum TxPoW work."),
    }},
    'megammr': {'actions': {
        'integrity': "Check the integrity of a MegaMMR export file. Use with file:",
    }, 'examples': ["megammr action:integrity file:megammr.dat"]},
    'vault': {'params': {
        'password': (False, "Use with action:passwordlock or action:passwordunlock. The password that\n"
                            "encrypts / decrypts your private keys. It cannot contain ;"),
        'confirm': (False, "Use with action:passwordlock. Must match password: exactly or the lock is refused."),
        'numkeys': (False, "Use with action:testphrase. How many keys and addresses to derive from the\n"
                           "phrase. Default 4."),
        'keyuses': (False, "Use with action:resetkeys. The number of previous uses to assume for every key.\n"
                           "Default 100."),
    }, 'actions': {
        'testphrase': "Derive the seed and the first numkeys public keys / addresses from a phrase, to check it. Does not touch your wallet.",
        'resetkeys': "BE CAREFUL. Wipe the chain, TxPoW and wallet databases and regenerate every key from the phrase (with keyuses: previous uses).",
    }, 'examples': ['vault action:testphrase phrase:"SPRAY LAMP.." numkeys:8',
                    'vault action:resetkeys phrase:"SPRAY LAMP.." keyuses:1000']},
    'backup': {'params': {
        'confirm': (False, "Must match password: exactly or the backup is refused."),
        'debug': (False, "true or false, default false. Log the backup file path and progress."),
    }, 'examples': ["backup password:Longsecurepassword456 confirm:Longsecurepassword456 file:my-full-backup-01-Jan-22"]},
    'restore': {'params': {
        'shutdown': (False, "true or false, default true. Shut the node down once the restore completes, ready\n"
                            "for a clean restart. false keeps it running."),
    }, 'examples': ["restore file:my-full-backup-01-Jan-22.bak password:Longsecurepassword456 shutdown:false"]},
    'test': {'params': {
        'show': (False, "Accepted but unused."),
        'action': (False, "Accepted but unused."),
    }, 'page': ("Developer command. Closes and reopens the node's SQL databases.", ["test"]),
        'brief': "Developer command - close and reopen the SQL databases"},
    'keys': {'params': {
        'phrase': (False, "Use with action:genkey. The seed phrase to derive the key from. If omitted a new\n"
                          "random phrase is generated and returned."),
        'modifier': (False, "Use with action:list. Only show the keys with this modifier."),
        'keyuses': (False, "Use with action:createallkeys. The number of previous uses to set on every key."),
    }, 'actions': {
        'createallkeys': "Create all your default wallet keys now, in the background, with keyuses: previous uses.",
    }, 'examples': ['keys action:genkey phrase:"SPRAY LAMP.."', "keys action:createallkeys keyuses:1000"]},
    'txnlist': {'params': {
        'transactiononly': (False, "true or false, default false. Return only the transaction itself, without the\n"
                                   "witness and the other row details."),
    }},
    'txnsign': {'params': {
        'password': (False, "If your wallet is password locked, unlock it for this signing and relock it afterwards."),
        'privatekey': (False, "Use with publickey:custom. The 0x private key seed to sign with."),
        'keyuses': (False, "Use with publickey:custom. " + KEYUSES),
    }, 'extra': {
        'publickey': ["    auto : sign with your own wallet keys - for transactions with simple inputs.",
                      "    custom : sign with the privatekey: and keyuses: you supply."],
    }, 'examples': ["txnsign id:multisig publickey:custom privatekey:0x.. keyuses:5"]},
    'txnexport': {'params': {
        'showtxn': (False, "true or false, default false. Include the transaction JSON in the response as\n"
                           "well as the export data."),
    }},
    'txnauto': {'params': {
        'id': (True, "The id for the new transaction."),
        'amount': (True, "The amount to send."),
        'address': (True, "The address to send to."),
        'tokenid': (False, "The token to send. Default 0x00 (Minima)."),
        'sign': (False, "true or false, default false. Sign the transaction once it is built."),
        'burn': (False, "The amount of Minima to burn with this transaction."),
        'mmrscript': (False, "true or false, default true. Add the MMR proofs and scripts for the inputs."),
    }, 'page': ("Create a complete transaction automatically: adds inputs for the amount, an output to\n"
                "address, the change back to you and the MMR proofs, then optionally signs it.\n"
                "\n"
                "Post it with txnpost.",
                ["txnauto id:mytxn amount:10 address:Mx..",
                 "txnauto id:mytxn amount:10 address:Mx.. tokenid:0x.. sign:true"])},
    'txnaddamount': {'params': {
        'id': (True, "The transaction id."),
        'amount': (True, "The amount to add - in token units when tokenid is set."),
        'address': (False, "The address to send the amount to. Not needed with onlychange:true."),
        'onlychange': (False, "true or false, default false. Only add the inputs and the change output - you have\n"
                              "already added the destination output yourself."),
        'tokenid': (False, "The token to add. Default 0x00 (Minima)."),
        'fromaddress': (False, "Only select input coins from this address."),
        'burn': (False, "The amount of Minima to burn with this transaction."),
        'storestate': (False, "true or false, default true. Keep the state variables on the output coins."),
        'split': (False, "Split the amount sent to address into this many output coins. Default 1."),
    }, 'examples': ["txnaddamount id:mytxn amount:10 address:Mx..",
                    "txnaddamount id:mytxn amount:10 onlychange:true",
                    "txnaddamount id:mytxn amount:10 address:Mx.. tokenid:0x.. split:4"]},
    'txnmmr': {'params': {
        'id': (True, "The transaction id."),
    }, 'page': ("Add the MMR proofs to every input of a transaction that does not already have one.",
                ["txnmmr id:mytxn"])},
    'txnmine': {'params': {
        'id': (False, "The id of a stored transaction to mine."),
        'data': (False, "Exported transaction data (from txnexport) to mine instead of a stored id."),
    }, 'page': ("Mine a transaction without posting it, from a stored transaction (id:) or from exported\n"
                "transaction data (data:).\n"
                "\n"
                "Post the mined result later with txnminepost.",
                ["txnmine id:mytxn", "txnmine data:0x.."])},
    'txnminepost': {'params': {
        'data': (True, "The pre-mined transaction data returned by txnmine."),
    }, 'page': ("Post a transaction that was pre-mined with txnmine.", ["txnminepost data:0x.."])},
    'sign': {'params': {
        'publickey': (True, "The public key (0x..) of one of your wallet keys to sign with."),
    }},
    'automine': {'page': ("Developer command. Currently a no-op in this node build: the enable: shown in older\n"
                          "summaries is disabled and is rejected as an invalid parameter.", ["automine"]),
                 'brief': "Simulate traffic (disabled in this build - accepts no parameters)"},
    'mempool': {'page': ("Show the TxPoW in the mempool - the transactions and blocks the node holds that are not\n"
                         "yet in the cascade - with counts of transactions and blocks.", ["mempool"])},
    'whitepaper': {'page': ("Print the Minima white paper.", ["whitepaper"])},
    'tutorial': {'page': ("Show the complete grammar of the Minima KISS VM scripting language.", ["tutorial"])},
}

# ----------------------------------------------------------------------------------

# Same rule as ParamDocs.java: 'name:' alone, or followed only by a (...) tag such as
# '(optional)', '(boolean)' or "(optional) default is 'list'".
HEADER_RE = re.compile(r'^([a-z0-9]+(?:\|[a-z0-9]+)*):\s*(\(.*)?$')   # 'id|to|publickey:' documents three params


def is_optional(m):
    return bool(m.group(2)) and 'optional' in m.group(2)


def load_registry():
    """name -> [(param, [values])] straight from CommandRegistry.DATA."""
    src = open(REGISTRY).read()
    reg = {}
    for m in re.finditer(r'^\s*"([a-z0-9]+) ::(.*?)",\s*$', src, re.M):
        params = []
        for tok in m.group(2).split():
            mm = re.match(r'(\w+)(?:<(.*)>)?$', tok)
            params.append((mm.group(1), mm.group(2).split(',') if mm.group(2) else []))
        reg[m.group(1)] = params
    return reg


def fix_title(name, page):
    # The node writes "\txnlist" (a tab) instead of "\ntxnlist" for every txn* page.
    if page.startswith('\t') and name.startswith('t'):
        page = '\n' + name + page[len(name):]
    return page


def headers(lines):
    """{param: line index} for every 'param:' header line."""
    out = {}
    for i, l in enumerate(lines):
        m = HEADER_RE.match(l.strip())
        if m:
            for n in m.group(1).split('|'):
                out.setdefault(n, i)
    return out


def any_marked(lines):
    return any(HEADER_RE.match(l.strip()) and is_optional(HEADER_RE.match(l.strip())) for l in lines)


def section_end(lines, start):
    """Index just past the body of the section whose header is at `start`."""
    i = start + 1
    while i < len(lines) and lines[i].strip() != '' and not HEADER_RE.match(lines[i].strip()) \
            and not lines[i].startswith('Examples'):
        i += 1
    return i


def make_section(name, required, desc, marked):
    head = name + ':' + ('' if (required or not marked) else ' (optional)')
    return [head] + ['    ' + d for d in desc.split('\n')]


def add_action_values(lines, values, name):
    hs = headers(lines)
    if 'action' not in hs:
        return lines
    start = hs['action']
    end = section_end(lines, start)
    body = lines[start + 1:end]
    # separator style used by this page: "value : desc" or "value - desc"
    sep = ' : '
    present = set()
    for b in body:
        m = re.match(r'^\s+([a-z0-9/]+)\s*([:-])\s', b)
        if m:
            present.add(m.group(1))
            sep = ' ' + m.group(2) + ' '
    new = ['    ' + v + sep + d for v, d in values.items() if v not in present]
    return lines[:end] + new + lines[end:]


def add_examples(lines, examples):
    if not examples:
        return lines
    while lines and lines[-1].strip() == '':
        lines.pop()
    if not any(l.startswith('Examples') for l in lines):
        lines += ['', 'Examples:']
    ex_at = max(i for i, l in enumerate(lines) if l.startswith('Examples'))
    for e in examples:
        if e not in lines[ex_at:]:
            lines += ['', e]
    return lines + ['']


def insert_params(lines, sections):
    """Insert param sections (list of line-lists) before Examples:, else at the end."""
    if not sections:
        return lines
    flat = []
    for s in sections:
        flat += s + ['']
    for i, l in enumerate(lines):
        if l.startswith('Examples'):
            return lines[:i] + flat + lines[i:]
    while lines and lines[-1].strip() == '':
        lines.pop()
    return lines + [''] + flat


def build_page(name, node, reg, sup, done):
    page = fix_title(name, node['fullhelp'] or '')
    has_page = bool(page.strip()) and page.strip() != (node['help'] or '').strip()
    params = dict(sup.get('params', {}))
    if 'copy_params_from' in sup:
        src = done[sup['copy_params_from']]
        for p, (req, desc) in parse_sections(src).items():
            if p in [r[0] for r in reg[name]]:
                params.setdefault(p, (req, desc))

    if not has_page:
        intro, examples = sup['page']
        lines = ['', name, ''] + intro.split('\n') + ['']
        order = [p for p, _ in reg[name]]
        secs = [make_section(p, params[p][0], params[p][1], True)
                for p in sorted(params, key=lambda p: (not params[p][0], order.index(p)))]
        lines = insert_params(lines, secs)
        lines = add_examples(lines, examples)
        return '\n'.join(lines)

    lines = page.split('\n')
    marked = any_marked(lines) or not headers(lines)
    hs = headers(lines)
    order = [p for p, _ in reg[name]]
    secs = [make_section(p, params[p][0], params[p][1], marked)
            for p in sorted(params, key=lambda p: order.index(p)) if p not in hs]
    lines = insert_params(lines, secs)
    for p, extra in sup.get('extra', {}).items():
        hs = headers(lines)
        end = section_end(lines, hs[p])
        lines = lines[:end] + extra + lines[end:]
    lines = add_action_values(lines, sup.get('actions', {}), name)
    lines = add_examples(lines, sup.get('examples', []))
    return '\n'.join(lines)


def parse_sections(page):
    """{param: (required, description)} from a finished page (same rule as ParamDocs)."""
    lines = page.split('\n')
    hs = headers(lines)
    marked = any_marked(lines)
    out = {}
    for p, i in hs.items():
        m = HEADER_RE.match(lines[i].strip())
        req = marked and not is_optional(m)
        body = [l.strip() for l in lines[i + 1:section_end(lines, i)]]
        out[p] = (req, '\n'.join(body))
    return out


def make_brief(name, node, reg, page, sup):
    old = node['help'] or ''
    desc = sup.get('brief')
    if desc is None:
        m = re.search(r'\)\s*-\s*|\s-\s', old)
        desc = old[m.end():].strip() if m else old.strip()
    secs = parse_sections(page)
    parts = []
    for p, values in reg[name]:
        req = secs.get(p, (False, ''))[0]
        body = p + ':' + ('|'.join(values) if values else '')
        parts.append(('[%s]' if req else '(%s)') % body)
    return (' '.join(parts) + ' - ' + desc) if parts else desc


def build_all(node, reg):
    """{name: {help, fullhelp}} for every node command - also used by the MDS build."""
    # 2. registry must equal the node's accepted params, per command, in order-free terms.
    bad = []
    for name in sorted(set(node) | set(reg)):
        if name not in reg:
            bad.append('%s: in node, not in CommandRegistry' % name)
        elif name not in node:
            bad.append('%s: in CommandRegistry, not in node' % name)
        else:
            a = set(p for p, _ in reg[name])
            b = set(node[name]['params'])
            if a != b:
                bad.append('%s: registry %s vs node %s' % (name, sorted(a - b), sorted(b - a)))
    if bad:
        print('CommandRegistry disagrees with the node:\n  ' + '\n  '.join(bad))
        sys.exit(1)

    out = {}
    done = {}
    # send before sendpoll (copy_params_from)
    for name in sorted(node, key=lambda n: (n == 'sendpoll', n)):
        sup = SUPPLEMENT.get(name, {})
        page = build_page(name, node[name], reg, sup, done)
        done[name] = page
        out[name] = {'help': make_brief(name, node[name], reg, page, sup), 'fullhelp': page}

    # 5. nothing the node accepts may be left undocumented.
    missing = []
    for name in node:
        secs = parse_sections(out[name]['fullhelp'])
        for p in node[name]['params']:
            if p not in secs:
                missing.append(name + ' ' + p)
        for p, values in reg[name]:
            if p == 'action' and values:
                body = '\n'.join(out[name]['fullhelp'].split('\n'))
                for v in values:
                    if not re.search(r'^\s+(?:[a-z0-9]+/)*%s(?:/[a-z0-9]+)*\s*[:-]\s' % re.escape(v), body, re.M):
                        missing.append('%s action=%s' % (name, v))
    if missing:
        print('Still undocumented:\n  ' + '\n  '.join(missing))
        sys.exit(1)
    return out


def main():
    out = build_all(json.load(open(NODE_JSON)), load_registry())
    with open(OUT, 'w') as f:
        json.dump(dict(sorted(out.items())), f, indent=1, ensure_ascii=False)
        f.write('\n')
    print('wrote %s: %d commands' % (OUT, len(out)))


if __name__ == '__main__':
    main()
