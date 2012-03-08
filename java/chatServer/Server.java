package chatServer;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

public class Server{
  private ServerSocket serverSocket;
  private ArrayBlockingQueue<Message> messages;
  private ArrayList<ClientProxy> clients;

  public Server(int serverPort) throws IOException{
    serverSocket = new ServerSocket(serverPort);
    messages = new ArrayBlockingQueue<Message>(10);
    clients = new ArrayList<ClientProxy>(10);
    (new Thread(new MessageDispatcher())).start();
  }

  public void start(){
    System.out.println("Starting the server");
    while(true) {
      try{
        Socket client = serverSocket.accept();
        ClientProxy cp = new ClientProxy(client);
        clients.add(cp);
        (new Thread(cp)).start();
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception{
    Server server = new Server(39020);
    server.start();
  }

  private class ClientConnectionHandler implements Runnable{
    private Socket client;
    private ObjectInputStream input;

    public ClientConnectionHandler(Socket client) throws IOException{
      this.client = client;
      this.input = new ObjectInputStream(client.getInputStream());
    }

    public void run(){
      System.out.println("Got that client!!");
      Message m;
      try{
        while((m = (Message) input.readObject()) != null){
          System.out.println(m);
          messages.add(m);
        }
      }catch(IOException ioe){
          ioe.printStackTrace();
      }catch(ClassNotFoundException cne){
        cne.printStackTrace();
      }
    }
  }

  private class MessageDispatcher implements Runnable{
    public void run(){
      while(true){
        Message message;
        try{
          message = messages.take();
          System.out.println("Inside publish message = " + message);
          for(ClientProxy client : clients) {
            try{
              client.write(message);
            }catch(IOException ioe){
              ioe.printStackTrace();
            }
          }
        }catch(InterruptedException ie){
          ie.printStackTrace(); 
        }
      }
    }
  }

  private class ClientProxy implements Runnable{
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public ClientProxy(Socket socket) throws IOException {
      this.socket = socket;
      this.out = new ObjectOutputStream(socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(socket.getInputStream());
      System.out.println("Initialized ClientProxy");
    }

    public void write(Message message) throws IOException{
      out.writeObject(message);
    }

    public void run(){
      System.out.println("Waiting on input");
      Message m;
      try{
        while((m = (Message) in.readObject()) != null){
          System.out.println(m);
          messages.add(m);
        }
      }catch(IOException ioe){
          ioe.printStackTrace();
      }catch(ClassNotFoundException cne){
        cne.printStackTrace();
      }
    }

  }
}
