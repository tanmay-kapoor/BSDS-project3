# BSDS-project3

## Available Scripts

After unzipping the project, type this in the terminal

```
cd out/artifacts   :   to enter the folder where all the jar files are present
```

### Starting the Coordinator

```
java -jar Coordinator.jar <ip-address> <port>
```

### Starting a Participant

```
java -jar Participant.jar <ip-address> <port> <coordinator-ip> <coordinator-port>
```

`coordinator-ip` and `coordinator-port` are the ip address and port of the coordinator that were
supplied in the previous command. Run this command multiple times to start multiple participants.

### Starting the Client

```
java -jar Client.jar <ip-address-1> <port-1> <ip-address-2> <port-2> <ip-address-3> <port-3>.....
```

`ip-address` and `port` are the ip address and port of the participants that were supplied in the
previous commands (you should have as many ip addresses and ports as you have participants). Run
this command only once.

Once these commands have been typed, you can start sending requests via the client. Requests are
tab separated which means you have to send the request in the following format

```
server_1 \t+ GET \t+ key
server_3 \t+ PUT \t+ key \t+ value can be space separated
server_2 \t+ DELETE \t+ key can also be space seprated      
```

Here `\t+` denotes one or more tab key presses. If you use space instead of tab, then the requests
will throw errors.

### To check the screenshots uploaded as part of this assignment

Do this step if you are in `artifacts` directory

```
cd ../screenshots
```

To show the operations comfortable, the screenshots run only 2 participants and 2 clients. You can
run as many instances as you want.

### Please check `Summary.md` in the `out` directory for key learnings.

## Clarifications

In my design, I have chosen to do the following:

- If a client does put/delete request on a key and another client does a get request on the same
  key,
  then the get request is rejected since the ongoing transaction might update that key. However, if
  the get request is for a different key then it is allowed since the 2 requests are unrelated.
- If a client does a put/delete request on one key and another client does a put/delete request on
  the same/different key, it is rejected. This was done since one of TAs informed me that two 2pc
  protocols should not be allowed to execute till the first one has finished executing.
