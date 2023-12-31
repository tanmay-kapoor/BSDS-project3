package project3.server;

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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import project3.Logger;
import project3.RequestHandler;

/**
 * Class that implements the RequestHandler interface and is basically a representation of the
 * object that needs to be export on a specific port number which can then be used by the client
 * to send requests to be served.
 */
public class RequestHandlerImpl implements RequestHandler {
  private final Map<String, String> map;
  private String filePath;
  private Coordinator coordinator;
  private final Scanner sc;
  private State state;

  private enum State {
    IDLE, BUSY
  }

  /**
   * Constructor that initializes the hash-map that is going to be the key-value store to be
   * demonstrated in this project and the state of the server which is idle at the beginning.
   *
   * @throws RemoteException in case of communication related errors during the execution of a
   *                         remote method call.
   */
  public RequestHandlerImpl() throws RemoteException {
    super();
    this.state = State.IDLE;
    sc = new Scanner(System.in);
    map = new ConcurrentHashMap<>();
    Logger.showInfo("Populating HashMap\n");

    // read values from file
    try {
      String fileName = "contents.json";
      File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
      filePath = file.getParent() + "/" + fileName;
      readFromFile();
    } catch (URISyntaxException e) {
      Logger.showError(e.getMessage());
    }
  }

  private void readFromFile() {
    try {
      InputStream is = new FileInputStream(filePath);
      Reader reader = new InputStreamReader(is);

      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
      JSONArray data = (JSONArray) jsonObject.get("data");
      for (Object pair : data) {
        JSONObject jsonPair = (JSONObject) pair;
        String key = (String) jsonPair.get("key");
        String value = (String) jsonPair.get("value");
        map.put(key, value);
      }
    } catch (FileNotFoundException ignored) {
      // file does not exist. But hashmap is already initialized hence ignore.
    } catch (IOException | ParseException e) {
      Logger.showError(e.getMessage());
    }
  }

  @Override
  public void setCoordinator(Coordinator coordinator) {
    this.coordinator = coordinator;
  }

  @Override
  public String handleRequest(String command) throws IOException, InterruptedException {
    command = command.trim();
    String res;

    String[] req = command.split("\\t+");
    req[0] = req[0].toUpperCase();

    boolean shouldProceed;
    switch (req[0]) {
      case "GET":
        validateRequest(req, 2);
        shouldProceed = !coordinator.isPartOfOngoingTransaction(req[1]);
        if (shouldProceed) {
//          Logger.showRequest(command);
          res = this.get(req[1]);
        } else {
          throw new RuntimeException("Request aborted. There is an ongoing transaction that deals with the specified key.");
        }
        break;

      case "PUT":
        validateRequest(req, 3);
        shouldProceed = coordinator.broadcastPrepare(req[1]);
        if (shouldProceed) {
//          Logger.showRequest(command);
          coordinator.broadcastPut(req[1], req[2]);
          res = "Put successful";
        } else {
          throw new RuntimeException("Request aborted. 1 or more participants failed to prepare/commit.");
        }
        break;

      case "DELETE":
        validateRequest(req, 2);
        shouldProceed = coordinator.broadcastPrepare(req[1]);
        if (shouldProceed) {
//          Logger.showRequest(command);
          coordinator.broadcastDelete(req[1]);
          res = "Delete successful";
        } else {
          throw new RuntimeException("Request aborted. 1 or more participants failed to prepare/commit.");
        }
        break;

      case "STOP":
        validateRequest(req, 1);
//        Logger.showRequest(command);
        this.writeToFile();
        res = "Disconnected client";
        break;

      default:
        Logger.showError("Invalid request");
        throw new IllegalArgumentException("Invalid request. Must be GET, PUT, DELETE or STOP only.");
    }

    return res;
  }

  @Override
  public boolean isBusy() throws RemoteException {
    return this.state == State.BUSY;
  }

  @Override
  public void setIdleState() throws RemoteException {
    this.state = State.IDLE;
  }

  @Override
  public void setBusyState() throws RemoteException {
    this.state = State.BUSY;
  }

  @Override
  public boolean askPrepare() throws RemoteException {
    if (isBusy()) {
      return false;
    }
    setBusyState();
    Logger.showInfo("New transaction started. Are you prepared? (yes/no) : ");
    return sc.nextLine().trim().equalsIgnoreCase("yes");
  }

  @Override
  public boolean askCommit() throws RemoteException {
    Logger.showInfo("Are you ready to commit? (yes/no) : ");
    return sc.nextLine().trim().equalsIgnoreCase("yes");
  }

  @Override
  public String get(String key) {
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException("Can't get key that doesn't exist");
    }
    return map.get(key);
  }

  @Override
  public void put(String key, String value) throws RuntimeException {
    map.put(key, value);
  }

  @Override
  public void delete(String key) {
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException("Can't delete key that doesn't exist");
    }
    map.remove(key);
  }

  private void writeToFile() throws IOException {
    // write to file and disconnect client
    JSONObject jsonObject = new JSONObject();
    JSONArray data = new JSONArray();
    for (String key : map.keySet()) {
      JSONObject details = new JSONObject();
      details.put("key", key);
      details.put("value", map.get(key));
      data.add(details);
    }
    jsonObject.put("data", data);

    FileWriter writer = new FileWriter(filePath);
    writer.write(jsonObject.toJSONString());
    writer.flush();
    writer.close();
  }

  private void validateRequest(String[] req, int len) {
    if (req.length != len) {
      String msg = "Invalid number of arguments with " + req[0] + " request. Must be exactly " + (len - 1);
      throw new IllegalArgumentException(msg);
    }
  }
}
