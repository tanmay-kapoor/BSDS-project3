package project3.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import project3.RequestHandler;

public interface Coordinator extends Remote {
  void addParticipant(RequestHandler participant) throws RemoteException;

  boolean broadcastPrepare() throws RemoteException, InterruptedException;

  boolean broadcastCommit() throws RemoteException, InterruptedException;

  void broadcastPut(String key, String value) throws RemoteException, InterruptedException;

  void broadcastDelete(String key) throws RemoteException, InterruptedException;
}
