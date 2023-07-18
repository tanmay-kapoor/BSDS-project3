package project3.server;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import project3.Common;
import project3.Logger;
import project3.RequestHandler;

public class Participant {

  public Participant(String host, int port) throws IOException, ParseException, URISyntaxException {
    Logger.showInfo("Starting server...\n");

    System.setProperty("java.rmi.server.hostname", host);
    RequestHandler obj = new RequestHandlerImpl();
    RequestHandler handler = (RequestHandler) UnicastRemoteObject.exportObject(obj, port);

    Logger.showInfo("Creating Registry\n");
    Registry registry = LocateRegistry.createRegistry(port);
    registry.rebind("handler", handler);

    // add new server to json file
    Common common = new Common();
    JSONArray servers = common.readServersFile();
    common.addNewServerToFile(servers, host, port);
  }

  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Incorrect cli arguments. Must be exactly 2. Host ip and port.");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Participant participant = new Participant(host, port);
      Logger.showInfo("Participant ready at port " + port + " \n\n");
    } catch (URISyntaxException | IOException | ParseException | IllegalArgumentException e) {
      Logger.showError(e.getMessage());
    }
  }
}
