package project3.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import project3.Logger;
import project3.RequestHandler;

public class Participant {

  public static void main(String[] args) {
    try {
      if (args.length != 4) {
        throw new IllegalArgumentException("Incorrect cli arguments. Must be exactly 4. " +
                "Host ip, port, coordinator host ip, coordinator port");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);
      String coordinatorHost = args[2];
      int coordinatorPort = Integer.parseInt(args[3]);

      Logger.showInfo("Starting server...\n");

      Registry coordinatorRegistry = LocateRegistry.getRegistry(coordinatorHost, coordinatorPort);
      Coordinator coordinator = (Coordinator) coordinatorRegistry.lookup("coordinator");

      System.setProperty("java.rmi.server.hostname", host);
      RequestHandler obj = new RequestHandlerImpl();
      RequestHandler handler = (RequestHandler) UnicastRemoteObject.exportObject(obj, port);

      handler.setCoordinator(coordinator);
      coordinator.addParticipant(handler);

      Logger.showInfo("Creating Registry\n");
      Registry registry = LocateRegistry.createRegistry(port);
      registry.rebind("handler", handler);

      Logger.showInfo("Participant ready at port " + port + " \n\n");
    } catch (IOException | IllegalArgumentException |
             NotBoundException e) {
      Logger.showError(e.getMessage());
    }
  }
}
