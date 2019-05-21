/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *
 * @author simo996
 */
public class FenetreConnexion extends JFrame {
    private final String destinataire = "41.103.214.63";
    private final int portConnexion = 43215;
    public static Socket connecteur;
    public static PrintWriter emetteurRequetes;
    public static BufferedInputStream recepteurReponses;
    public static BufferedReader recepteurDonnees;
    private FenetreMessagerie fm;
    private FenetreEnregistrement fe;
    private ChampTexte champID;
    private ChampMDP champMDP;
    private JButton confirmer,inscription,annuler;
    public FenetreConnexion(){
        try{
            connecteur = new Socket(this.destinataire,this.portConnexion);
            emetteurRequetes = new PrintWriter(connecteur.getOutputStream());
            recepteurReponses = new BufferedInputStream(connecteur.getInputStream());
            recepteurDonnees = new BufferedReader(new InputStreamReader(connecteur.getInputStream()));
        }
        catch(ConnectException exp){
            exp.printStackTrace();
            JOptionPane p = new JOptionPane();
            p.showMessageDialog(null,"Le serveur est temporairement indisponible, veuillez réessayer ultérieurement","Erreur de connexion",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        catch(UnknownHostException exp){
            exp.printStackTrace();
        }
        catch(IOException exp){
            exp.printStackTrace();
        }

        this.initWindow();
        this.initComponent();
    }
    private void initWindow(){
        this.setTitle("MSSM-Chat Connexion");
        this.setSize(400, 180);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }
    private void initComponent(){
        this.champID =  new ChampTexte(new Dimension(200,30));
        this.champID.addKeyListener(new verifierInfosText());
        this.champMDP = new ChampMDP();
        this.champMDP.addKeyListener(new verifierInfosText());
        this.inscription = new JButton("S'enregistrer");
        this.inscription.setPreferredSize(new Dimension(120,30));
        this.inscription.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                fe = new FenetreEnregistrement();
                fe.setVisible(true);
            }
        });
        this.confirmer = new JButton("Connexion");
        this.confirmer.setPreferredSize(new Dimension(120,30));
        this.confirmer.addActionListener(new verifierInfosBouton());
        this.annuler = new JButton("Annuler");
        this.annuler.setPreferredSize(new Dimension(120,30));
        this.annuler.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               try{
                    envoyerRequete("CLOSE","");
                    connecteur.close();
                    emetteurRequetes.close();
                    recepteurReponses.close();
                    recepteurDonnees.close();
               }
               catch(IOException | NullPointerException exp){
                   exp.printStackTrace();
               }
               System.exit(0);
           } 
        });
        JPanel content = new JPanel(),pCenter = new JPanel(),pSouth = new JPanel();
        content.setBackground(Color.white);
        pCenter.setBackground(Color.white);
        pSouth.setBackground(Color.white);
        pCenter.add(new Formulaire(new Dimension(375,50),Variables.bordure,"ID",this.champID));
        pCenter.add(new Formulaire(new Dimension(375,50),Variables.bordure,"MDP",this.champMDP));
        pSouth.add(this.confirmer);
        pSouth.add(this.inscription);
        pSouth.add(this.annuler);
        content.setLayout(new BorderLayout());
        content.add(pCenter,BorderLayout.CENTER);
        content.add(pSouth,BorderLayout.SOUTH);
        this.setContentPane(content);
    }
    private void connexion() throws IOException{
        String user = champID.getText();
        String mdp = champMDP.getMDP();
        envoyerRequete("CHECK_ID_PASS",user + " " + mdp);
        String reponse = lireReponse();
        System.out.println("Reponse = " + reponse);
        if(!reponse.equals("0")){
            // L'utilisateur existe
            int k = reponse.indexOf(" ");
            int num = Integer.parseInt(reponse.substring(0,k));
            k++;
            fm = new FenetreMessagerie(reponse.substring(k));
            setVisible(false);
            Variables.NUM_CLIENT = num;
            System.out.println("Client = " + Variables.NUM_CLIENT + " est connecté");
            fm.setVisible(true);
        }
        else {
            JOptionPane option = new JOptionPane();
            option.showMessageDialog(null,"Nom d'utilisateur ou mot de passe inconnu", 
            "Erreur d'authentification", JOptionPane.OK_OPTION);
        }
    }
    class verifierInfosBouton implements ActionListener{
        public void actionPerformed(ActionEvent evt){
            try{
                connexion();
            }
            catch(NullPointerException exp){
                JOptionPane option = new JOptionPane();
                option.showMessageDialog(null,"Nom d'utilisateur ou mot de passe inconnu/Echec de la connexion au serveur", 
                    "Erreur d'authentification", JOptionPane.OK_OPTION);
            }
            catch(IOException exp){
                exp.printStackTrace();
            }
        }
    }
    class verifierInfosText implements KeyListener{
        public void keyTyped(KeyEvent e) {
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                try{
                    connexion();
                }
                catch(NullPointerException exp){
                    JOptionPane option = new JOptionPane();
                    option.showMessageDialog(null,"Nom d'utilisateur ou mot de passe inconnu/Echec de la connexion au serveur", 
                        "Erreur d'authentification", JOptionPane.OK_OPTION);
                }
                catch(IOException exp){
                    exp.printStackTrace();
                }
            }
        }
        public void keyReleased(KeyEvent e) {
        }
    }
    class ChampMDP extends JPasswordField{
        public ChampMDP(){
            this.setPreferredSize(new Dimension(200,30));
            this.setHorizontalAlignment(JPasswordField.CENTER);
            this.setBorder(BorderFactory.createEtchedBorder(5, Color.blue, Color.red));
        }
        public String getMDP() throws NullPointerException{
            String str = "";
            char[] tChar = this.getPassword();
            for(char c : tChar)
                str += (char)c;
            return str;
        }
    }
    public static void envoyerRequete(String requete,String message) throws NullPointerException{
        if(message.equals(""))
            emetteurRequetes.write(requete);
        else emetteurRequetes.write(requete + " " + message);
        emetteurRequetes.flush();
    }
    public static String lireReponse() throws IOException,NullPointerException{
        String reponse = "";
        byte[] flux = new byte[1024];
        int i = recepteurReponses.read(flux);
        reponse += new String(flux,0,i);
        return reponse;
    }
    public static String lireDonnees() throws IOException,NullPointerException{
        String str = recepteurDonnees.readLine();
        int nbLignes = Integer.parseInt(str);
        String reponse = "";
        for(int i = 0; i < nbLignes; i++){
            str = recepteurDonnees.readLine();
            if(!str.equals("") && (str != null))
                reponse += str + "\r\n";
        }
        return reponse;
    }
}
