package chatServer;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.net.Socket;

public class Client{
  private class ServerConnectionHandler implements Runnable{
    private Socket server;
    private BufferedReader input;
    private ObjectInputStream ois;

    public ServerConnectionHandler(Socket server) throws IOException{
      this.server = server;
      this.ois = new ObjectInputStream(server.getInputStream());
    }

    public void run(){
      Message m;
      try{
        while((m = (Message) ois.readObject()) != null){
          System.out.println(m);
        }
      }catch(ClassNotFoundException cne){
        cne.printStackTrace();
      }catch(EOFException eofe){
        System.out.println("You are now logged out");
        System.exit(0);
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

  private Socket server;
  private String name;
  private BufferedReader input;
  private DataOutputStream outputStream;

  public Client(String name, int port) throws IOException{
    this.name = name;
    this.server = new Socket("localhost", port);
    (new Thread(new ServerConnectionHandler(this.server), name)).start();
  }

  public void start() throws IOException{
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    Message msg = Message.signIn(name);
    String m;
    ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
    oos.flush();
    oos.writeObject(msg);
    while((m = in.readLine()) != Message.signOutText){
      msg = new Message(name, m);
      oos.writeObject(msg);
    }
    oos.writeObject(Message.signOut(name));
    server.close();
  }

  public static void main(String[] args){
    try{
      Client c = new Client(args[0], 39020);
      c.start();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

}
