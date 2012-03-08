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
  private ArrayList<Socket> clients;

  public Server(int serverPort) throws IOException{
    serverSocket = new ServerSocket(serverPort);
    messages = new ArrayBlockingQueue<Message>(10);
    clients = new ArrayList<Socket>(10);
    (new Thread(new ClientOutputStreamHandler())).start();
  }

  public void start(){
    System.out.println("Starting the server");
    while(true) {
      try{
        Socket client = serverSocket.accept();
        clients.add(client);
        (new Thread(new ClientConnectionHandler(client))).start();
      }catch(IOException ioe){
        ioe.printStackTrace();
      }
    }
  }

  public void publish(Message message){
    System.out.println("Inside publish message = " + message);
    for (int j=0; j < clients.size(); j++){
      try{
        Socket client = clients.get(j);
        new ObjectOutputStream(client.getOutputStream()).writeObject(message);
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

  private class ClientOutputStreamHandler implements Runnable{

    public void run(){
      while(true){
        Message message;
        try{
          message = messages.take();
          publish(message);
        }catch(InterruptedException ie){
          ie.printStackTrace(); 
        }
      }
    }
  }
}
