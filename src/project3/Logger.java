package project3;

import java.sql.Timestamp;

/**
 * Helper class that is used by other classes to display information to the user on the cli.
 */
public class Logger {

  /**
   * Method to display any additional info that may need to be displayed to the user at any given
   * time by the server/client.
   *
   * @param msg The info as a String.
   */
  public static void showInfo(String msg) {
    System.out.print(msg);
  }

  /**
   * Method to display the request that is to be sent to the client or has to be
   * processed by the server.
   *
   * @param req The request as a String (eg: DELETE 75).
   */
  public static void showRequest(String req) {
    System.out.println(getTimestamp() + " REQ: " + req);
  }

  /**
   * Method to show the response that the client receives after sending a request to server
   * or the response that the server is going to send back o the client.
   *
   * @param res The response as a String.
   */
  public static void showResponse(String res) {
    System.out.println(getTimestamp() + " RES: " + res);
  }

  /**
   * Method to display any errors encountered throughout the execution of all server/clients.
   *
   * @param msg The error message as a String.
   */
  public static void showError(String msg) {
    System.out.println(getTimestamp() + " ERROR: " + msg);
  }

  private static String getTimestamp() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    return "[" + timestamp + "]";
  }
}
