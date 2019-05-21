/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 *
 * @author simo996
 */
public class Formulaire extends JPanel{
    private JComponent comp;
    public Formulaire(Dimension dim,Border b,String str,JComponent cmp){
        this.setPreferredSize(dim);
        this.setBorder(b);
        this.setBackground(Color.white);
        this.initComponents(str, cmp);
    }
    public Formulaire(Dimension dim1,Dimension dim2,Border b,String str,JComponent cmp){
        this.setPreferredSize(dim1);
        this.setBorder(b);
        this.setBackground(Color.white);
        this.initComponents(str,dim2,cmp);
    }
    private void initComponents(String str,JComponent cmp){
        JLabel label = new JLabel(str + ":");
        label.setPreferredSize(new Dimension(100,30));
        label.setFont(new Font("Arial",Font.BOLD,14));
        this.comp = cmp;
        this.add(label);
        this.add(this.comp);
    }
    private void initComponents(String str,Dimension dim,JComponent cmp){
        JLabel label = new JLabel(str + ":");
        label.setPreferredSize(dim);
        label.setFont(new Font("Arial",Font.BOLD,14));
        this.comp = cmp;
        this.add(label);
        this.add(this.comp);
    }
}
