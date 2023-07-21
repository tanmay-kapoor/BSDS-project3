package project3;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import project3.server.Coordinator;

/**
 * Interface that holds the common methods that can be performed by the server. Implementations of
 * this interface are the classes that handle all the requests. This interface's implementations
 * are also added to the rmi registry which is then used at the client side to retrieve and
 * call its methods.
 */
public interface RequestHandler extends Remote {
  /**
   * Method that is used to set the coordinator for a server. Each put/delete request is completed
   * only if the coordinator is successful in completing the 2 phase commit protocol.
   *
   * @param coordinator The coordinator object that is used to perform the 2 phase commit protocol.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  void setCoordinator(Coordinator coordinator) throws RemoteException;

  /**
   * Method that is the gateway for all GET, PUT, DELETE or disconnect requests. All requests are
   * taken in the parameter as a tab separated string and depending on the request type retrieved
   * from the content of the string, it is processed differently.
   *
   * @param command The request as a tab separated String.
   * @return The response that is sent to the client as a String.
   * @throws IOException          in case of any errors while interaction between the server/client
   *                              and/or any read-write problems.
   * @throws InterruptedException in case the main thread fails to wait till all the threads have
   *                              finished executing.
   */
  String handleRequest(String command) throws IOException, InterruptedException;

  /**
   * Method that is called by the coordinator for each server to ask whether it is ready to carry
   * out the transaction or not.
   *
   * @return true if it is ready, false if it is busy (most likely with an ongoing transaction).
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  boolean askPrepare() throws RemoteException;

  /**
   * Method that is called by the coordinator for each server to ask whether it is ready to commit
   * the transaction or not.
   *
   * @return true if it wants to commit, false if it wants to abort the transaction.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  boolean askCommit() throws RemoteException;

  /**
   * Method that is responsible for handling the GET request from the client.
   *
   * @param key Key to retrieve the value of from the hashmap.
   * @return The value of the key in the hashmap.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  String get(String key) throws RemoteException;

  /**
   * Method that is responsible for handling the PUT request from the client.
   *
   * @param key   Key to insert/replace the value of in the hashmap.
   * @param value Value of the key to be stored in the hashmap.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  void put(String key, String value) throws RemoteException;

  /**
   * Method that is responsible for handling the Delete request from the client.
   *
   * @param key Key to delete from the hashmap.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  void delete(String key) throws RemoteException;

  /**
   * Method that checks whether the current server is busy or not.
   *
   * @return true if it is busy, false if it is idle.
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  boolean isBusy() throws RemoteException;

  /**
   * Method that is called by the coordinator to set the state of the server to idle.
   *
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  void setIdleState() throws RemoteException;

  /**
   * Method that is called by the coordinator to set the state of the server to busy.
   *
   * @throws RemoteException in case of any errors while interaction between the server/client
   *                         and/or any read-write problems.
   */
  void setBusyState() throws RemoteException;
}
