package controller;

import javax.swing.*;

import view.GamePanel;

import java.awt.*;

public class Main {

    public static void main(String[]args){

    JFrame window = new JFrame("Tetris");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        KeyHandler kh = new KeyHandler();
        GamePanel gp = new GamePanel(kh);

        kh.addObserver(gp);
        window.add(gp);
        window.pack();

        window.getContentPane().setBackground(Color.DARK_GRAY);

        gp.LaunchGame();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

    }
}
