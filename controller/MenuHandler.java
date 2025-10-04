package controller;


import java.awt.*;
import java.awt.Graphics2D;


public class MenuHandler {

    public static final int WIDTH = 1150;
    public static final int HEIGHT = 620;

    public static int right_x;
    public static int left_x;
    public static int top_y;
    public static int bottom_y;

    public void draw(Graphics2D g2){
    GradientPaint menuBG = new GradientPaint(0, 0, Color.DARK_GRAY, 0, HEIGHT, Color.BLACK);
    Paint oldPaint = g2.getPaint();
    Composite oldComposite = g2.getComposite();
    g2.setPaint(menuBG);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
    g2.fillRect(0, 0, WIDTH, HEIGHT);
    g2.setComposite(oldComposite);
    g2.setPaint(oldPaint);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.setColor(Color.WHITE);
    Font titleFont = new Font("Verdana", Font.BOLD, 64);
    g2.setFont(titleFont);
    String title = "Bem vindo ao Tetris";
    int titleWidth = g2.getFontMetrics().stringWidth(title);
    g2.drawString(title, (WIDTH - titleWidth) / 2, HEIGHT / 3);

    }

    public void update()
    {

    }
}
