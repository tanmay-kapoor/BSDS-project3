package project3.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Coordinator extends Remote {
  boolean broadcastPrepare() throws RemoteException, InterruptedException;

  boolean broadcastCommit() throws RemoteException, InterruptedException;

  void broadcastPut(String key, String value) throws RemoteException, InterruptedException;

  void broadcastDelete(String key) throws RemoteException, InterruptedException;
}
