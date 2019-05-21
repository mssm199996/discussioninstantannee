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
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author simo996
 */
public class FenetreEnregistrement extends JFrame{
    private ChampTexte champText[];
    private JButton confirmer,annuler;
    public FenetreEnregistrement(){
        this.initWindow();
        this.initComponents();
    }
    private void initWindow(){
        this.setSize(400,310);
        this.setLocationRelativeTo(null);
        this.setTitle("Nouvel enregistrement");
    }
    private void initComponents(){
        JPanel contentPane = new JPanel(),pCenter = new JPanel(),pSouth = new JPanel();
        contentPane.setLayout(new BorderLayout());
        pCenter.setBackground(Color.white);
        pSouth.setBackground(Color.white);
        this.champText = new ChampTexte[5];
        for(int i = 0; i < this.champText.length; i++){
            this.champText[i] = new ChampTexte(new Dimension(200,25));
            String caption = "";
            Color c = Color.black;
            switch(i){
                case 0: 
                    caption = "ID"; 
                    c = Color.red;
                    break;
                case 1: 
                    caption = "MDP"; 
                    c = Color.blue;
                    break;
                case 2: 
                    caption = "Nom"; 
                    c = Color.green;
                    break;
                case 3: 
                    caption = "Prenom"; 
                    c = Color.yellow;
                    break;
                case 4: 
                    caption = "Surnom"; 
                    c = Color.magenta;
                    break;
            }
            pCenter.add(new Formulaire(new Dimension(380,40),new Dimension(100,25),BorderFactory.createLineBorder(c, 3, true)
                    ,caption,this.champText[i]));
        }
        this.confirmer = new JButton("Confirmer");
        this.confirmer.setPreferredSize(new Dimension(180,35));
        this.confirmer.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               try{
                   newAbonnee();
               }
               catch(IOException | NullPointerException exp){
                   exp.printStackTrace();
               }
           } 
        });
        this.annuler = new JButton("Annuler");
        this.annuler.setPreferredSize(new Dimension(180,35));
        this.annuler.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               setVisible(false);
           } 
        });
        pSouth.add(this.confirmer);
        pSouth.add(this.annuler);
        contentPane.add(pCenter,BorderLayout.CENTER);
        contentPane.add(pSouth,BorderLayout.SOUTH);
        this.setContentPane(contentPane);
    }
    private void newAbonnee() throws NullPointerException,IOException{
        boolean creer = true;
        int i = 0;
        while((creer) && (i < this.champText.length)){
            if(this.champText[i].getText().equals(""))
                creer = false;
            i++;
        }
        if(creer){
            String id = this.champText[0].getText();
            String mdp = this.champText[1].getText();
            String nom = this.champText[2].getText();
            String prenom = this.champText[3].getText();
            String surnom = this.champText[4].getText();
            FenetreConnexion.envoyerRequete("NEW_USER", id + "-" + surnom + "-" + nom + "-" + prenom + "-" + mdp);
            String reponse = FenetreConnexion.lireReponse();
            JOptionPane p = new JOptionPane();
            switch(reponse){
                case "0":
                    p.showMessageDialog(null, "Ce surnom est déja utilisé, veuillez en choisir un autre", "Nickname error...", JOptionPane.ERROR_MESSAGE);
                    break;
                case "1":
                    p.showMessageDialog(null, "Cet identifiant est déja utilisé, veuillez en choisir un autre", "ID error...", JOptionPane.ERROR_MESSAGE);
                    break;
                case "2":
                    p.showMessageDialog(null, "Vous vous êtes bien enregistré(e), soyez le bienvenu", "Bravo !...", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    break;
            }
        }
        else {
            JOptionPane p = new JOptionPane();
            p.showMessageDialog(null, "Veuillez remplir tout les champs", "Erreur...", JOptionPane.ERROR_MESSAGE);
        }
    }
}
