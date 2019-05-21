/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javafx.scene.input.KeyCode;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author simo996
 */
public class FenetreMessagerie extends JFrame{
    private DefaultListModel<String> listeAmis;
    private ChampTexteRiche afficheur,ecrivain;
    private String pseudo,correspondant = null;
    private Timer Rafraichisseur;
    private boolean newMessage,getMessages;
    public FenetreMessagerie(String str){
        this.pseudo = str;
        this.initWindow(str);
        this.initVariables();
        this.initComponent();
        this.Rafraichisseur.start();
        this.setVisible(true);
    }
    private void initWindow(String str){
        this.setTitle("MSSM-Chat (" + str + ")");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter(){
           public void windowClosing(WindowEvent evt){
               if(JOptionPane.showConfirmDialog(null, 
                    "Voulez vous vraiment quitter MSSM-Chat ?", "Confirmation de deconnexion", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                        FenetreConnexion.envoyerRequete("CLOSE",Integer.toString(Variables.NUM_CLIENT));
                        System.exit(0);
                } 
           }
        });
    }
    private void initVariables(){
        this.newMessage = false;
        this.getMessages = true;
        this.listeAmis = new DefaultListModel<String>();
        this.Rafraichisseur = new Timer(250,new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.out.println("----------------------------------------- Rafraichissement -----------------------------------------");
                try{
                    String reponse = "";
                    // Disscussion courante
                    if(correspondant != null){
                        if(newMessage){
                            String donnees = ecrivain.getText() + "\r\n";
                            ecrivain.setText("");
                            FenetreConnexion.envoyerRequete("STOCK_MESSAGE",pseudo + "-" + correspondant);
                            reponse = FenetreConnexion.lireReponse();
                            FenetreConnexion.envoyerRequete(donnees,"");
                            ecrivain.repaint();
                            newMessage = false;
                            reponse = FenetreConnexion.lireReponse();
                            if(reponse.equals("0")){
                                JOptionPane p = new JOptionPane();
                                p.showMessageDialog(null, "Le message n'a pas été envoyé à destination, veuillez réessayer", "Erreur d'envoi", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        if(getMessages){
                            FenetreConnexion.envoyerRequete("GET_NEW_MESSAGES", pseudo + "-" + correspondant);
                            reponse = FenetreConnexion.lireDonnees();
                            if(reponse.equals("")){
                                afficheur.setCorrespondantLabel("Aucun correspondant selectionné");
                                afficheur.setText("");
                            }
                            else {
                                afficheur.setText(reponse);
                            }
                        }
                    }
                    // Membres en ligne
                    FenetreConnexion.envoyerRequete("ON_MEMBERS","");
                    reponse = FenetreConnexion.lireReponse();
                    listeAmis.clear();
                    int debut = 0;
                    while(debut < reponse.length()){
                        int fin = reponse.indexOf("|",debut);
                        String str = reponse.substring(debut, fin);
                        if(!str.equals(pseudo))
                            listeAmis.addElement(str);
                        debut = fin + 1;
                    }
                }
                catch(IOException exp){
                    exp.printStackTrace();
                }
            }
        });
    }
    private void initComponent(){
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int)dim.getHeight();
        int width = (int)dim.getWidth();
        this.afficheur = new ChampTexteRiche(new Dimension(2*width/3,2*height/3),"Aucun correspondant selectionné",false,new Color(255,250,140),Color.green);
        this.ecrivain = new ChampTexteRiche(new Dimension(2*width/3,height/6),"Ecriture",true,new Color(140,247,255),Color.red);
        Panneau content = new Panneau(Variables.bordure);
        content.add(new Liste(this.listeAmis),BorderLayout.EAST);
        content.add(this.ecrivain,BorderLayout.SOUTH);
        content.add(this.afficheur,BorderLayout.CENTER);
        this.setContentPane(content);
    }
    class Liste extends JPanel{
        private JList liste;
        private ChampTexte recherche;
        public Liste(DefaultListModel dlm){
            this.setBackground(Color.white);
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue, 2, true), 
                    "Liste des ami(e)s", TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.CENTER, new Font("Arial",Font.BOLD,14)));
            this.setLayout(new BorderLayout());
            this.initComponents(dlm);
        }
        private void initComponents(DefaultListModel dlm){
            this.liste = new JList(dlm);
            this.liste.setBorder(BorderFactory.createEtchedBorder(2,Color.black,Color.red));
            this.liste.setBackground(new Color(255,191,233));
            this.liste.addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    if(getSelectedElement() != null){
                        correspondant = getSelectedElement();
                        afficheur.setCorrespondantLabel("Chat avec: " + correspondant);
                    }
                }
            });
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int height = (int)dim.getHeight();
            int width = (int)dim.getWidth();
            this.liste.setPreferredSize(new Dimension(width/6,height));
            this.recherche = new ChampTexte(new Dimension(150,25));
            Panneau pNorth = new Panneau(),pCenter = new Panneau();
            pNorth.add(new Formulaire(new Dimension(250,40),new Dimension(90,40),null,"Rechercher",this.recherche),BorderLayout.CENTER);
            pCenter.add(this.liste,BorderLayout.CENTER);
            this.add(pNorth,BorderLayout.NORTH);
            this.add(pCenter,BorderLayout.CENTER);
        }
        private String getSelectedElement(){
            return (String)this.liste.getSelectedValue();
        }
    }
    class Panneau extends JPanel{
        public Panneau(){
            this.setBackground(Color.WHITE);
            this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.setLayout(new BorderLayout());
        }
        public Panneau(Border b){
            this.setBackground(Color.WHITE);
            this.setBorder(b);
            this.setLayout(new BorderLayout());
        }
    }
    class ChampTexteRiche extends JPanel{
        private JTextArea jta;
        public ChampTexteRiche(Dimension dim,String str,boolean b,Color c1,Color c2){
            this.setBackground(Color.white);
            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(c2, 2, true), 
                    str, TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.CENTER, new Font("Arial",Font.BOLD,14)));
            this.setPreferredSize(dim);
            this.setLayout(new BorderLayout());
            this.initComponents(b,c1);
        }
        private void initComponents(boolean b,Color c){
            this.jta = new JTextArea();
            this.jta.setBackground(c);
            this.jta.setEditable(b);
            this.jta.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            this.jta.setLineWrap(true);
            this.jta.setFont(new Font("Arial",Font.BOLD,12));
            this.jta.addKeyListener(new EnvoyeurDeMessage());
            JScrollPane p = new JScrollPane(this.jta);
            p.getVerticalScrollBar().addMouseListener(new MouseListener(){
                public void mouseClicked(MouseEvent e) {
                }
                public void mousePressed(MouseEvent e) {
                    getMessages = false;
                }
                public void mouseReleased(MouseEvent e) {
                    getMessages = true;
                }
                public void mouseEntered(MouseEvent e) {
                }
                public void mouseExited(MouseEvent e) {
                }
            });
            this.add(p,BorderLayout.CENTER);
        }
        public String getText(){
            return this.jta.getText();
        }
        
        public void AddText(String str){
            this.jta.setText(this.getText() + "\r\n" + str);
        }
        public void setText(String str){
            this.jta.setText(str);
        }
        public void setCorrespondantLabel(String str){
            TitledBorder b = (TitledBorder)this.getBorder();
            b.setTitle(str);
            this.repaint();
        }
    }
    class EnvoyeurDeMessage implements KeyListener{
        public void keyTyped(KeyEvent e) {
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                if(correspondant != null)
                    newMessage = true;
                else {
                    JOptionPane p = new JOptionPane();
                    p.showMessageDialog(null, "Veuillez d'abords selectionner un correspondant", "Erreur...", JOptionPane.ERROR_MESSAGE);
                    try{
                        Robot b = new Robot();
                        b.keyPress(KeyEvent.VK_BACK_SPACE);
                    }
                    catch(AWTException exp){
                        exp.printStackTrace();
                    }
                }
            }
        }
        public void keyReleased(KeyEvent e) {
        }
    }
}
