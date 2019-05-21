package discussioninstantannee;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class Variables {
    public static int NUM_CLIENT = -1;
    public static Border bordure = BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.red, 
                        Color.blue,Color.yellow,Color.green),
                BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.red,
                        Color.blue,Color.yellow,Color.green));
}
