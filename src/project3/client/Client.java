package project3.client;

import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import project3.Common;
import project3.Logger;
import project3.RequestHandler;

public class Client {
  private final Scanner sc;
  private List<RequestHandler> servers;

  public Client() throws URISyntaxException, NotBoundException, IOException, ParseException {
    this.sc = new Scanner(System.in);
    this.servers = new ArrayList<>();

    Logger.showInfo("Looking up all servers...\n\n");
    Common common = new Common();
    this.servers = common.lookupRegistries();
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

        String res = this.servers.get(serverNumber - 1).handleRequest(command);
        Logger.showResponse(res);
      } catch (StringIndexOutOfBoundsException e) {
        Logger.showError("Please specify which server to send the request to.");
      } catch (IndexOutOfBoundsException e) {
        Logger.showError("Please specify a number from 1 to " + this.servers.size() + " which is the current number of servers.");
      } catch (RuntimeException | InterruptedException | IOException e) {
        Logger.showError(e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    try {
      Client client = new Client();
      client.start();
    } catch (FileNotFoundException e) {
      Logger.showError("No servers found!");
    } catch (URISyntaxException | NotBoundException | ParseException |
             IOException e) {
      Logger.showError(e.getMessage());
    }
  }
}
