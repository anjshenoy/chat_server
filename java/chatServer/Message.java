package chatServer;

import java.io.Serializable;

class Message implements Serializable{
   private String client;
   private String body;
   private static final String signInText = "Signing In";
   private static final String signOutText = "Signing Out";

   public Message(String client){
     this.client = client;
     this.body = "";
   }

   public Message(String client, String body){
     this.client = client;
     this.body = body;
   }

   public void setBody(String text){
     this.body = text;
   }

   public static Message signIn(String client){
      return new Message(client, Message.signInText);
   }

   public static Message signOut(String client){
      return new Message(client, Message.signOutText);
   }

   public boolean signingIn(){
     return (this.body.equals(signInText));
   }

   public boolean signingOut(){
     return (this.body.equals(signOutText));
   }

   public String toString(){
     return this.client + ": " + this.body;
   }

   public String getClientName(){
      return this.client;
   }
}
