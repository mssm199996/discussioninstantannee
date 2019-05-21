/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee.serveur;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author simo996
 */
public class Serveur {
    public static String urlfb = "jdbc:firebirdsql://localhost:3050/";
    public static String userfb = "SYSDBA";
    public static String passwdfb = "masterkey";
    public static ArrayList<Abonne> listeClient; 
    private ServerSocket gerant;
    private int port;
    private String ipServer;
    private int maxConnexion;
    private boolean serveurON;
    
    public Serveur(String add,int port,int maxConnexion){
        try{
            this.ipServer = add;
            this.port = port;
            this.maxConnexion = maxConnexion;
            this.gerant = new ServerSocket(this.port);
            this.serveurON = true;
            String str = (getClass().getResource("/BDD/DISCUSSIONINSTANTANNEE.FDB")).toString();
            str = str.substring(str.indexOf("/") + 1);
            urlfb += str;
            listeClient = new ArrayList<Abonne>();
            System.out.println("Final url = " + urlfb);
        }
        catch(UnknownHostException exp){
            System.out.println("IP Du serveur inconnu");
        }
        catch(IOException exp){
            exp.printStackTrace();
        }
    }
    public static int getAbonne(int num){
        int i = 0;
        while((i < listeClient.size()) && (listeClient.get(i).getNumber() != num))
            i++;
        return i;
    }
    public static int getNumbAbonnee(String str){
        int i = 0;
        while((i < listeClient.size()) && (!listeClient.get(i).getNickname().equals(str)))
            i++;
        if(i < listeClient.size())
            return listeClient.get(i).getNumber();
        else return 0;
    }
    public void connecxion(){
        Thread t = new Thread(new Runnable(){
            public void run() {
                try{
                    while(serveurON){
                        Socket client = gerant.accept();
                        Thread customer = new Thread(new Employee(client));
                        customer.start();
                    }
                }
                catch(IOException exp){
                    exp.printStackTrace();
                }
            }
        });
        t.start();
    }
}
