package project3.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import project3.Logger;
import project3.RequestHandler;

/**
 * Class that simulates the Participant in the 2 phase commit protocol and a server in the Java RMI
 * client/server model. This class validates the arguments provided from cli, establishes
 * connection at the specified ip address, and also creates the rmi registry which will hold the
 * RequestHandler object. It also gets the coordinator object from the coordinator registry and adds
 * the current participant to the coordinator's list of participants.
 */
public class Participant {

  /**
   * Driver method that is the entry point of the program.
   * This method is executed when we run the program where it validates the cli arguments received
   * and calls the required methods to proceed further in the program execution.
   *
   * @param args String array for command line arguments to be passed when running the program.
   *             For this program this array should have four elements which are the ip address
   *             of host and port number to export the object to and the coordinator's ip address
   *             and port.
   */
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
