package project3.server;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import project3.Common;
import project3.Logger;
import project3.RequestHandler;

public class CoordinatorImpl implements Coordinator {
  private final List<RequestHandler> participants;

  public CoordinatorImpl(String host, int port) throws URISyntaxException, IOException, NotBoundException, ParseException {
    Logger.showInfo("Starting coordinator...\n");

    System.setProperty("java.rmi.server.hostname", host);
    Coordinator thisObj = (Coordinator) UnicastRemoteObject.exportObject(this, port);

    Logger.showInfo("Creating Registry\n\n");
    Registry registry = LocateRegistry.createRegistry(port);

    registry.rebind("coordinator", thisObj);

    Common common = new Common();
    participants = common.lookupRegistries(this);
    File f = new File(common.getServersFilePath());
    f.deleteOnExit();

    if (participants.size() == 0) {
      throw new RuntimeException("No participants running!");
    }
    Logger.showInfo("Coordinator is setup and ready to go!\n\n");
  }

  @Override
  public boolean broadcastPrepare() throws RemoteException, InterruptedException {
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

  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Incorrect cli arguments. Must be exactly 2. Host ip and port.");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Coordinator coordinator = new CoordinatorImpl(host, port);
    } catch (RuntimeException | URISyntaxException | IOException | NotBoundException |
             ParseException e) {
      Logger.showError(e.getMessage());
    }
  }
}
