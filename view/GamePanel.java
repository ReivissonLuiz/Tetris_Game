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
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    private JPanel recordsPanel;
    // Records with name and score
    private static class Record {
        String name;
        int score;
        Record(String n, int s) { name = n; score = s; }
    }
    private java.util.List<Record> records = new ArrayList<>();
    // perfil atual
    private String currentProfileName = "Player";
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
        // reiniciar o estado do jogo
                gameOverProcessed = false;
                // limpar blocos estáticos e resetar intervalo de queda
                model.PlayManager.staticBlocks.clear();
                model.PlayManager.dropInterval = 40;
                // criar novo PlayManager para reiniciar posição e estado
                pm = new PlayManager();
                // garantir que o jogo está em execução e não em pause
                KeyHandler.gamestart = true;
                KeyHandler.pausePressed = false;
                // esconder botões de game over
                restartBtn.setVisible(false);
                quitBtn.setVisible(false);
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
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

        // painel de records (centralizado no painel direito) - criado aqui e posicionado em update()
        recordsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // fundo semi-transparente com borda arredondada
                RoundRectangle2D rr = new RoundRectangle2D.Double(0, 0, w, h, 18, 18);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 43, 50, 220), 0, h, new Color(25, 27, 30, 200));
                g2.setPaint(gp);
                g2.fill(rr);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(160,160,170,160));
                g2.draw(rr);

                // título
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.setColor(new Color(240,240,240));
                String title = "Records";
                int tx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
                g2.drawString(title, tx, 28);

                // listar records com nome
                g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g2.setColor(new Color(220,220,220));
                int startY = 48;
                int step = 26;
                for (int i = 0; i < records.size(); i++) {
                    Record r = records.get(i);
                    String line = String.format("%d. %s - %d", i+1, r.name, r.score);
                    int lx = 16;
                    int ly = startY + i * step;
                    g2.drawString(line, lx, ly);
                }
            }
        };
        recordsPanel.setOpaque(false);
        recordsPanel.setVisible(false);
        this.add(recordsPanel);
        // carregar records e perfil do arquivo (se existir)
    loadRecordsFromFile();
    loadProfileFromFile();

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
                // abrir diálogo para escolher/editar o nome do perfil
                String input = JOptionPane.showInputDialog(GamePanel.this, "Nome do perfil:", currentProfileName);
                if (input != null) {
                    String trimmed = input.trim();
                    if (!trimmed.isEmpty()) {
                        currentProfileName = trimmed;
                        saveProfileToFile();
                        JOptionPane.showMessageDialog(GamePanel.this, "Perfil atualizado: " + currentProfileName);
                    }
                }
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

    // Carrega records de data/records.txt; formato: name:score por linha (ou somente score para compatibilidade)
    private void loadRecordsFromFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path file = dataDir.resolve("records.txt");
            if (!Files.exists(file)) {
                // criar arquivo com valores padrão
                List<String> lines = new ArrayList<>();
                // valores padrão
                lines.add("Player:1200");
                lines.add("Player:800");
                lines.add("Player:400");
                lines.add("Player:200");
                lines.add("Player:100");
                Files.write(file, lines);
                // carregar os padrões para a lista
                records.clear();
                records.add(new Record("Player", 1200));
                records.add(new Record("Player", 800));
                records.add(new Record("Player", 400));
                records.add(new Record("Player", 200));
                records.add(new Record("Player", 100));
                return;
            }
            List<String> lines = Files.readAllLines(file);
            records.clear();
            for (String l : lines) {
                String line = l.trim();
                if (line.isEmpty()) continue;
                String name = "Player";
                String scorePart = line;
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    name = parts[0].trim();
                    scorePart = parts[1].trim();
                }
                try {
                    int sc = Integer.parseInt(scorePart);
                    records.add(new Record(name, sc));
                } catch (NumberFormatException ex) {
                    // ignorar linha inválida
                }
            }
            // ordenar por score decrescente e manter top5
            records.sort((a,b) -> Integer.compare(b.score, a.score));
            if (records.size() > 5) records = new ArrayList<>(records.subList(0,5));
        } catch (IOException ex) {
            // ignorar falha de I/O — manter valores padrão
        }
    }

    // Salva os records atuais em data/records.txt (formato name:score)
    private void saveRecordsToFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path file = dataDir.resolve("records.txt");
            List<String> lines = new ArrayList<>();
            for (Record r : records) lines.add(r.name + ":" + r.score);
            Files.write(file, lines);
        } catch (IOException ex) {
            // ignorar falha de I/O
        }
    }

    // Carrega o perfil atual (nome) de data/profile.txt
    private void loadProfileFromFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path f = dataDir.resolve("profile.txt");
            if (!Files.exists(f)) {
                // criar com default
                Files.write(f, java.util.List.of(currentProfileName));
                return;
            }
            List<String> lines = Files.readAllLines(f);
            if (!lines.isEmpty()) {
                String n = lines.get(0).trim();
                if (!n.isEmpty()) currentProfileName = n;
            }
        } catch (IOException ex) {
            // ignorar
        }
    }

    private void saveProfileToFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path f = dataDir.resolve("profile.txt");
            Files.write(f, java.util.List.of(currentProfileName));
        } catch (IOException ex) {
            // ignorar
        }
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

    // flag para detectar transição para game over e processar records
    private boolean gameOverProcessed = false;

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
            } else if (KeyHandler.pausePressed && !pm.gameOver) {
                // quando pausado, mostrar os mesmos botões (Novo jogo / Sair)
                restartBtn.setVisible(true);
                quitBtn.setVisible(true);
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
                int btnY = pm.getButtonsY() + 20;
                int btnW = 160;
                int gap = 20;
                int leftX = (WIDTH - (btnW * 2 + gap)) / 2;
                restartBtn.setBounds(leftX, btnY, btnW, 40);
                quitBtn.setBounds(leftX + btnW + gap, btnY, btnW, 40);
                // esconder controles enquanto pausado
                ctrlLeftBtn.setVisible(false);
                ctrlDownBtn.setVisible(false);
                ctrlRotateBtn.setVisible(false);
                ctrlRightBtn.setVisible(false);
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
            // mostrar painel de records no meio do painel direito
            int recordsW = 180;
            int recordsH = 180;
            int recordsX = rpX + (rpW - recordsW) / 2;
            int recordsY = rpY + (rpH - recordsH) / 2 - 30; // leve ajuste para cima
            recordsPanel.setBounds(recordsX, recordsY, recordsW, recordsH);
            recordsPanel.setVisible(true);
        } else {
            ctrlLeftBtn.setVisible(false);
            ctrlDownBtn.setVisible(false);
            ctrlRotateBtn.setVisible(false);
            ctrlRightBtn.setVisible(false);
            pauseBtn.setVisible(false);
            profileBtn.setVisible(false);
            recordsPanel.setVisible(false);
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
        // quando entrou em game over, atualizar records se necessário
        if (pm.gameOver && !gameOverProcessed) {
            gameOverProcessed = true;
            int finalScore = pm.getScore();
            // adicionar novo record com o nome do perfil atual
            records.add(new Record(currentProfileName, finalScore));
            // ordenar e manter top5
            records.sort((a,b) -> Integer.compare(b.score, a.score));
            if (records.size() > 5) records = new ArrayList<>(records.subList(0,5));
            saveRecordsToFile();
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
