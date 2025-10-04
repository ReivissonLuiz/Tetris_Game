package view;

import javax.swing.*;

import model.PlayManager;
import model.PowerUpObserver;
import controller.KeyHandler;
import controller.MenuHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GamePanel extends JPanel implements Runnable, PowerUpObserver {

    public static  final int WIDTH=1150;
    public static  final int HEIGHT=620;

    final int FPS = 60; 
    Thread GameThread; 

    PlayManager pm = new PlayManager();
    MenuHandler mh = new MenuHandler();
    public static Sound music = new Sound();
    public static Sound se = new Sound();

    private JButton startBtn;
    private JButton musicBtn;
    private JButton restartBtn;
    private JButton quitBtn;
    private JButton ctrlLeftBtn;
    private JButton ctrlDownBtn;
    private JButton ctrlRotateBtn;
    private JButton ctrlRightBtn;
    private BufferedImage menuBackground;

    public static Boolean powerupused;

    public static int PowerupCounter;

    public static Boolean powerupInProgress;



    public GamePanel(KeyHandler kh) {
        powerupused = false;
        powerupInProgress = false;
        this.setPreferredSize(new Dimension(WIDTH,HEIGHT));
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        this.addKeyListener(kh);
        this.setFocusable(true);

        // criar botão Iniciar
        startBtn = new JButton("Iniciar");
        startBtn.setBounds((WIDTH - 200) / 2, (HEIGHT / 2) - 40, 200, 50);
        startBtn.setFocusable(false);
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                KeyHandler.gamestart = true;
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
                // garantir que a música comece se musicOn já estiver true
                if (KeyHandler.musicOn && !GamePanel.music.isMusicPlaying()) {
                    GamePanel.music.play(0, true);
                    GamePanel.music.loop();
                }
                GamePanel.this.requestFocusInWindow();
            }
        });
        this.add(startBtn);

        // botão Reiniciar (usado na tela de game over)
        restartBtn = new JButton("Novo jogo");
        restartBtn.setBounds((WIDTH - 340) / 2, (HEIGHT / 2) + 120, 160, 40);
        restartBtn.setFocusable(false);
        restartBtn.setVisible(false);
        restartBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
        // ... (setupHoldAction will wire o comportamento de press-and-hold e hover)
                GamePanel.this.requestFocusInWindow();
                if (KeyHandler.musicOn && !GamePanel.music.isMusicPlaying()) {
                    GamePanel.music.play(0, true);
                    GamePanel.music.loop();
                }
            }
        });
        this.add(restartBtn);

        // botão Sair (aparece na tela de game over)
        quitBtn = new JButton("Sair");
        quitBtn.setBounds((WIDTH - 340) / 2 + 180, (HEIGHT / 2) + 120, 160, 40);
        quitBtn.setFocusable(false);
        quitBtn.setVisible(false);
        quitBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                System.exit(0);
            }
        });
        this.add(quitBtn);

        // criar botão de música abaixo do botão iniciar
        musicBtn = new JButton(KeyHandler.musicOn ? "Música: On" : "Música: Off");
        musicBtn.setBounds((WIDTH - 130) / 2, (HEIGHT / 2) + 20, 130, 30);
        musicBtn.setFocusable(false);
        musicBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                if (KeyHandler.musicOn) {
                    GamePanel.music.stop();
                    KeyHandler.musicOn = false;
                    musicBtn.setText("Música: Off");
                } else {
                    if(!GamePanel.music.isMusicPlaying()){
                        GamePanel.music.play(0, true);
                        GamePanel.music.loop();
                    }
                    KeyHandler.musicOn = true;
                    musicBtn.setText("Música: On");
                }
                // manter foco no painel
                GamePanel.this.requestFocusInWindow();
            }
        });
        this.add(musicBtn);

        // tentar carregar a imagem de fundo do menu (várias estratégias)
        menuBackground = null;
        try {
            java.net.URL url = getClass().getResource("menu_bg.jpg");
            if (url != null) {
                menuBackground = ImageIO.read(url);
            } else {
                url = getClass().getResource("/view/menu_bg.jpg");
                if (url != null) {
                    menuBackground = ImageIO.read(url);
                } else {
                    // tentar via ClassLoader
                    java.net.URL clUrl = Thread.currentThread().getContextClassLoader().getResource("view/menu_bg.jpg");
                    if (clUrl != null) {
                        menuBackground = ImageIO.read(clUrl);
                    } else {
                        // tentar arquivo relativo ao diretório atual do processo
                        String userDir = System.getProperty("user.dir");
                        java.io.File f1 = new java.io.File("view/menu_bg.jpg");
                        java.io.File f2 = new java.io.File(userDir + java.io.File.separator + "view" + java.io.File.separator + "menu_bg.jpg");
                        if (f1.exists()) {
                            menuBackground = ImageIO.read(f1);
                        } else if (f2.exists()) {
                            menuBackground = ImageIO.read(f2);
                        } else {
                        }
                    }
                }
            }
        } catch (IOException ex) {
            menuBackground = null;
        }

        // controles de toque (painel direito, centralizados dentro do painel direito)
        ctrlLeftBtn = new JButton("◄");
        ctrlDownBtn = new JButton("▼");
        ctrlRotateBtn = new JButton("⟳");
        ctrlRightBtn = new JButton("►");
        JButton[] ctrls = new JButton[] {ctrlLeftBtn, ctrlDownBtn, ctrlRotateBtn, ctrlRightBtn};
        for (JButton b : ctrls) {
            b.setFocusable(false);
            b.setVisible(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 18));
            // estilo: fundo escuro, texto claro, borda arredondada
            b.setBackground(new Color(40, 44, 50));
            b.setForeground(new Color(230, 230, 230));
            b.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 110)));
            this.add(b);
        }

        // listeners: apenas definem as flags do KeyHandler (o Mino consumirá as flags no próximo update)
        // implementar press-and-hold: Timer dispara ação repetida enquanto o botão for mantido pressionado
        // todos os botões: ação única por clique
        ctrlLeftBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.KeyHandler.leftPressed = true;
                GamePanel.this.requestFocusInWindow();
            }
        });
        ctrlDownBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.KeyHandler.downPressed = true;
                GamePanel.this.requestFocusInWindow();
            }
        });
        // girar: apenas uma ação por clique
        ctrlRotateBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.KeyHandler.upPressed = true;
                GamePanel.this.requestFocusInWindow();
            }
        });
        ctrlRightBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.KeyHandler.rightPressed = true;
                GamePanel.this.requestFocusInWindow();
            }
        });

    }

    


    public void LaunchGame(){
        GameThread = new Thread(this);
        GameThread.start();

    }

    @Override
    public void run() {

        double drawInterval = (double) 1000000000 /FPS;
        double delta =0;

        long lastTime = System.nanoTime();
        long currentTime;
        double poweruptimedelta = 0;

        long currentPowerupTime;
        long lastPowerUpTime = System.currentTimeMillis();

        int powerupCounter = 0;

        int powerupInProgressCounter = 0;

        while(GameThread != null){

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >=1){
                update();
                repaint();
                delta--;
            }

            currentPowerupTime = System.currentTimeMillis();

            poweruptimedelta += (currentPowerupTime - lastPowerUpTime)/1000F;

            lastPowerUpTime = currentPowerupTime;

            if (poweruptimedelta >= 1){

                poweruptimedelta--;

                if(powerupused && powerupCounter >= 20){
                    powerupused = false;
                    powerupCounter = 0;
                }
                else if(powerupused && powerupCounter < 20){
                    powerupCounter++;
                }

                if(powerupused && powerupInProgress){
                    powerupInProgressCounter++;
                }

                if(powerupInProgress && powerupInProgressCounter >= 10){
                    powerupInProgress = false;
                    PlayManager.dropInterval -= 40;
                    powerupInProgressCounter = 0;
                }
            }

        }
    }

    private void update() {
        // mostrar/ocultar botões do menu inicial conforme estado do jogo
        if (startBtn != null && musicBtn != null && restartBtn != null && quitBtn != null) {
            boolean inMenu = !KeyHandler.gamestart && !pm.gameOver;
            startBtn.setVisible(inMenu);
            musicBtn.setVisible(inMenu);
            // quando game over, mostrar restart/quit
            if (pm.gameOver) {
                restartBtn.setVisible(true);
                quitBtn.setVisible(true);
                // esconder botões do menu
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
                // reposicionar botões para ficarem sempre abaixo da pontuação
                int btnY = pm.getButtonsY();
                int btnW = 160;
                int gap = 20;
                int leftX = (WIDTH - (btnW * 2 + gap)) / 2;
                restartBtn.setBounds(leftX, btnY, btnW, 40);
                quitBtn.setBounds(leftX + btnW + gap, btnY, btnW, 40);
            } else {
                restartBtn.setVisible(false);
                quitBtn.setVisible(false);
            }
        }

        // posicionamento e visibilidade dos botões de controle — centralizados dentro do painel direito
        int btnW = 48;
        int btnH = 48;
        int gap = 8;
        int totalW = btnW * 4 + gap * 3;
        // usar as coordenadas do painel direito fornecidas pelo PlayManager
        int rpX = pm.getRightPanelX();
        int rpY = pm.getRightPanelY();
        int rpW = pm.getRightPanelW();
        int rpH = pm.getRightPanelH();
        int baseX = rpX + Math.max(0, (rpW - totalW) / 2);
        int baseY = rpY + rpH - btnH - 24; // 24px acima da base do painel
        if (KeyHandler.gamestart && !pm.gameOver) {
            ctrlLeftBtn.setVisible(true);
            ctrlDownBtn.setVisible(true);
            ctrlRotateBtn.setVisible(true);
            ctrlRightBtn.setVisible(true);
            ctrlLeftBtn.setBounds(baseX, baseY, btnW, btnH);
            ctrlDownBtn.setBounds(baseX + (btnW + gap), baseY, btnW, btnH);
            ctrlRotateBtn.setBounds(baseX + (btnW + gap) * 2, baseY, btnW, btnH);
            ctrlRightBtn.setBounds(baseX + (btnW + gap) * 3, baseY, btnW, btnH);
        } else {
            ctrlLeftBtn.setVisible(false);
            ctrlDownBtn.setVisible(false);
            ctrlRotateBtn.setVisible(false);
            ctrlRightBtn.setVisible(false);
        }

        if (!KeyHandler.gamestart) {
            mh.update();
        }
        if (KeyHandler.gamequit)
        {
            System.exit(0);
        }
        else if(!KeyHandler.pausePressed && !pm.gameOver)
        {
        pm.update();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        if (!KeyHandler.gamestart)
        {
            // desenhar imagem de fundo do menu se disponível
            if (menuBackground != null) {
                g2.drawImage(menuBackground, 0, 0, WIDTH, HEIGHT, null);
            }
            mh.draw(g2);
        }
        else {
            // desenhar a mesma imagem de fundo e aplicar o gradiente semi-transparente
            if (menuBackground != null) {
                g2.drawImage(menuBackground, 0, 0, WIDTH, HEIGHT, null);
            }
            // aplicar gradiente igual ao menu (composite restaurado depois)
            Paint oldPaint = g2.getPaint();
            Composite oldComposite = g2.getComposite();
            GradientPaint menuBG = new GradientPaint(0, 0, Color.DARK_GRAY, 0, HEIGHT, Color.BLACK);
            g2.setPaint(menuBG);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setComposite(oldComposite);
            g2.setPaint(oldPaint);

            pm.draw(g2);
            }

    }

    @Override
    public void PowerUpUpdate() {
       if(!powerupused){
           PlayManager.dropInterval += 40;
           System.out.println(PlayManager.dropInterval);
           powerupused = true;
           powerupInProgress = true;
           PowerupCounter = 0;
       }
       else {
       }
    }
}
