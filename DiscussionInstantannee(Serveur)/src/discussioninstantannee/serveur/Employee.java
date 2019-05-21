/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee.serveur;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simo996
 */
public class Employee extends Thread{
    private Connection connect;
    private Socket client;
    private BufferedInputStream recepteurRequetes;
    private BufferedReader recepteurDonnees;
    private PrintWriter emetteurDeReponses;
    private boolean occuperClient;
    public Employee(Socket s){
        this.client = s;
        this.occuperClient = true;
    }
    public void run(){
        try{
            while(this.occuperClient){
                // IO Objetcs
                this.emetteurDeReponses = new PrintWriter(this.client.getOutputStream());
                this.recepteurRequetes = new BufferedInputStream(this.client.getInputStream());
                this.recepteurDonnees = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                String requete = lireRequete();
                System.out.println("Requete = " + requete);
                String reponse = getReponse(requete);
                this.emetteurDeReponses.write(reponse);
                this.emetteurDeReponses.flush();
                if(!this.occuperClient){
                    this.emetteurDeReponses.close();
                    this.recepteurRequetes.close();
                    this.recepteurDonnees.close();
                    this.client.close();
                }
            }
        }
        catch(SocketException exp){
            exp.printStackTrace();
        }
        catch(IOException | ClassNotFoundException | SQLException exp){
            exp.printStackTrace();
        }
    }
    private String lireRequete() throws IOException{
        String reponse = "";
        byte[] flux = new byte[1024];
        int i = this.recepteurRequetes.read(flux);
        reponse = new String(flux,0,i);
        return reponse;
    }
    private String lireDonnees() throws IOException{
        String reponse = this.recepteurDonnees.readLine();
        return reponse;
    }
    private String getReponse(String requete) throws IOException,ClassNotFoundException,SQLException{
        String reponse = "";
        if(requete.startsWith("CHECK_ID_PASS")){
                int espace1 = requete.indexOf(" ");
                int espace2 = requete.indexOf(" ",espace1 + 1);
                String id = requete.substring(espace1 + 1,espace2);
                String mdp = requete.substring(espace2 + 1, requete.length());
                this.getConnectionUtility(Serveur.urlfb, Serveur.userfb, Serveur.passwdfb);
                Statement state = this.connect.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet resultat = state.executeQuery("select * from COMPTES where ID_CLIENT = '" + id + "' and "
                        + "PASS_CLIENT = '" + mdp + "'");
                boolean available = resultat.first();
                if(available){
                    reponse = Integer.toString(resultat.getInt("NUM_CLIENT"));
                    int num = Integer.parseInt(reponse);
                    resultat = state.executeQuery("select SURNOM_CLIENT,NUM_CLIENT from "
                            + "INFORMATIONS_CLIENT where NUM_CLIENT = " + num);
                    resultat.first();
                    String surnom = resultat.getString("SURNOM_CLIENT");
                    Serveur.listeClient.add(new Abonne(num,surnom));
                    reponse += (" " + surnom);
                    resultat.close();
                    state.close();
                    this.connect.close();
                    this.connect = null;
                }
                else  reponse = "0";                
            }
        else if(requete.equals("ON_MEMBERS")){
            for(Abonne a : Serveur.listeClient)
                reponse += a.getNickname() + "|";
        }
        else if(requete.startsWith("GET_NEW_MESSAGES")){
            int debut = requete.indexOf(" ") + 1;
            int fin = requete.indexOf("-");
            String person1 = requete.substring(debut,fin);
            String person2 = requete.substring(fin + 1);
            int NUM1 = Serveur.getNumbAbonnee(person1);
            int NUM2 = Serveur.getNumbAbonnee(person2);
            if((NUM1 != 0) && (NUM2 != 0)){
                this.getConnectionUtility(Serveur.urlfb, Serveur.userfb, Serveur.passwdfb);
                Statement state = this.connect.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet resultat = state.executeQuery("select * from FORMULAIRES where (NUM_PERSONNE_1 = " + NUM1 
                    + " and NUM_PERSONNE_2 = " + NUM2 + ") or (NUM_PERSONNE_2 = " + NUM1 + " and NUM_PERSONNE_1 = " + NUM2 + ")");
                boolean available = resultat.first();
                if(available){
                    String path = "FichiersTXT/" + Integer.toString(resultat.getInt("NUM_FORMULAIRE")) + ".txt";
                    Path p = Paths.get(path);
                    System.out.println("Chemin = " + path);
                    List<String> donnees = Files.readAllLines(p,Charset.forName("UTF-8"));
                    reponse += donnees.size() + "\r\n";
                    for(String str : donnees)
                        reponse += str + "\r\n";
                }
                else reponse = "0\r\n";
                resultat.close();
                state.close();
                this.connect.close();
                this.connect = null;
            }
            else reponse = "0\r\n";
        }
        else if(requete.startsWith("NEW_USER")){
            int debut = requete.indexOf(" ") + 1;
            int fin = requete.indexOf("-",debut);
            String id = requete.substring(debut, fin);
            this.getConnectionUtility(Serveur.urlfb, Serveur.userfb, Serveur.passwdfb);
            Statement state = this.connect.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet resultat = state.executeQuery("select NUM_CLIENT,ID_CLIENT from COMPTES where ID_CLIENT = '" + id + "'");
            boolean existe = resultat.first();
            if(!existe){
                debut = fin + 1;
                fin = requete.indexOf("-",debut);
                String surnom = requete.substring(debut,fin);
                resultat = state.executeQuery("select NUM_CLIENT,SURNOM_CLIENT from INFORMATIONS_CLIENT where SURNOM_CLIENT = '" + surnom + "'");
                existe = resultat.first();
                if(!existe){
                    debut = fin + 1;
                    fin = requete.indexOf("-",debut);
                    String nom = requete.substring(debut,fin);
                    debut = fin + 1;
                    fin = requete.indexOf("-",debut);
                    String prenom = requete.substring(debut,fin);
                    debut = fin + 1;
                    fin = requete.length();
                    String mdp = requete.substring(debut,fin);
                    resultat = state.executeQuery("select NUM_CLIENT from COMPTES");
                    existe = resultat.last();
                    int num = 1;
                    if(existe) num = resultat.getInt("NUM_CLIENT") + 1;
                    state.executeUpdate("insert into COMPTES (NUM_CLIENT,ID_CLIENT,PASS_CLIENT) values ("
                            + "'" + num + "'," 
                            + "'" + id + "',"
                            + "'" + mdp + "'"
                            + ")");
                    state.executeUpdate("insert into INFORMATIONS_CLIENT (NOM_CLIENT,PRENOM_CLIENT,SURNOM_CLIENT,NUM_CLIENT) values ("
                            + "'" + nom + "',"
                            + "'" + prenom + "',"
                            + "'" + surnom + "',"
                            + "'" + num + "'"
                            + ")");
                    reponse = "2";
                }
                else reponse = "0";
            }
            else reponse = "1";
            resultat.close();
            state.close();
            this.connect.close();
            this.connect = null;
        }
        else if(requete.startsWith("STOCK_MESSAGE")){
            int debut = requete.indexOf(" ") + 1;
            int fin = requete.indexOf("-",debut);
            String user1 = requete.substring(debut,fin);
            debut = fin + 1;
            String user2 = requete.substring(debut);
            int NUM1 = Serveur.getNumbAbonnee(user1);
            int NUM2 = Serveur.getNumbAbonnee(user2);
            this.getConnectionUtility(Serveur.urlfb, Serveur.userfb, Serveur.passwdfb);
            Statement state = this.connect.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet resultat = state.executeQuery("select * from FORMULAIRES where (NUM_PERSONNE_1 = " + NUM1 
                    + " and NUM_PERSONNE_2 = " + NUM2 + ") or (NUM_PERSONNE_2 = " + NUM1 + " and NUM_PERSONNE_1 = " + NUM2 + ")");
            boolean available = resultat.first();
            String path = "FichiersTXT/";
            int num = 1;
            if(available)
                num = resultat.getInt("NUM_FORMULAIRE");
            else {
                resultat = state.executeQuery("select NUM_FORMULAIRE from FORMULAIRES");
                boolean available2 = resultat.last();
                if(available2)
                    num = resultat.getInt("NUM_FORMULAIRE") + 1;
            }
            path += Integer.toString(num) + ".txt";
            reponse = path;
            // envoie de la reponse
            this.emetteurDeReponses.write(reponse);
            this.emetteurDeReponses.flush();
            String donnees = lireDonnees();
            reponse = "0";
            if(available){
                try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), Charset.forName("UTF-8"), StandardOpenOption.APPEND)){
                    bw.write("\r\n" + donnees, 0,(donnees + "\r\n").length());
                }
                reponse = "1";
            }
            else {
                state.executeUpdate("insert into FORMULAIRES (NUM_FORMULAIRE,NUM_PERSONNE_1,NUM_PERSONNE_2,DATE_FORMULAIRE) values ("
                        + "'" + num + "',"
                        + "'" + NUM1 + "',"
                        + "'" + NUM2 + "',"
                        + "CURRENT_DATE)");
                resultat.close();
                state.close();
                this.connect.close();
                this.connect = null;
                try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(path),Charset.forName("UTF-8"))){
                    bw.write(donnees, 0,donnees.length());
                }
                reponse = "1";
            }
        }
        else if(requete.startsWith("CLOSE")){
            // recherche de la personne dans la liste et la supprimer
            if(!requete.equals("CLOSE")){
                int debut = requete.indexOf(" ") + 1;
                String num = requete.substring(debut, requete.length());
                int indice = Serveur.getAbonne(Integer.parseInt(num));
                Abonne b = Serveur.listeClient.remove(indice);
            }
            this.occuperClient = false;
            this.emetteurDeReponses.close();
            this.recepteurRequetes.close();
            this.recepteurDonnees.close();
            this.client.close();
        }
        else System.out.println("Requete inconnu");
        return reponse;
    }
    
    public  void getConnectionUtility(String urlfb,String userfb,String passwdfb) throws ClassNotFoundException{
        if(connect == null){
            try {
                Class.forName("org.firebirdsql.jdbc.FBDriver");
                connect = DriverManager.getConnection(urlfb, userfb,passwdfb);
            } 
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
