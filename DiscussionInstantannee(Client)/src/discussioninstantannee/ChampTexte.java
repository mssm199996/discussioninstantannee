/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discussioninstantannee;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/**
 *
 * @author simo996
 */
public class ChampTexte extends JTextField{
    public ChampTexte(Dimension dim){
        this.setFont(new Font("Arial",Font.BOLD,14));
        this.setBorder(BorderFactory.createEtchedBorder(5, Color.blue, Color.red));
        this.setHorizontalAlignment(JTextField.HORIZONTAL);
        this.setPreferredSize(dim);
    }
}
