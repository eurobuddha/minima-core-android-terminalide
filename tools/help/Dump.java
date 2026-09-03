import org.minima.system.commands.Command;
import org.minima.system.commands.CommandRunner;
import org.minima.utils.json.JSONObject;
import org.minima.utils.json.JSONArray;
public class Dump {
  public static void main(String[] a) throws Exception {
    JSONObject all = new JSONObject();
    for (Command c : CommandRunner.ALL_COMMANDS) {
      JSONObject j = new JSONObject();
      j.put("help", c.getHelp());
      j.put("fullhelp", c.getFullHelp());
      JSONArray p = new JSONArray(); for (String s : c.getValidParams()) p.add(s);
      j.put("params", p);
      all.put(c.getName(), j);
    }
    System.out.println(all.toString());
  }
}
