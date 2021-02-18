package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import com.google.protobuf.ByteString;

public class Chat {
  public static String usuario = "";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    
    factory.setHost("ec2-54-221-15-76.compute-1.amazonaws.com");
    factory.setUsername("matheu|Jorge");
    factory.setPassword("12345678");
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    Channel channel_arquivos = connection.createChannel();
    
    Scanner entrada = new Scanner(System.in);
    Grupo grupo = new Grupo(channel,channel_arquivos);
    
    Mensagem line = new Mensagem();
    
    System.out.print("User: ");
    String user = entrada.nextLine();
    
    String nomeGrupo = "";
    line.criarDiretorio(user);
    
    String QUEUE_NAME = user;
    String QUEUE_NAME_FILE = QUEUE_NAME + "F";
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
    channel_arquivos.queueDeclare(QUEUE_NAME_FILE, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
        throws IOException {
          try{
            System.out.println(line.recebeMessagem(body, user));
          } catch (Exception ex){
            System.out.println (ex.toString());
          }
          System.out.println(Chat.usuario + ">>");
        }
    };
    

    String msg = "";
    
    channel.basicConsume(QUEUE_NAME, true,    consumer);
    channel_arquivos.basicConsume(QUEUE_NAME_FILE, true, consumer);

    while(msg.equals(".") == false){
      System.out.print(Chat.usuario + ">> " + "");
      msg = entrada.nextLine();

      if(msg.equals(".") == true) 
        break;
      if(msg.charAt(0) == '@'){
        Chat.usuario = msg;
        nomeGrupo = "";
      }
      else if(msg.charAt(0) == '#'){
        Chat.usuario = msg;
        nomeGrupo = msg.substring(1);
      }
      else if(msg.charAt(0) == '!'){
        grupo.verificaMensagem(msg, user, Chat.usuario.substring(1), nomeGrupo);
      }
      else if(Chat.usuario.equals("") == false){  
        if(Chat.usuario.charAt(0) == '#')
          line.enviarMessagem(user, msg, "", channel, nomeGrupo);
        else 
          line.enviarMessagem(user, msg, Chat.usuario.substring(1), channel, "");
      }
    }
    channel.close();
    connection.close();
  }
}