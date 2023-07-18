package project3;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import project3.server.Coordinator;

public class Common {
  private final String filePath;

  public Common() throws URISyntaxException {
    File file = new File(Common.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    String fileName = "servers.json";
    filePath = file.getParent() + "/" + fileName;
  }

  public String getServersFilePath() {
    return filePath;
  }

  public JSONArray readServersFile() throws IOException, ParseException {
    try {
      InputStream is = new FileInputStream(filePath);
      Reader reader = new InputStreamReader(is);

      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
      return (JSONArray) jsonObject.get("servers");
    } catch (FileNotFoundException e) {
      return new JSONArray();
    }
  }

  public void addNewServerToFile(JSONArray servers, String host, int port) throws IOException {
    JSONObject newServer = new JSONObject();
    newServer.put("host", host);
    newServer.put("port", port);

    servers.add(newServer);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("servers", servers);

    FileWriter writer = new FileWriter(filePath);
    writer.write(jsonObject.toJSONString());
    writer.flush();
    writer.close();
  }

  public List<RequestHandler> lookupRegistries() throws NotBoundException, IOException, ParseException {
    return lookupRegistries(null);
  }

  public List<RequestHandler> lookupRegistries(Coordinator coordinator) throws IOException, NotBoundException, ParseException {
    List<RequestHandler> participants = new ArrayList<>();
    int i = 1;
    for (Object pair : readServersFile()) {
      JSONObject jsonPair = (JSONObject) pair;
      String host = (String) jsonPair.get("host");
      int port = (int) ((long) jsonPair.get("port"));

      Registry registry = LocateRegistry.getRegistry(host, port);
      Logger.showInfo(String.format("Located registry for server %d at port %d\n", i++, port));

      RequestHandler server = (RequestHandler) registry.lookup("handler");
      if (coordinator != null) {
        server.setCoordinator(coordinator);
      }
      participants.add(server);
      Logger.showInfo(String.format("Found request handler at port %d\n\n", port));
    }
    return participants;
  }
}
