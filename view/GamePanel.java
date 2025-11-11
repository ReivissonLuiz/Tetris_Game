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

    public static final int WIDTH = 1150;
    public static final int HEIGHT = 620;
    final int FPS = 60;
    Thread GameThread;

    PlayManager pm = new PlayManager(this);
    MenuHandler mh = new MenuHandler();
    public static Sound music = new Sound();
    public static Sound se = new Sound();

    private JButton startBtn, musicBtn, restartBtn, quitBtn, ctrlLeftBtn, ctrlDownBtn, ctrlRotateBtn, ctrlRightBtn, pauseBtn, profileBtn, continueBtn;
    private JPanel recordsPanel;
    private static class Record {
        String name; int score;
        Record(String n, int s) { name = n; score = s; }
    }
    private java.util.List<Record> records = new ArrayList<>();
    private String currentProfileName = "Player";
    private BufferedImage menuBackground;

    public static Boolean powerupused;
    public static int PowerupCounter;
    public static Boolean powerupInProgress;

    private boolean isPastelPieceColor = false;
    private JRadioButton colorDefaultRadio, colorPastelRadio;
    private ButtonGroup colorGroup;
    private JPanel colorChoicePanel;
    private boolean previewEnabled = false;
    private JDialog profileDialog;

    public GamePanel(KeyHandler kh) {
        powerupused = false;
        powerupInProgress = false;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);
        addKeyListener(kh);
        setFocusable(true);

        startBtn = new JButton("Iniciar");
        startBtn.setFocusable(false);
        startBtn.addActionListener(ev -> {
            isPastelPieceColor = colorPastelRadio.isSelected();
            pm.setAltPieceColor(isPastelPieceColor);
            KeyHandler.gamestart = true;
            startBtn.setVisible(false);
            musicBtn.setVisible(false);
            colorDefaultRadio.setVisible(false);
            colorPastelRadio.setVisible(false);
            if (KeyHandler.musicOn && !music.isMusicPlaying()) {
                music.play(0, true);
                music.loop();
            }
            requestFocusInWindow();
        });
        add(startBtn);

        colorChoicePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 60, 90), 0, h, new Color(30, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0,0,0,40));
                g2.fillRect(6, 6, w-12, h-12);
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
        add(colorChoicePanel);

        restartBtn = new JButton("Novo jogo");
        restartBtn.setBounds((WIDTH - 340) / 2, (HEIGHT / 2) + 120, 160, 40);
        restartBtn.setFocusable(false);
        restartBtn.setVisible(false);
        restartBtn.addActionListener(ev -> {
            model.PlayManager.staticBlocks.clear();
            model.PlayManager.dropInterval = 40;
            powerupused = false;
            powerupInProgress = false;
            PowerupCounter = 0;
            KeyHandler.leftPressed = false;
            KeyHandler.rightPressed = false;
            KeyHandler.downPressed = false;
            KeyHandler.upPressed = false;
            KeyHandler.pausePressed = false;
            KeyHandler.gamequit = false;
            pm = new PlayManager(this);
            isPastelPieceColor = colorPastelRadio.isSelected();
            pm.setAltPieceColor(isPastelPieceColor);
            KeyHandler.gamestart = true;
            restartBtn.setVisible(false);
            quitBtn.setVisible(false);
            startBtn.setVisible(false);
            musicBtn.setVisible(false);
            colorDefaultRadio.setVisible(true);
            colorPastelRadio.setVisible(true);
            requestFocusInWindow();
            repaint();
            if (KeyHandler.musicOn && !music.isMusicPlaying()) {
                music.play(0, true);
                music.loop();
            }
        });
        add(restartBtn);

        quitBtn = new JButton("Sair");
        quitBtn.setBounds((WIDTH - 340) / 2 + 180, (HEIGHT / 2) + 120, 160, 40);
        quitBtn.setFocusable(false);
        quitBtn.setVisible(false);
        quitBtn.addActionListener(ev -> System.exit(0));
        add(quitBtn);

        musicBtn = new JButton(KeyHandler.musicOn ? "Música: On" : "Música: Off");
        musicBtn.setFocusable(false);
        musicBtn.addActionListener(ev -> {
            if (KeyHandler.musicOn) {
                music.stop();
                KeyHandler.musicOn = false;
                musicBtn.setText("Música: Off");
            } else {
                if (!music.isMusicPlaying()) {
                    music.play(0, true);
                    music.loop();
                }
                KeyHandler.musicOn = true;
                musicBtn.setText("Música: On");
            }
            requestFocusInWindow();
        });
        add(musicBtn);

        menuBackground = null;
        try {
            java.net.URL url = getClass().getResource("menu_bg.jpg");
            if (url != null) menuBackground = ImageIO.read(url);
            else {
                url = getClass().getResource("/view/menu_bg.jpg");
                if (url != null) menuBackground = ImageIO.read(url);
                else {
                    java.net.URL clUrl = Thread.currentThread().getContextClassLoader().getResource("view/menu_bg.jpg");
                    if (clUrl != null) menuBackground = ImageIO.read(clUrl);
                    else {
                        String userDir = System.getProperty("user.dir");
                        java.io.File f1 = new java.io.File("view/menu_bg.jpg");
                        java.io.File f2 = new java.io.File(userDir + java.io.File.separator + "view" + java.io.File.separator + "menu_bg.jpg");
                        if (f1.exists()) menuBackground = ImageIO.read(f1);
                        else if (f2.exists()) menuBackground = ImageIO.read(f2);
                    }
                }
            }
        } catch (IOException ex) { menuBackground = null; }

        recordsPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                RoundRectangle2D rr = new RoundRectangle2D.Double(0, 0, w, h, 18, 18);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 43, 50, 220), 0, h, new Color(25, 27, 30, 200));
                g2.setPaint(gp);
                g2.fill(rr);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(160,160,170,160));
                g2.draw(rr);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.setColor(new Color(240, 240, 240));
                String title = "Records";
                int tx = (w - g2.getFontMetrics().stringWidth(title)) / 2;
                g2.drawString(title, tx, 28);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g2.setColor(new Color(220,220,220));
                int startY = 48, step = 26;
                for (int i = 0; i < records.size(); i++) {
                    Record r = records.get(i);
                    String line = String.format("%d. %s - %d", i+1, r.name, r.score);
                    int lx = 16, ly = startY + i * step;
                    g2.drawString(line, lx, ly);
                }
            }
        };
        recordsPanel.setOpaque(false);
        recordsPanel.setVisible(false);
        add(recordsPanel);
        loadRecordsFromFile();
        loadProfileFromFile();

        ctrlLeftBtn = new JButton("◄");
        ctrlDownBtn = new JButton("▼");
        ctrlRotateBtn = new JButton("⟳");
        ctrlRightBtn = new JButton("►");
        pauseBtn = new JButton();
        profileBtn = new JButton();
        add(ctrlLeftBtn); add(ctrlDownBtn); add(ctrlRotateBtn); add(ctrlRightBtn); add(pauseBtn); add(profileBtn);

        setControlButtonIcon(ctrlLeftBtn, "ctrl_left.png");
        setControlButtonIcon(ctrlDownBtn, "ctrl_down.png");
        setControlButtonIcon(ctrlRotateBtn, "ctrl_rotate.png");
        setControlButtonIcon(ctrlRightBtn, "ctrl_right.png");
        setControlButtonIcon(pauseBtn, "pause.png");
        setControlButtonIcon(profileBtn, "profile.png");

        JButton[] arrowBtns = {ctrlLeftBtn, ctrlDownBtn, ctrlRotateBtn, ctrlRightBtn};
        for (JButton btn : arrowBtns) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setBackground(new Color(0, 0, 0, 0));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setBorderPainted(false);
        }
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pauseBtn.setBackground(new Color(0, 0, 0, 0));
        pauseBtn.setForeground(Color.WHITE);
        pauseBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        pauseBtn.setFocusPainted(false);
        pauseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseBtn.setContentAreaFilled(false);
        pauseBtn.setOpaque(false);
        pauseBtn.setBorderPainted(false);

        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileBtn.setBackground(new Color(0, 0, 0, 0));
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        profileBtn.setFocusPainted(false);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.setContentAreaFilled(false);
        profileBtn.setOpaque(false);
        profileBtn.setBorderPainted(false);

        pauseBtn.addActionListener(ev -> {
            if (KeyHandler.gamestart && !pm.gameOver) {
                KeyHandler.pausePressed = !KeyHandler.pausePressed;
                if (KeyHandler.pausePressed) music.stop();
                else if(KeyHandler.musicOn) {
                    music.play(0,true);
                    music.loop();
                }
                requestFocusInWindow();
            }
        });

        continueBtn = new JButton("Continuar");
        continueBtn.setFocusable(false);
        continueBtn.setVisible(false);
        continueBtn.addActionListener(ev -> {
            KeyHandler.pausePressed = false;
            if (KeyHandler.musicOn && !music.isMusicPlaying()) {
                music.play(0, true);
                music.loop();
            }
            requestFocusInWindow();
        });
        add(continueBtn);

        profileDialog = new JDialog((Frame) null, "Perfil do Jogador", true);
        profileDialog.setSize(360, 220);
        profileDialog.setUndecorated(false);
        profileDialog.setLayout(null);
        profileDialog.setResizable(false);
        profileDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel profilePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(50, 60, 90), 0, h, new Color(30, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0,0,0,40));
                g2.fillRect(8, 8, w-16, h-16);
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
        nameField.setBackground(new Color(40, 50, 80));
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
        JButton previewBtn = new JButton("Pré-visualizar peça");
        previewBtn.setBounds(32, 140, 296, 32);
        previewBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewBtn.setBackground(new Color(80, 120, 200));
        previewBtn.setForeground(Color.WHITE);
        previewBtn.setFocusPainted(false);
        previewBtn.setBorder(BorderFactory.createLineBorder(new Color(80, 120, 200), 2, true));
        previewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        previewBtn.addActionListener(e -> {
            previewEnabled = !previewEnabled;
            previewBtn.setText(previewEnabled ? "Pré-visualização: ON" : "Pré-visualizar peça");
            repaint();
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
        profilePanel.add(previewBtn);
        profileDialog.setContentPane(profilePanel);
        profileBtn.addActionListener(e -> {
            nameField.setText(currentProfileName);
            if (!profileDialog.isVisible()) {
                profileDialog.setLocationRelativeTo(this);
                profileDialog.setVisible(true);
            }
        });
        profileDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) { requestFocusInWindow(); }
            public void windowClosing(java.awt.event.WindowEvent e) { requestFocusInWindow(); }
        });
    }

    private void loadRecordsFromFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path file = dataDir.resolve("records.txt");
            if (!Files.exists(file)) {
                List<String> lines = new ArrayList<>();
                lines.add("Player:1200");
                lines.add("Player:800");
                lines.add("Player:400");
                lines.add("Player:200");
                lines.add("Player:100");
                Files.write(file, lines);
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
                } catch (NumberFormatException ex) {}
            }
            records.sort((a,b) -> Integer.compare(b.score, a.score));
            if (records.size() > 5) records = new ArrayList<>(records.subList(0,5));
        } catch (IOException ex) {}
    }

    private void saveProfileToFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path f = dataDir.resolve("profile.txt");
            Files.write(f, java.util.List.of(currentProfileName));
        } catch (IOException ex) {}
    }

    private void loadProfileFromFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path f = dataDir.resolve("profile.txt");
            if (!Files.exists(f)) {
                Files.write(f, java.util.List.of(currentProfileName));
                return;
            }
            List<String> lines = Files.readAllLines(f);
            if (!lines.isEmpty()) {
                String n = lines.get(0).trim();
                if (!n.isEmpty()) currentProfileName = n;
            }
        } catch (IOException ex) {}
    }

    public void addRecord(String name, int score) {
        records.add(new Record(name, score));
        records.sort((a, b) -> Integer.compare(b.score, a.score));
        if (records.size() > 5) records = new ArrayList<>(records.subList(0, 5));
        saveRecordsToFile();
        recordsPanel.repaint();
    }

    private void saveRecordsToFile() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            Path file = dataDir.resolve("records.txt");
            List<String> lines = new ArrayList<>();
            for (Record r : records) {
                lines.add(r.name + ":" + r.score);
            }
            Files.write(file, lines);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setControlButtonIcon(JButton btn, String filename) {
        try {
            Image img = null;
            java.net.URL url = getClass().getResource(filename);
            if (url == null) url = getClass().getResource("/view/" + filename);
            if (url != null) img = ImageIO.read(url);
            else {
                java.io.File f = new java.io.File("view" + java.io.File.separator + filename);
                if (f.exists()) img = ImageIO.read(f);
            }
            if (img != null) {
                BufferedImage scaledBuf = getProgressiveDownscale(img, 48, 48);
                if (scaledBuf != null) {
                    btn.setIcon(new ImageIcon(scaledBuf));
                    btn.setText("");
                }
            }
        } catch (IOException ex) {}
    }

    private BufferedImage getHighQualityScaledImage(Image srcImg, int targetW, int targetH) {
        if (srcImg == null) return null;
        BufferedImage srcBuf;
        if (srcImg instanceof BufferedImage) srcBuf = (BufferedImage) srcImg;
        else {
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

    private BufferedImage getProgressiveDownscale(Image srcImg, int targetW, int targetH) {
        if (srcImg == null) return null;
        int currentW = srcImg.getWidth(null), currentH = srcImg.getHeight(null);
        if (currentW <= 0 || currentH <= 0) return null;
        BufferedImage cur;
        if (srcImg instanceof BufferedImage) cur = (BufferedImage) srcImg;
        else {
            cur = new BufferedImage(currentW, currentH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = cur.createGraphics();
            g.drawImage(srcImg, 0, 0, null);
            g.dispose();
        }
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
            if (currentW <= targetW * 2 && currentH <= targetH * 2) break;
        }
        return getHighQualityScaledImage(prev, targetW, targetH);
    }

    public void LaunchGame() {
        GameThread = new Thread(this);
        GameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        double poweruptimedelta = 0;
        long currentPowerupTime;
        long lastPowerUpTime = System.currentTimeMillis();
        int powerupCounter = 0;
        int powerupInProgressCounter = 0;
        while (GameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
            currentPowerupTime = System.currentTimeMillis();
            poweruptimedelta += (currentPowerupTime - lastPowerUpTime) / 1000F;
            lastPowerUpTime = currentPowerupTime;
            if (poweruptimedelta >= 1) {
                poweruptimedelta--;
                if (powerupused && powerupCounter >= 20) {
                    powerupused = false;
                    powerupCounter = 0;
                } else if (powerupused && powerupCounter < 20) {
                    powerupCounter++;
                }
                if (powerupused && powerupInProgress) {
                    powerupInProgressCounter++;
                }
                if (powerupInProgress && powerupInProgressCounter >= 10) {
                    powerupInProgress = false;
                    PlayManager.dropInterval -= 40;
                    powerupInProgressCounter = 0;
                }
            }
        }
    }

    private void update() {
        mh.update();
        if (startBtn != null && musicBtn != null && restartBtn != null && quitBtn != null && continueBtn != null) {
            boolean inMenu = !KeyHandler.gamestart && !pm.gameOver;
            startBtn.setVisible(inMenu);
            musicBtn.setVisible(inMenu);
            if (inMenu) {
                int panelX = mh.getPanelX(), panelY = mh.getPanelY(), panelW = mh.getPanelW(), panelH = mh.getPanelH();
                int startW = 200, startH = 50, musicW = 130, musicH = 30;
                int startX = panelX + (panelW - startW) / 2;
                int startY = panelY + panelH - startH - 60;
                int musicX = panelX + (panelW - musicW) / 2;
                int musicY = startY + startH + 10;
                startBtn.setBounds(startX, startY, startW, startH);
                musicBtn.setBounds(musicX, musicY, musicW, musicH);
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
                ctrlLeftBtn.setVisible(false);
                ctrlDownBtn.setVisible(false);
                ctrlRotateBtn.setVisible(false);
                ctrlRightBtn.setVisible(false);
                pauseBtn.setVisible(false);
                profileBtn.setVisible(false);
                recordsPanel.setVisible(false);
            } else if (KeyHandler.gamestart && !pm.gameOver && !KeyHandler.pausePressed) {
                ctrlLeftBtn.setVisible(true);
                ctrlDownBtn.setVisible(true);
                ctrlRotateBtn.setVisible(true);
                ctrlRightBtn.setVisible(true);
                pauseBtn.setVisible(true);
                profileBtn.setVisible(true);
                recordsPanel.setVisible(true);
                int btnW = 48, btnH = 48, gap = 8;
                int totalW = btnW * 4 + gap * 3;
                int rpX = pm.getRightPanelX(), rpY = pm.getRightPanelY(), rpW = pm.getRightPanelW(), rpH = pm.getRightPanelH();
                int baseX = rpX + (rpW - totalW) / 2;
                int baseY = rpY + rpH - btnH - 24;
                ctrlLeftBtn.setBounds(baseX, baseY, btnW, btnH);
                ctrlDownBtn.setBounds(baseX + (btnW + gap), baseY, btnW, btnH);
                ctrlRotateBtn.setBounds(baseX + (btnW + gap) * 2, baseY, btnW, btnH);
                ctrlRightBtn.setBounds(baseX + (btnW + gap) * 3, baseY, btnW, btnH);
                int cornerPadding = 12;
                pauseBtn.setBounds(rpX + cornerPadding, rpY + cornerPadding, btnW, btnH);
                profileBtn.setBounds(rpX + rpW - cornerPadding - btnW, rpY + cornerPadding, btnW, btnH);
                int recordsW = 180, recordsH = 180;
                int recordsX = rpX + (rpW - recordsW) / 2;
                int recordsY = rpY + (rpH - recordsH) / 2 - 30;
                recordsPanel.setBounds(recordsX, recordsY, recordsW, recordsH);
            }
        }
        if (KeyHandler.pausePressed && !pm.gameOver) {
            int btnW = 120, btnH = 38, gap = 18, totalW = btnW * 3 + gap * 2;
            int baseX = (WIDTH - totalW) / 2, baseY = HEIGHT / 2 + 10;
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
        if (KeyHandler.gamestart && !KeyHandler.pausePressed && !pm.gameOver) {
            pm.update();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (!KeyHandler.gamestart) {
            if (menuBackground != null) {
                g2.drawImage(menuBackground, 0, 0, WIDTH, HEIGHT, null);
            }
            if (!KeyHandler.pausePressed) {
                mh.draw(g2);
            }
        } else {
            if (menuBackground != null) {
                g2.drawImage(menuBackground, 0, 0, WIDTH, HEIGHT, null);
            }
            Paint oldPaint = g2.getPaint();
            Composite oldComposite = g2.getComposite();
            GradientPaint menuBG = new GradientPaint(0, 0, Color.DARK_GRAY, 0, HEIGHT, Color.BLACK);
            g2.setPaint(menuBG);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setComposite(oldComposite);
            g2.setPaint(oldPaint);
            pm.draw(g2);
            if (previewEnabled) pm.drawPreview(g2);
        }
        if (KeyHandler.pausePressed && !pm.gameOver) {
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
        if (pm.gameOver) {
            g2.setColor(new Color(20, 20, 30, 230));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            ctrlLeftBtn.setVisible(false);
            ctrlDownBtn.setVisible(false);
            ctrlRotateBtn.setVisible(false);
            ctrlRightBtn.setVisible(false);
            pauseBtn.setVisible(false);
            profileBtn.setVisible(false);
            recordsPanel.setVisible(false);
            g2.setFont(new Font("Verdana", Font.BOLD, 60));
            g2.setColor(new Color(255, 220, 80));
            String perdeu = "Você perdeu";
            int perdeuW = g2.getFontMetrics().stringWidth(perdeu);
            g2.drawString(perdeu, (WIDTH - perdeuW) / 2, HEIGHT / 2 - 60);
            g2.setFont(new Font("SansSerif", Font.BOLD, 32));
            g2.setColor(new Color(255, 255, 255));
            String pontuacao = "Pontuação: " + pm.getScore();
            int pontuacaoW = g2.getFontMetrics().stringWidth(pontuacao);
            g2.drawString(pontuacao, (WIDTH - pontuacaoW) / 2, HEIGHT / 2);
            int btnW = 160, btnH = 40, gap = 30;
            int btnY = HEIGHT / 2 + 60;
            int leftX = (WIDTH - (btnW * 2 + gap)) / 2;
            restartBtn.setBounds(leftX, btnY, btnW, btnH);
            quitBtn.setBounds(leftX + btnW + gap, btnY, btnW, btnH);
            restartBtn.setVisible(true);
            quitBtn.setVisible(true);
            restartBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            restartBtn.setBackground(new Color(60, 120, 210));
            restartBtn.setForeground(Color.WHITE);
            restartBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 3, true));
            restartBtn.setFocusPainted(false);
            restartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quitBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            quitBtn.setBackground(new Color(180, 80, 80));
            quitBtn.setForeground(Color.WHITE);
            quitBtn.setBorder(BorderFactory.createLineBorder(new Color(120, 40, 40), 3, true));
            quitBtn.setFocusPainted(false);
            quitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void PowerUpUpdate() {
        if (!powerupused) {
            PlayManager.dropInterval += 40;
            powerupused = true;
            powerupInProgress = true;
            PowerupCounter = 0;
        }
    }
}