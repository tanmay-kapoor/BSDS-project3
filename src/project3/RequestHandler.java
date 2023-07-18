package project3;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import project3.server.Coordinator;

public interface RequestHandler extends Remote {
  void setCoordinator(Coordinator coordinator) throws RemoteException;

  String handleRequest(String command) throws IOException, InterruptedException;

  boolean askPrepare() throws RemoteException;

  boolean askCommit() throws RemoteException;

  void put(String key, String value) throws RemoteException;

  void delete(String key) throws RemoteException;
}
