package chatServer;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client{
  private class ServerConnectionHandler implements Runnable{
    private Socket server;
    private BufferedReader input;
    private ObjectInputStream ois;

    public ServerConnectionHandler(Socket server) throws IOException{
      System.out.println("INSIDE constructor");
      this.server = server;
      System.out.println("set up server");
      this.ois = new ObjectInputStream(server.getInputStream());
      System.out.println("Initialized ServerConnectionHandler!");
    }

    public void run(){
      System.out.println("Inside run");
      Message m;
      try{
        while((m = (Message) ois.readObject()) != null){
          System.out.println(m.toString());
        }
      }catch(IOException ioe){
        ioe.printStackTrace();
      }catch(ClassNotFoundException cne){
        cne.printStackTrace();
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
    System.out.println("Inside start");
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    Message msg = Message.signIn(name);
    String m;
    ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
    oos.flush();
    oos.writeObject(msg);
    while((m = in.readLine())!=null){
      msg = new Message(name, m);
      oos.writeObject(msg);
    }
  }

  public static void main(String[] args){
    System.out.println(args[0]);
    try{
      Client c = new Client(args[0], 39020);
      System.out.println(c.name);
      c.start();
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

}
