package model;

import java.awt.*;

public class Block extends Rectangle {

    public int x;
    public int y;
    public static int SIZE = 28; // Aumentado para deixar o tabuleiro maior (altura total ~ 28*20 = 560 dentro dos 620px)


    public Color c;

    public Block(Color c){
        this.c=c;

    }

    public void draw(Graphics2D g2){
        int margin =2;
        g2.setColor(c);
        g2.fillRect(x+margin,y+margin,SIZE - (margin*2),SIZE - (margin*2));


    }
}
