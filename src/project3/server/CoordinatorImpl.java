package project3.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import project3.Logger;
import project3.RequestHandler;

/**
 * Class that simulates the Participant in the 2 phase commit protocol. This class validates the
 * arguments provided from cli, establishes connection at the specified ip address, and also creates
 * the rmi registry which will hold the Coordinator object. It is responsible for broadcasting
 * prepare to all servers, receiving positive negative response, broadcasting commit message to all
 * servers and receiving positive or negative response.
 */
public class CoordinatorImpl implements Coordinator {
  private final List<RequestHandler> participants;
  private String ongoingTransactionKey;

  /**
   * Constructor to initialize the participants list. Whenever a new participant is started, it is
   * added to this list to reference in the future.
   */
  public CoordinatorImpl() {
    participants = new ArrayList<>();
    ongoingTransactionKey = "";
  }

  @Override
  public void addParticipant(RequestHandler participant) throws RemoteException {
    participants.add(participant);
    Logger.showInfo("Added participant\n");
  }

  @Override
  public boolean isPartOfOngoingTransaction(String key) throws RemoteException {
    return ongoingTransactionKey.equals(key);
  }

  @Override
  public boolean broadcastPrepare(String key) throws RemoteException, InterruptedException {
    ongoingTransactionKey = key;
    boolean shouldProceed = this.broadcast("prepare");
    if (!shouldProceed) {
      Logger.showError("A participant failed to prepare.");
      return false;
    }
    return this.broadcastCommit();
  }

  @Override
  public boolean broadcastCommit() throws RemoteException, InterruptedException {
    boolean shouldProceed = this.broadcast("commit");
    ongoingTransactionKey = "";
    if (!shouldProceed) {
      Logger.showError("A participant failed to commit.");
      return false;
    }
    return true;
  }

  private boolean broadcast(String command) throws RemoteException, InterruptedException {
    List<Thread> threads = new ArrayList<>();
    List<Boolean> results = new ArrayList<>();

    for (RequestHandler participant : participants) {
      if (participant.isBusy()) {
        return false;
      }
      Thread thread = new Thread(() -> {
        try {
          switch (command) {
            case "prepare":
              results.add(participant.askPrepare());
              break;

            case "commit":
              results.add(participant.askCommit());
              break;
          }
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      threads.add(thread);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (int i = 0; i < threads.size(); i++) {
      threads.get(i).join();
      participants.get(i).setIdleState();
    }

    boolean shouldProceed = true;
    for (boolean result : results) {
      if (!result) {
        shouldProceed = false;
        break;
      }
    }
    return shouldProceed;
  }

  @Override
  public void broadcastPut(String key, String value) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();

    for (RequestHandler participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          participant.put(key, value);
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }

  @Override
  public void broadcastDelete(String key) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();

    for (RequestHandler participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          participant.delete(key);
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }

  /**
   * Driver method of the class which is the entry point of the program. It takes 2 cli arguments
   * which are the ip address and port of the machine to export the object to.
   *
   * @param args String array for command line arguments to be passed when running the program.
   *             For this program this array should have two elements which are the ip address and
   *             port to export the object to.
   */
  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Incorrect cli arguments. Must be exactly 2. Host ip and port.");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Logger.showInfo("Starting coordinator...\n");
      Coordinator obj = new CoordinatorImpl();

      System.setProperty("java.rmi.server.hostname", host);
      Coordinator coordinator = (Coordinator) UnicastRemoteObject.exportObject(obj, port);

      Logger.showInfo("Creating Registry\n\n");
      Registry registry = LocateRegistry.createRegistry(port);
      registry.rebind("coordinator", coordinator);

      Logger.showInfo("Coordinator is setup and ready to go!\n\n");
    } catch (RuntimeException | IOException e) {
      Logger.showError(e.getMessage());
    }
  }
}
