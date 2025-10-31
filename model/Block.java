package model;

import java.awt.*;

public class Block extends Rectangle {

    public int x;
    public int y;
    public static int SIZE = 28;

    public Color c;

    private static boolean pastelColorMode = false;

    public static void setPastelColorMode(boolean pastel) {
        pastelColorMode = pastel;
    }

    private Color getPastelColor(Color original) {
        if (original.equals(Color.orange))   return new Color(255, 183, 94);
        if (original.equals(Color.yellow))   return new Color(255, 255, 180);
        if (original.equals(Color.red))      return new Color(255, 160, 160);
        if (original.equals(Color.blue))     return new Color(173, 216, 230);
        if (original.equals(Color.cyan))     return new Color(180, 255, 255);
        if (original.equals(Color.green))    return new Color(170, 255, 170);
        if (original.equals(Color.magenta))  return new Color(255, 180, 255);
        return new Color(220, 220, 220);
    }

    public void draw(Graphics2D g2){
        int margin = 2;
        Color drawColor = pastelColorMode ? getPastelColor(c) : c;
        g2.setColor(drawColor);
        g2.fillRect(x + margin, y + margin, SIZE - (margin * 2), SIZE - (margin * 2));
    }

    public Block() {
        // Construtor padrão, não faz nada
    }

    public Block(Color c) {
        this.c = c;
    }
}
