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
    // private JButton previewToggleBtn; // Remova esta linha
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

    private boolean isDarkTheme = true;
    private boolean isAltPieceColor = false;

    private JButton editProfileBtn;

    private JDialog profileDialog;

    // Troque para pastel, não neon
    private boolean isPastelPieceColor = false;
    private JRadioButton colorDefaultRadio;
    private JRadioButton colorPastelRadio;
    private ButtonGroup colorGroup;

    // Painel de escolha de cor e botão para abrir/fechar
    private JPanel colorChoicePanel;

    // Adicione flag para pré-visualização
    private boolean previewEnabled = false;

    // Adicione o botão de continuar como atributo
    private JButton continueBtn;

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
        startBtn.setFocusable(false);
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                isPastelPieceColor = colorPastelRadio.isSelected();
                pm.setAltPieceColor(isPastelPieceColor);
                KeyHandler.gamestart = true;
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
                colorDefaultRadio.setVisible(false);
                colorPastelRadio.setVisible(false);
                if (KeyHandler.musicOn && !GamePanel.music.isMusicPlaying()) {
                    GamePanel.music.play(0, true);
                    GamePanel.music.loop();
                }
                GamePanel.this.requestFocusInWindow();
            }
        });
        this.add(startBtn);

        // Painel estilizado para escolha de cor, igual ao estilo do perfil
        colorChoicePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Gradiente de fundo igual ao perfil
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 60, 90), 0, h, new Color(30, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                // Sombra leve igual ao perfil
                g2.setColor(new Color(0,0,0,40));
                g2.fillRect(6, 6, w-12, h-12);
                // Título
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                g2.setColor(new Color(180, 210, 255));
                String title = "Estilo das peças";
                int tx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
                g2.drawString(title, tx, 32);
            }
        };
        colorChoicePanel.setLayout(null);
        colorChoicePanel.setOpaque(false);
        int panelW = 340, panelH = 130;
        colorChoicePanel.setBounds((WIDTH - panelW) / 2, 0, panelW, panelH);

        // Radio buttons com estilo
        colorDefaultRadio = new JRadioButton("Cores padrão");
        colorPastelRadio = new JRadioButton("Cores pastel");
        colorGroup = new ButtonGroup();
        colorGroup.add(colorDefaultRadio);
        colorGroup.add(colorPastelRadio);
        colorDefaultRadio.setSelected(true);

        colorDefaultRadio.setFocusable(false);
        colorPastelRadio.setFocusable(false);

        colorDefaultRadio.setBounds(40, 55, 120, 32);
        colorPastelRadio.setBounds(180, 55, 120, 32);

        colorDefaultRadio.setFont(new Font("SansSerif", Font.BOLD, 15));
        colorPastelRadio.setFont(new Font("SansSerif", Font.BOLD, 15));
        colorDefaultRadio.setBackground(new Color(60, 120, 210));
        colorPastelRadio.setBackground(new Color(120, 80, 160));
        colorDefaultRadio.setForeground(Color.WHITE);
        colorPastelRadio.setForeground(Color.WHITE);
        colorDefaultRadio.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2, true));
        colorPastelRadio.setBorder(BorderFactory.createLineBorder(new Color(80, 40, 120), 2, true));

        // Descrições
        JLabel padraoDesc = new JLabel("Vibrante e clássico");
        padraoDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        padraoDesc.setForeground(new Color(180, 210, 255));
        padraoDesc.setBounds(40, 85, 120, 20);

        JLabel pastelDesc = new JLabel("Suave e moderno");
        pastelDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pastelDesc.setForeground(new Color(180, 210, 255));
        pastelDesc.setBounds(180, 85, 120, 20);

        colorChoicePanel.add(colorDefaultRadio);
        colorChoicePanel.add(colorPastelRadio);
        colorChoicePanel.add(padraoDesc);
        colorChoicePanel.add(pastelDesc);

        colorChoicePanel.setVisible(false);

        this.add(colorChoicePanel);

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
                // reinicializar flags de PowerUp
                powerupused = false;
                powerupInProgress = false;
                PowerupCounter = 0;
                // reinicializar flags de controle
                KeyHandler.leftPressed = false;
                KeyHandler.rightPressed = false;
                KeyHandler.downPressed = false;
                KeyHandler.upPressed = false;
                KeyHandler.pausePressed = false;
                KeyHandler.gamequit = false;
                // criar novo PlayManager para reiniciar posição e estado
                pm = new PlayManager();
                isPastelPieceColor = colorPastelRadio.isSelected();
                pm.setAltPieceColor(isPastelPieceColor);
                KeyHandler.gamestart = true;
                restartBtn.setVisible(false);
                quitBtn.setVisible(false);
                startBtn.setVisible(false);
                musicBtn.setVisible(false);
                colorDefaultRadio.setVisible(true);
                colorPastelRadio.setVisible(true);
                GamePanel.this.requestFocusInWindow();
                repaint();
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
                g2.setColor(new Color(240, 240, 240));
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

        // Inicialize os botões de controle ANTES de usar setControlButtonIcon
        ctrlLeftBtn = new JButton("◄");
        ctrlDownBtn = new JButton("▼");
        ctrlRotateBtn = new JButton("⟳");
        ctrlRightBtn = new JButton("►");
        pauseBtn = new JButton();
        profileBtn = new JButton();

        // Adicione os botões ao painel
        this.add(ctrlLeftBtn);
        this.add(ctrlDownBtn);
        this.add(ctrlRotateBtn);
        this.add(ctrlRightBtn);
        this.add(pauseBtn);
        this.add(profileBtn);

        // Agora pode chamar setControlButtonIcon sem erro de null
        setControlButtonIcon(ctrlLeftBtn, "ctrl_left.png");
        setControlButtonIcon(ctrlDownBtn, "ctrl_down.png");
        setControlButtonIcon(ctrlRotateBtn, "ctrl_rotate.png");
        setControlButtonIcon(ctrlRightBtn, "ctrl_right.png");
        setControlButtonIcon(pauseBtn, "pause.png");
        setControlButtonIcon(profileBtn, "profile.png");

        // Estilização dos botões de controle (setas) para ficarem arredondados igual ao pause/profile
        JButton[] arrowBtns = {ctrlLeftBtn, ctrlDownBtn, ctrlRotateBtn, ctrlRightBtn};
        for (JButton btn : arrowBtns) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setBackground(new Color(0, 0, 0, 0));
            btn.setForeground(Color.WHITE);
            // Remova a borda azul, deixe apenas espaço interno para arredondamento visual
            btn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setBorderPainted(false);
        }

        // Estilização dos botões de pause/profile (arredondados e sem borda azul)
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pauseBtn.setBackground(new Color(0, 0, 0, 0));
        pauseBtn.setForeground(Color.WHITE);
        // Borda arredondada personalizada (remova a borda azul quadrada)
        pauseBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pauseBtn.setFocusPainted(false);
        pauseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseBtn.setContentAreaFilled(false);
        pauseBtn.setOpaque(false);
        pauseBtn.setBorderPainted(false);

        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileBtn.setBackground(new Color(0, 0, 0, 0));
        profileBtn.setForeground(Color.WHITE);
        // Borda arredondada personalizada (remova a borda azul quadrada)
        profileBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        profileBtn.setFocusPainted(false);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.setContentAreaFilled(false);
        profileBtn.setOpaque(false);
        profileBtn.setBorderPainted(false);

        // Listeners corretos para pause/profile
        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                // Pausar apenas se o jogo estiver rodando e não estiver em game over
                if (KeyHandler.gamestart && !pm.gameOver) {
                    KeyHandler.pausePressed = !KeyHandler.pausePressed;
                    if (KeyHandler.pausePressed) {
                        GamePanel.music.stop();
                    } else if(KeyHandler.musicOn) {
                        GamePanel.music.play(0,true);
                        GamePanel.music.loop();
                    }
                    GamePanel.this.requestFocusInWindow();
                }
            }
        });

        // criar botão Continuar (aparece no pause)
        continueBtn = new JButton("Continuar");
        continueBtn.setFocusable(false);
        continueBtn.setVisible(false);
        continueBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                KeyHandler.pausePressed = false;
                if (KeyHandler.musicOn && !GamePanel.music.isMusicPlaying()) {
                    GamePanel.music.play(0, true);
                    GamePanel.music.loop();
                }
                GamePanel.this.requestFocusInWindow();
            }
        });
        this.add(continueBtn);

        // Inicialização do diálogo de perfil
        profileDialog = new JDialog((Frame) null, "Perfil do Jogador", true);
        profileDialog.setSize(360, 220);
        profileDialog.setUndecorated(false);
        profileDialog.setLayout(null);
        profileDialog.setResizable(false);
        profileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel profilePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // NÃO chame super.paintComponent(g) para evitar fundo branco!
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Fundo principal
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 60, 90), 0, h, new Color(30, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Sombra leve
                g2.setColor(new Color(0,0,0,40));
                g2.fillRect(8, 8, w-16, h-16);

                // Título (apenas texto branco, sem faixa azul)
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String title = "Perfil do Jogador";
                int tx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
                g2.drawString(title, tx, 32);
            }
        };
        profilePanel.setLayout(null);
        profilePanel.setBounds(0, 0, 360, 220);
        profilePanel.setOpaque(true);

        JLabel nameLabel = new JLabel("Nome:");
        nameLabel.setBounds(32, 56, 60, 28);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(new Color(180, 210, 255));

        JTextField nameField = new JTextField(currentProfileName);
        nameField.setBounds(100, 56, 180, 28);
        nameField.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameField.setBackground(new Color(40, 50, 80)); // fundo escuro igual ao resto
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(new Color(180, 210, 255));
        nameField.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 2, true));

        JButton saveBtn = new JButton("Salvar nome");
        saveBtn.setBounds(290, 56, 40, 28);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(80, 120, 200));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 2, true));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel styleLabel = new JLabel("Estilo das peças:");
        styleLabel.setBounds(32, 100, 120, 24);
        styleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        styleLabel.setForeground(new Color(180, 210, 255));

        JButton pastelBtn = new JButton("Pastel");
        pastelBtn.setBounds(160, 100, 80, 28);
        pastelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pastelBtn.setBackground(new Color(180, 160, 220));
        pastelBtn.setForeground(Color.WHITE);
        pastelBtn.setFocusPainted(false);
        pastelBtn.setBorder(BorderFactory.createLineBorder(new Color(120, 80, 160), 2, true));
        pastelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton padraoBtn = new JButton("Padrão");
        padraoBtn.setBounds(250, 100, 80, 28);
        padraoBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        padraoBtn.setBackground(new Color(60, 120, 210));
        padraoBtn.setForeground(Color.WHITE);
        padraoBtn.setFocusPainted(false);
        padraoBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2, true));
        padraoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Remova o JLabel dicaLabel
        // JLabel dicaLabel = new JLabel("Dica: personalize seu nome e estilo!");
        // dicaLabel.setBounds(32, 150, 300, 22);
        // dicaLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        // dicaLabel.setForeground(new Color(180, 210, 255, 180));

        // Adicione o botão de pré-visualização mais para cima
        JButton previewBtn = new JButton("Pré-visualizar peça");
        previewBtn.setBounds(32, 140, 296, 32); // posição Y ajustada para cima
        previewBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewBtn.setBackground(new Color(80, 120, 200));
        previewBtn.setForeground(Color.WHITE);
        previewBtn.setFocusPainted(false);
        previewBtn.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 2, true));
        previewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        previewBtn.addActionListener(e -> {
            previewEnabled = !previewEnabled;
            previewBtn.setText(previewEnabled ? "Pré-visualização: ON" : "Pré-visualizar peça");
            GamePanel.this.repaint();
        });

        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty()) {
                currentProfileName = newName;
                saveProfileToFile();
                JOptionPane.showMessageDialog(profileDialog, "Nome salvo!", "Perfil", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pastelBtn.addActionListener(e -> {
            isPastelPieceColor = true;
            colorPastelRadio.setSelected(true);
            colorDefaultRadio.setSelected(false);
            pm.setAltPieceColor(true);
            JOptionPane.showMessageDialog(profileDialog, "Estilo pastel ativado!", "Perfil", JOptionPane.INFORMATION_MESSAGE);
        });

        padraoBtn.addActionListener(e -> {
            isPastelPieceColor = false;
            colorDefaultRadio.setSelected(true);
            colorPastelRadio.setSelected(false);
            pm.setAltPieceColor(false);
            JOptionPane.showMessageDialog(profileDialog, "Estilo padrão ativado!", "Perfil", JOptionPane.INFORMATION_MESSAGE);
        });

        profilePanel.add(nameLabel);
        profilePanel.add(nameField);
        profilePanel.add(saveBtn);
        profilePanel.add(styleLabel);
        profilePanel.add(pastelBtn);
        profilePanel.add(padraoBtn);
        // Remova: profilePanel.add(dicaLabel);
        profilePanel.add(previewBtn);

        profileDialog.setContentPane(profilePanel);

        // Corrija o botão de perfil para abrir apenas UMA janela
        profileBtn.addActionListener(e -> {
            nameField.setText(currentProfileName);
            if (!profileDialog.isVisible()) {
                profileDialog.setLocationRelativeTo(GamePanel.this);
                profileDialog.setVisible(true);
            }
        });

        // Adicione este WindowListener para garantir que o foco volta ao painel após fechar o perfil
        profileDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                GamePanel.this.requestFocusInWindow();
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                GamePanel.this.requestFocusInWindow();
            }
        });

        // Remova qualquer outro listener duplicado para profileBtn
    }

    // Os valores padrão de pontuação para records estão definidos neste trecho:
    // 1200, 800, 400, 200, 100
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
                // Comentado para não imprimir no terminal:
                // System.out.println("[IconLoad] " + filename + " loaded from=" + loadedFrom + " orig=" + ow + "x" + oh + " target=48x48");
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
        if (startBtn != null && musicBtn != null && restartBtn != null && quitBtn != null && continueBtn != null) {
            boolean inMenu = !KeyHandler.gamestart && !pm.gameOver;

            // Botões da tela inicial
            startBtn.setVisible(inMenu);
            musicBtn.setVisible(inMenu);

            // Quando estamos no menu, posicionar start/music centralizados dentro do painel do menu
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
                int startY = panelY + panelH - startH - 60;
                int musicX = panelX + (panelW - musicW) / 2;
                int musicY = startY + startH + 10;
                startBtn.setBounds(startX, startY, startW, startH);
                musicBtn.setBounds(musicX, musicY, musicW, musicH);

                // Estilização moderna dos botões iniciais
                startBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
                startBtn.setBackground(new Color(60, 120, 210));
                startBtn.setForeground(Color.WHITE);
                startBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 3, true));
                startBtn.setFocusPainted(false);
                startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                musicBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
                musicBtn.setBackground(new Color(80, 120, 200));
                musicBtn.setForeground(Color.WHITE);
                musicBtn.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 2, true));
                musicBtn.setFocusPainted(false);
                musicBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Esconda os botões de controle, perfil, pause e records na tela inicial
                ctrlLeftBtn.setVisible(false);
                ctrlDownBtn.setVisible(false);
                ctrlRotateBtn.setVisible(false);
                ctrlRightBtn.setVisible(false);
                pauseBtn.setVisible(false);
                profileBtn.setVisible(false);
                recordsPanel.setVisible(false);
            }
            // ...existing code for pause/game over/buttons...
            else if (KeyHandler.gamestart && !pm.gameOver && !KeyHandler.pausePressed) {
                // Mostre os botões de controle, perfil, pause e records durante o jogo
                ctrlLeftBtn.setVisible(true);
                ctrlDownBtn.setVisible(true);
                ctrlRotateBtn.setVisible(true);
                ctrlRightBtn.setVisible(true);
                pauseBtn.setVisible(true);
                profileBtn.setVisible(true);
                recordsPanel.setVisible(true);

                // Posicione os botões de controle e records
                int btnW = 48;
                int btnH = 48;
                int gap = 8;
                int totalW = btnW * 4 + gap * 3;
                int rpX = pm.getRightPanelX();
                int rpY = pm.getRightPanelY();
                int rpW = pm.getRightPanelW();
                int rpH = pm.getRightPanelH();
                int baseX = rpX + (rpW - totalW) / 2;
                int baseY = rpY + rpH - btnH - 24;

                ctrlLeftBtn.setBounds(baseX, baseY, btnW, btnH);
                ctrlDownBtn.setBounds(baseX + (btnW + gap), baseY, btnW, btnH);
                ctrlRotateBtn.setBounds(baseX + (btnW + gap) * 2, baseY, btnW, btnH);
                ctrlRightBtn.setBounds(baseX + (btnW + gap) * 3, baseY, btnW, btnH);

                int cornerPadding = 12;
                pauseBtn.setBounds(rpX + cornerPadding, rpY + cornerPadding, btnW, btnH);
                profileBtn.setBounds(rpX + rpW - cornerPadding - btnW, rpY + cornerPadding, btnW, btnH);

                int recordsW = 180;
                int recordsH = 180;
                int recordsX = rpX + (rpW - recordsW) / 2;
                int recordsY = rpY + (rpH - recordsH) / 2 - 30;
                recordsPanel.setBounds(recordsX, recordsY, recordsW, recordsH);
            }
        }
        // PAUSE: mostrar botões de novo jogo, sair e continuar
        if (KeyHandler.pausePressed && !pm.gameOver) {
            int btnW = 120;
            int btnH = 38;
            int gap = 18;
            int totalW = btnW * 3 + gap * 2;
            int baseX = (WIDTH - totalW) / 2;
            int baseY = HEIGHT / 2 + 10;

            restartBtn.setBounds(baseX, baseY, btnW, btnH);
            continueBtn.setBounds(baseX + btnW + gap, baseY, btnW, btnH);
            quitBtn.setBounds(baseX + (btnW + gap) * 2, baseY, btnW, btnH);

            restartBtn.setVisible(true);
            continueBtn.setVisible(true);
            quitBtn.setVisible(true);

            restartBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            restartBtn.setBackground(new Color(60, 120, 210));
            restartBtn.setForeground(Color.WHITE);
            restartBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2, true));
            restartBtn.setFocusPainted(false);
            restartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            continueBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            continueBtn.setBackground(new Color(80, 180, 120));
            continueBtn.setForeground(Color.WHITE);
            continueBtn.setBorder(BorderFactory.createLineBorder(new Color(40, 120, 60), 2, true));
            continueBtn.setFocusPainted(false);
            continueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            quitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            quitBtn.setBackground(new Color(180, 80, 80));
            quitBtn.setForeground(Color.WHITE);
            quitBtn.setBorder(BorderFactory.createLineBorder(new Color(120, 40, 40), 2, true));
            quitBtn.setFocusPainted(false);
            quitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // NÃO esconda os controles, pause, perfil e records durante o pause
            ctrlLeftBtn.setVisible(true);
            ctrlDownBtn.setVisible(true);
            ctrlRotateBtn.setVisible(true);
            ctrlRightBtn.setVisible(true);
            pauseBtn.setVisible(true);
            profileBtn.setVisible(true);
            recordsPanel.setVisible(true);
        } else if (!KeyHandler.pausePressed && !pm.gameOver) {
            continueBtn.setVisible(false);
            restartBtn.setVisible(false);
            quitBtn.setVisible(false);
        }
        // Adicione esta linha para garantir que as peças descem corretamente:
        if (KeyHandler.gamestart && !KeyHandler.pausePressed && !pm.gameOver) {
            pm.update();
        }
        // ...existing code...
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (!KeyHandler.gamestart)
        {
            // Fundo já é imagem, não desenhe gradiente
            if (menuBackground != null) {
                g2.drawImage(menuBackground, 0, 0, WIDTH, HEIGHT, null);
            }

            // NÃO desenhe nada do MenuHandler se estiver pausado
            if (!KeyHandler.pausePressed) {
                mh.draw(g2);
            }
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
            // Desenha pré-visualização se ativado
            if (previewEnabled) {
                pm.drawPreview(g2); // Certifique-se que PlayManager tem drawPreview(Graphics2D)
            }
        }

        if (KeyHandler.pausePressed && !pm.gameOver) {
            // Painel escurecido e mensagem de pausa
            g2.setColor(new Color(30, 30, 40, 220));
            g2.fillRoundRect(WIDTH / 2 - 260, HEIGHT / 2 - 120, 520, 240, 32, 32);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 38));
            g2.setColor(new Color(180, 210, 255));
            String msg = "Jogo Pausado";
            int msgW = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (WIDTH - msgW) / 2, HEIGHT / 2 - 40);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            g2.setColor(new Color(220, 220, 220));
            String dica = "Escolha uma opção abaixo";
            int dicaW = g2.getFontMetrics().stringWidth(dica);
            g2.drawString(dica, (WIDTH - dicaW) / 2, HEIGHT / 2);
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