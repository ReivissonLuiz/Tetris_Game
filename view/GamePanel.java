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
    private JButton pauseBtn;
    private JButton profileBtn;
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
    // bounds serão definidos em update() para alinhar com o painel do menu
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
    // bounds serão definidos em update() para alinhar com o painel do menu
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
            // estilo preparado para imagem: sem fundo, sem borda, com cursor de mão
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setOpaque(false);
            b.setForeground(new Color(230, 230, 230));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            this.add(b);
        }

        // botões de canto: pause (esq) e profile (dir) — estilo igual aos botões de controle
        pauseBtn = new JButton();
        profileBtn = new JButton();
        JButton[] cornerBtns = new JButton[] { pauseBtn, profileBtn };
        for (JButton b : cornerBtns) {
            b.setFocusable(false);
            b.setVisible(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 18));
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setOpaque(false);
            b.setForeground(new Color(230, 230, 230));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            this.add(b);
        }

        setControlButtonIcon(pauseBtn, "pause.png");
        setControlButtonIcon(profileBtn, "profile.png");

        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                KeyHandler.pausePressed = !KeyHandler.pausePressed;
                if (KeyHandler.pausePressed) GamePanel.music.stop(); else if(KeyHandler.musicOn) { GamePanel.music.play(0,true); GamePanel.music.loop(); }
                GamePanel.this.requestFocusInWindow();
            }
        });

        profileBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                // placeholder: abrir diálogo de perfil
                JOptionPane.showMessageDialog(GamePanel.this, "Área de perfil (placeholder)");
            }
        });

        // tentar carregar ícones padrão (se houver arquivos em view/)
        // nomes esperados: view/ctrl_left.png, view/ctrl_down.png, view/ctrl_rotate.png, view/ctrl_right.png
        setControlButtonIcon(ctrlLeftBtn, "ctrl_left.png");
        setControlButtonIcon(ctrlDownBtn, "ctrl_down.png");
        setControlButtonIcon(ctrlRotateBtn, "ctrl_rotate.png");
        setControlButtonIcon(ctrlRightBtn, "ctrl_right.png");

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

    // helper: tenta carregar um ícone a partir de /view/ ou do diretório local view/
    private void setControlButtonIcon(JButton btn, String filename) {
        try {
            Image img = null;
            String loadedFrom = null;
            // tentar resource no mesmo pacote
            java.net.URL url = getClass().getResource(filename);
            if (url == null) url = getClass().getResource("/view/" + filename);
            if (url != null) {
                img = ImageIO.read(url);
                loadedFrom = url.toString();
            } else {
                // tentar arquivo relativo à pasta do projeto
                java.io.File f = new java.io.File("view" + java.io.File.separator + filename);
                if (f.exists()) {
                    img = ImageIO.read(f);
                    loadedFrom = f.getAbsolutePath();
                }
            }
            if (img != null) {
                // log simples para ajudar a diagnosticar qualidade: origem e dimensões
                int ow = img.getWidth(null);
                int oh = img.getHeight(null);
                System.out.println("[IconLoad] " + filename + " loaded from=" + loadedFrom + " orig=" + ow + "x" + oh + " target=48x48");
                // usar downscale progressivo para preservar mais nitidez em reduções grandes
                BufferedImage scaledBuf = getProgressiveDownscale(img, 48, 48);
                if (scaledBuf != null) {
                    btn.setIcon(new ImageIcon(scaledBuf));
                    btn.setText("");
                }
            }
        } catch (IOException ex) {
            // silenciar falha de carregamento — mantém o texto
        }
    }

    // Escala uma Image para BufferedImage com alta qualidade (bicubic + render quality).
    private BufferedImage getHighQualityScaledImage(Image srcImg, int targetW, int targetH) {
        if (srcImg == null) return null;
        // garantir que temos um BufferedImage de origem
        BufferedImage srcBuf;
        if (srcImg instanceof BufferedImage) {
            srcBuf = (BufferedImage) srcImg;
        } else {
            srcBuf = new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = srcBuf.createGraphics();
            g.drawImage(srcImg, 0, 0, null);
            g.dispose();
        }

        BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.drawImage(srcBuf, 0, 0, targetW, targetH, null);
        g2.dispose();
        return dst;
    }

    // Downscale progressivo: reduz pela metade repetidamente usando bicubic até atingir tamanho alvo.
    private BufferedImage getProgressiveDownscale(Image srcImg, int targetW, int targetH) {
        if (srcImg == null) return null;
        int currentW = srcImg.getWidth(null);
        int currentH = srcImg.getHeight(null);
        if (currentW <= 0 || currentH <= 0) return null;

        BufferedImage cur;
        if (srcImg instanceof BufferedImage) {
            cur = (BufferedImage) srcImg;
        } else {
            cur = new BufferedImage(currentW, currentH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cur.createGraphics();
            g.drawImage(srcImg, 0, 0, null);
            g.dispose();
        }

        // Se já está próximo do alvo, usar único redimensionamento de alta qualidade
        if (currentW <= targetW * 2 && currentH <= targetH * 2) {
            return getHighQualityScaledImage(cur, targetW, targetH);
        }

        BufferedImage prev = cur;
        while (currentW / 2 >= targetW && currentH / 2 >= targetH) {
            int nextW = Math.max(targetW, currentW / 2);
            int nextH = Math.max(targetH, currentH / 2);
            BufferedImage tmp = new BufferedImage(nextW, nextH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(prev, 0, 0, nextW, nextH, null);
            g2.dispose();
            prev = tmp;
            currentW = nextW;
            currentH = nextH;
            // se já atingiu um pouco acima do alvo, sair e fazer o ajuste final
            if (currentW <= targetW * 2 && currentH <= targetH * 2) break;
        }

        // ajuste final com alta qualidade
        return getHighQualityScaledImage(prev, targetW, targetH);
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
                // deixar os botões de game-over um pouco mais para baixo
                int btnY = pm.getButtonsY() + 20;
                int btnW = 160;
                int gap = 20;
                int leftX = (WIDTH - (btnW * 2 + gap)) / 2;
                restartBtn.setBounds(leftX, btnY, btnW, 40);
                quitBtn.setBounds(leftX + btnW + gap, btnY, btnW, 40);
            } else {
                restartBtn.setVisible(false);
                quitBtn.setVisible(false);
            }
            // quando estamos no menu, posicionar start/music centralizados dentro do painel do menu
            if (inMenu) {
                int panelX = mh.getPanelX();
                int panelY = mh.getPanelY();
                int panelW = mh.getPanelW();
                int panelH = mh.getPanelH();
                int startW = 200;
                int startH = 50;
                int musicW = 130;
                int musicH = 30;
                int startX = panelX + (panelW - startW) / 2;
                int startY = panelY + panelH - startH - 60; // 60px acima da base do painel
                int musicX = panelX + (panelW - musicW) / 2;
                int musicY = startY + startH + 10;
                startBtn.setBounds(startX, startY, startW, startH);
                musicBtn.setBounds(musicX, musicY, musicW, musicH);
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
            // mostrar também os botões de canto (estilo igual aos controles)
            pauseBtn.setVisible(true);
            profileBtn.setVisible(true);
        } else {
            ctrlLeftBtn.setVisible(false);
            ctrlDownBtn.setVisible(false);
            ctrlRotateBtn.setVisible(false);
            ctrlRightBtn.setVisible(false);
            pauseBtn.setVisible(false);
            profileBtn.setVisible(false);
        }

        if (!KeyHandler.gamestart) {
            mh.update();
        }
    // posicionar os botões DENTRO do painel direito (sem fundo)
    int cornerPadding = 12;
        // usar coordenadas do painel direito
        int rpx = pm.getRightPanelX();
        int rpy = pm.getRightPanelY();
        int rpw = pm.getRightPanelW();
    // pause no canto superior esquerdo do painel direito
    pauseBtn.setBounds(rpx + cornerPadding, rpy + cornerPadding, btnW, btnH);
    // profile no canto superior direito do painel direito
    profileBtn.setBounds(rpx + rpw - cornerPadding - btnW, rpy + cornerPadding, btnW, btnH);
        // reforçar estilo sem fundo
        pauseBtn.setContentAreaFilled(false);
        pauseBtn.setBorderPainted(false);
        pauseBtn.setOpaque(false);
        profileBtn.setContentAreaFilled(false);
        profileBtn.setBorderPainted(false);
        profileBtn.setOpaque(false);
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
           // removido log de debug: System.out.println(PlayManager.dropInterval);
           powerupused = true;
           powerupInProgress = true;
           PowerupCounter = 0;
       }
       else {
       }
    }
}
