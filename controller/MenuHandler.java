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
    // menu simples: título e subtítulo (sem painel adicional)
    private final int panelW = 660;
    private final int panelH = 340;
    // contador para animação do título
    private int tick = 0;

    // removed unused fields titleScale and scaleIncreasing; animation is driven by 'tick'

    public void draw(Graphics2D g2){
        // fundo em gradiente
        GradientPaint menuBG = new GradientPaint(0, 0, Color.DARK_GRAY, 0, HEIGHT, Color.BLACK);
        Paint oldPaint = g2.getPaint();
        Composite oldComposite = g2.getComposite();
        g2.setPaint(menuBG);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setComposite(oldComposite);
        g2.setPaint(oldPaint);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // título central com animação de zoom pulsante
        String title = "Bem vindo ao Tetris";
        double pulse = 1.0 + Math.sin(tick * 0.08) * 0.08; // varia em torno de 1.0
        Font baseTitle = new Font("Verdana", Font.BOLD, 64);
        Font titleFont = baseTitle.deriveFont((float) (64 * pulse));
        g2.setFont(titleFont);
        FontMetrics fmTitle = g2.getFontMetrics(titleFont);
        int titleWidth = fmTitle.stringWidth(title);
        int titleX = (WIDTH - titleWidth) / 2;
        int titleY = HEIGHT / 3;

        // sombra
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(title, titleX + 4, titleY + 4);
        // cor principal do título
        g2.setColor(new Color(240, 240, 245));
        g2.drawString(title, titleX, titleY);

        // subtítulo abaixo do título
        Font subF = new Font("SansSerif", Font.PLAIN, 18);
        g2.setFont(subF);
        String subtitle = "Pressione Iniciar para começar a jogar";
        int subW = g2.getFontMetrics(subF).stringWidth(subtitle);
        g2.setColor(new Color(200, 200, 210));
        g2.drawString(subtitle, (WIDTH - subW) / 2, titleY + 36);

    }

    public void update()
    {
        // Animação de zoom pulsante
        tick++;
        // Força repaint externo para garantir animação fluida
    }

    // getters para permitir alinhamento de botões externos
    public int getPanelX() { return (WIDTH - panelW) / 2; }
    public int getPanelY() { return (HEIGHT - panelH) / 2; }
    public int getPanelW() { return panelW; }
    public int getPanelH() { return panelH; }

}
