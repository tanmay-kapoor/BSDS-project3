package project3.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import project3.RequestHandler;

/**
 * Interface that defines the methods that the Coordinator object should implement.
 * Coordinator is the one that interacts with all the servers in the system to ensure data
 * synchronization between key-value store of each server.
 */
public interface Coordinator extends Remote {
  /**
   * Method to add to the list of participants. It is called whenever a new participant is started.
   *
   * @param participant The server to be added to the list of participants.
   * @throws RemoteException If there is an error in the remote method call.
   */
  void addParticipant(RequestHandler participant) throws RemoteException;

  /**
   * Method to check if any of the participants is busy.
   *
   * @return True if any of the participants is busy. False otherwise.
   * @throws RemoteException If there is an error in the remote method call.
   */
  boolean isAnyParticipantBusy() throws RemoteException;

  /**
   * Method that initiates the 2 phase commit protocol across all the participants.
   * It asks each participant on a separate thread to prepare for commit.
   *
   * @return True if all participants are ready to commit. False otherwise.
   * @throws RemoteException      If there is an error in the remote method call.
   * @throws InterruptedException If the thread is interrupted while waiting for the participants
   *                              to prepare.
   */
  boolean broadcastPrepare() throws RemoteException, InterruptedException;

  /**
   * Method that asks all participants whether they want to commit/abort the ongoing transaction.
   *
   * @return True if all participants want to commit. False otherwise.
   * @throws RemoteException      If there is an error in the remote method call.
   * @throws InterruptedException If the thread is interrupted while waiting for the participants
   *                              to vote.
   */
  boolean broadcastCommit() throws RemoteException, InterruptedException;

  /**
   * Method that asks all participants to insert/replace a key-value pair in their key-value store.
   *
   * @param key   Key to be inserted/replaced.
   * @param value Value to be inserted/replaced.
   * @throws RemoteException      If there is an error in the remote method call.
   * @throws InterruptedException If the thread is interrupted while waiting for the participants
   */
  void broadcastPut(String key, String value) throws RemoteException, InterruptedException;

  /**
   * Method that asks all participants to delete a key from their key-value store.
   *
   * @param key Key to be deleted.
   * @throws RemoteException      If there is an error in the remote method call.
   * @throws InterruptedException If the thread is interrupted while waiting for the participants
   */
  void broadcastDelete(String key) throws RemoteException, InterruptedException;
}
