package project3.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import project3.Logger;
import project3.RequestHandler;

/**
 * Class that represents a client in the Remote Method Invocation simulation. This is where all
 * users connect to the servers and send requests which send back a response.
 */
public class Client {
  private final Scanner sc;
  private final List<RequestHandler> serversList;

  /**
   * Constructor to initialize the scanner object for user input. Also, to initialize the list of
   * servers to send requests to.
   */
  public Client() {
    this.sc = new Scanner(System.in);
    this.serversList = new ArrayList<>();
  }

  private void start() {
    String command = "";
    Logger.showInfo(
            "\nAll valid request formats:\n\n" +
                    "server_i GET x\n" +
                    "server_i PUT x y\n" +
                    "server_i DELETE x\n" +
                    "STOP\n\n" +
                    "Requests are tab separated. eg : server_3 \\t PUT \\t This is the key \\t This is the value\n");
    while (true) {
      try {
        if (command.trim().equalsIgnoreCase("stop")) {
          break;
        }
        Logger.showInfo("REQ to send: ");
        command = sc.nextLine();

        int serverNumber = 1;
        if (!command.trim().equalsIgnoreCase("stop")) {
          int serverNumberEndIndex = command.indexOf("\t", 8);
          String serverNumberString = command.substring(7, serverNumberEndIndex).trim();
          serverNumber = Integer.parseInt(serverNumberString);
          command = command.substring(serverNumberEndIndex + 1).trim();
        }

        String res = this.serversList.get(serverNumber - 1).handleRequest(command);
        Logger.showResponse(res);
      } catch (StringIndexOutOfBoundsException e) {
        Logger.showError("Please specify which server to send the request to.");
      } catch (IndexOutOfBoundsException e) {
        Logger.showError("Please specify a number from 1 to " + this.serversList.size() + " which is the current number of servers.");
      } catch (RuntimeException | InterruptedException | IOException e) {
        Logger.showError(e.getMessage());
      }
    }
  }

  /**
   * Driver that is the entry point for the client. This method is executed when we run the
   * program where it validates the cli arguments received and calls the required methods to
   * proceed further in the program execution.
   *
   * @param args String array for command line arguments to be passed when running the program.
   *             For this program this array should have even elements. For every 2 elements,
   *             first is the host ip of server and second will be the port at which the server
   *             is running.
   */
  public static void main(String[] args) {
    try {
      if (args.length % 2 == 1) {
        throw new IllegalArgumentException("Invalid number of arguments! " +
                "Please specify the host and port number for each server.");
      }

      Logger.showInfo("Looking up all servers...\n\n");
      Client client = new Client();

      for (int i = 0; i < args.length; i += 2) {
        String host = args[i];
        int port = Integer.parseInt(args[i + 1]);
        Registry registry = LocateRegistry.getRegistry(host, port);
        RequestHandler handler = (RequestHandler) registry.lookup("handler");
        client.serversList.add(handler);
      }

      if (client.serversList.size() == 0) {
        throw new RuntimeException("No servers running!");
      }

      client.start();
    } catch (RuntimeException | NotBoundException | RemoteException e) {
      Logger.showError(e.getMessage());
    }
  }
}
