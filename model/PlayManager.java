package model;

import pieces.*;
import view.GamePanel;
import java.awt.*;
import java.util.*;
import java.awt.geom.RoundRectangle2D;
import controller.KeyHandler;

public class PlayManager {

    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int LEFT_PANEL_GAP = 24;
    private static final int PREVIEW_BOX_SIZE = 170;
    private static final int PREVIEW_BOX_TOP_OFFSET = 70;
    private static final int INFO_TOP_OFFSET = 70;

    public static int right_x, left_x, top_y, bottom_y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();
    public static int dropInterval = 40;

    private int leftPanelX, leftPanelY, leftPanelW, leftPanelH;
    private int rightPanelX, rightPanelY, rightPanelW, rightPanelH;
    private final int MINO_START_X, MINO_START_Y;
    private final int NEXT_MINO_X, NEXT_MINO_Y;

    private Mino currentMino, nextMino;
    public boolean gameOver = false;
    private boolean effectsCounterOn = false;
    private int effectCounter = 0;
    private ArrayList<Integer> effectY = new ArrayList<>();
    private int level = 1, lines = 0, score = 0;
    private boolean gameOverSaved = false;
    private view.GamePanel gamePanel;

    public PlayManager(view.GamePanel gp) {
        this.gamePanel = gp;
        
        top_y = Math.max((GamePanel.HEIGHT - Block.SIZE * ROWS) / 2, 20);
        leftPanelX = 40;
        leftPanelY = top_y;
        leftPanelW = 250;
        leftPanelH = Block.SIZE * ROWS;

        rightPanelW = leftPanelW;
        rightPanelH = Block.SIZE * ROWS;
        int rightMargin = 40;
        rightPanelX = GamePanel.WIDTH - rightMargin - rightPanelW;
        rightPanelY = top_y;

        int innerLeftLimit = leftPanelX + leftPanelW + LEFT_PANEL_GAP;
        int innerRightLimit = rightPanelX - LEFT_PANEL_GAP;
        int availableWidth = innerRightLimit - innerLeftLimit;
        left_x = innerLeftLimit + (availableWidth - Block.SIZE * COLS) / 2;
        right_x = left_x + Block.SIZE * COLS;
        bottom_y = top_y + Block.SIZE * ROWS;

        MINO_START_X = left_x + (Block.SIZE * COLS / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;
        NEXT_MINO_X = leftPanelX + (leftPanelW / 2) - Block.SIZE;
        NEXT_MINO_Y = leftPanelY + PREVIEW_BOX_TOP_OFFSET + (PREVIEW_BOX_SIZE / 2) - Block.SIZE;

        staticBlocks = new ArrayList<>();
        currentMino = minoPicker();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = new Mino_Bar();
        nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);
    }

    private Mino minoPicker() {
        return switch (new Random().nextInt(7)) {
            case 0 -> new Mino_L1();
            case 1 -> new Mino_L2();
            case 2 -> new Mino_Square();
            case 3 -> new Mino_Bar();
            case 4 -> new Mino_T();
            case 5 -> new Mino_Z1();
            case 6 -> new Mino_Z2();
            default -> null;
        };
    }

public void update() {
    if (!KeyHandler.gamestart) return;

    if (!gameOver && !KeyHandler.pausePressed) {
            if (!currentMino.active) {
                boolean minoSobreposto = Arrays.stream(currentMino.b)
                    .anyMatch(block -> staticBlocks.stream()
                        .anyMatch(b -> b.x == block.x && b.y == block.y));
                boolean minoNaEntrada = Arrays.stream(currentMino.b)
                    .anyMatch(block -> block.x == MINO_START_X && block.y == MINO_START_Y);

                if (minoNaEntrada || minoSobreposto) {
                    gameOver = true;
                    GamePanel.music.stop();
                    GamePanel.se.play(2, false);
                    // Salvar record imediatamente quando game over
                if (gamePanel != null && !gameOverSaved) {
                        gameOverSaved = true;
                        // Obtain the current profile name via a public getter if available,
                        // otherwise fall back to reflective access of the field.
                        String profileName = null;
                        try {
                            try {
                                java.lang.reflect.Method m = gamePanel.getClass().getMethod("getCurrentProfileName");
                                Object v = m.invoke(gamePanel);
                                if (v instanceof String) profileName = (String) v;
                            } catch (NoSuchMethodException nm) {
                                java.lang.reflect.Field f = gamePanel.getClass().getDeclaredField("currentProfileName");
                                f.setAccessible(true);
                                Object v = f.get(gamePanel);
                                if (v instanceof String) profileName = (String) v;
                            }
                        } catch (Exception e) {
                            // ignore and leave profileName as null
                        }
                        gamePanel.addRecord(profileName, score);
                    }
                    return;
                }

                Collections.addAll(staticBlocks, currentMino.b);
                currentMino.deactivating = false;
                currentMino = nextMino;
                currentMino.setXY(MINO_START_X, MINO_START_Y);
                nextMino = minoPicker();
                nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);
                checkDelete();
            } else {
                currentMino.update();
            }
        }
    }

    private void checkDelete() {
        if (staticBlocks.isEmpty()) return;

        boolean[][] occupied = new boolean[ROWS][COLS];
        for (Block b : staticBlocks) {
            int col = (b.x - left_x) / Block.SIZE;
            int row = (b.y - top_y) / Block.SIZE;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                occupied[row][col] = true;
            }
        }

        boolean[] isFullRow = new boolean[ROWS];
        ArrayList<Integer> fullRows = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            isFullRow[r] = true;
            for (int c = 0; c < COLS; c++) {
                if (!occupied[r][c]) {
                    isFullRow[r] = false;
                    break;
                }
            }
            if (isFullRow[r]) fullRows.add(r);
        }
        if (fullRows.isEmpty()) return;

        effectsCounterOn = true;
        effectY.clear();
        fullRows.forEach(r -> effectY.add(top_y + r * Block.SIZE));

        int[] clearedBelow = new int[ROWS];
        int acc = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            clearedBelow[r] = acc;
            if (isFullRow[r]) acc++;
        }

        ArrayList<Block> newStatic = new ArrayList<>(staticBlocks.size());
        for (Block b : staticBlocks) {
            int row = (b.y - top_y) / Block.SIZE;
            int col = (b.x - left_x) / Block.SIZE;
            if (row < 0 || row >= ROWS || col < 0 || col >= COLS) continue;
            if (isFullRow[row]) continue;
            int newRow = row + clearedBelow[row];
            if (newRow >= ROWS) continue;
            b.y = top_y + newRow * Block.SIZE;
            newStatic.add(b);
        }
        staticBlocks = newStatic;

        int clearedCount = fullRows.size();
        lines += clearedCount;
        if (lines > 0 && lines % 10 == 0 && dropInterval > 1) {
            level++;
            dropInterval -= (dropInterval > 10) ? 10 : 1;
        }
        GamePanel.se.play(1, false);

        score += switch (clearedCount) {
            case 1 -> 40;
            case 2 -> 100;
            case 3 -> 300;
            case 4 -> 1200;
            default -> 0;
        };
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint playBG = new GradientPaint(left_x, top_y, new Color(60, 63, 65), right_x, bottom_y, new Color(25, 28, 30));
        g2.setPaint(playBG);
        g2.fillRect(left_x, top_y, Block.SIZE * COLS, Block.SIZE * ROWS);

        GradientPaint highlight = new GradientPaint(left_x, top_y, new Color(255,255,255,30), left_x, top_y + Block.SIZE * ROWS / 2, new Color(255,255,255,6));
        g2.setPaint(highlight);
        g2.fillRect(left_x + 4, top_y + 4, Block.SIZE * COLS - 8, Block.SIZE * ROWS / 2);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(5f));
        GradientPaint borderGP = new GradientPaint(left_x, top_y, new Color(200,200,200,200), left_x, top_y + Block.SIZE * ROWS, new Color(120,120,120,200));
        g2.setPaint(borderGP);
        g2.drawRect(left_x - 4, top_y - 2, Block.SIZE * COLS + 8, Block.SIZE * ROWS + 8);
        g2.setStroke(oldStroke);

        g2.setColor(new Color(255,255,255,30));
        g2.setStroke(new BasicStroke(1f));
        for (int ci = 1; ci < COLS; ci++) {
            int gx = left_x + ci * Block.SIZE;
            g2.drawLine(gx, top_y, gx, bottom_y);
        }
        for (int ri = 1; ri < ROWS; ri++) {
            int gy = top_y + ri * Block.SIZE;
            g2.drawLine(left_x, gy, right_x, gy);
        }
        g2.setStroke(oldStroke);

        drawLeftPanel(g2);
        drawRightPanel(g2);

        Font pmScoreFont = new Font("SansSerif", Font.BOLD, 56);
        g2.setFont(pmScoreFont);
        String scoreStr = String.valueOf(score);
        FontMetrics pmFmScore = g2.getFontMetrics(pmScoreFont);
        int centerX = leftPanelX + (leftPanelW / 2);
        int pmScoreY = leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET;
        int pmScoreX = centerX - (pmFmScore.stringWidth(scoreStr) / 2);
        g2.setColor(new Color(255,215,0));
        g2.drawString(scoreStr, pmScoreX, pmScoreY);

        Font labelFont = new Font("SansSerif", Font.PLAIN, 16);
        g2.setFont(labelFont);
        String scoreLabel = "Pontuação";
        int labelX = centerX - (g2.getFontMetrics(labelFont).stringWidth(scoreLabel) / 2);
        int labelY = pmScoreY + 22;
        g2.setColor(new Color(230,230,230));
        g2.drawString(scoreLabel, labelX, labelY);

        Font smallFont = new Font("SansSerif", Font.BOLD, 18);
        g2.setFont(smallFont);
        String stats = "Nível: " + level + "   Linhas: " + lines;
        int statsX = centerX - (g2.getFontMetrics(smallFont).stringWidth(stats) / 2);
        int statsY = labelY + 20;
        g2.setColor(new Color(185,195,205));
        g2.drawString(stats, statsX, statsY);

        Font keysFont = new Font("SansSerif", Font.PLAIN, 14);
        g2.setFont(keysFont);
        g2.setColor(new Color(220,220,220));
        String[] keys = {
            "Controles:",
            "Mover esquerda: A",
            "Mover direita:  D",
            "Descer: S",
            "Girar: W",
            "Pausar: Espaço",
            "Música: M"
        };
        int keysStartY = statsY + 56;
        int keyLineStep = 20;
        for (int k = 0; k < keys.length; k++) {
            String line = keys[k];
            int kx = centerX - (g2.getFontMetrics(keysFont).stringWidth(line) / 2);
            int ky = keysStartY + (k * keyLineStep);
            if (k == 0) {
                g2.setColor(new Color(240,240,240));
                g2.setFont(keysFont.deriveFont(Font.BOLD));
                g2.drawString(line, kx, ky);
                g2.setFont(keysFont);
                g2.setColor(new Color(220,220,220));
            } else {
                g2.drawString(line, kx, ky);
            }
        }

        if (currentMino != null) currentMino.draw(g2);
        nextMino.draw(g2);
        staticBlocks.forEach(block -> block.draw(g2));

        if (effectsCounterOn) {
            effectCounter++;
            g2.setColor(Color.RED);
            effectY.forEach(y -> g2.fillRect(left_x, y, Block.SIZE * COLS, Block.SIZE));
            if (effectCounter == 10) {
                effectsCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }
        }
    }

    // Adicione este método para desenhar a pré-visualização
    public void drawPreview(Graphics2D g2) {
        if (currentMino == null || !currentMino.active) return;
        // Cria uma cópia das posições atuais
        int[] px = new int[4];
        int[] py = new int[4];
        for (int i = 0; i < 4; i++) {
            px[i] = currentMino.b[i].x;
            py[i] = currentMino.b[i].y;
        }
        // Simula queda até o chão ou colisão
        boolean collision;
        do {
            collision = false;
            for (int i = 0; i < 4; i++) {
                int testY = py[i] + Block.SIZE;
                if (testY + Block.SIZE > bottom_y) {
                    collision = true;
                    break;
                }
                for (Block b : staticBlocks) {
                    if (b.x == px[i] && b.y == testY) {
                        collision = true;
                        break;
                    }
                }
                if (collision) break;
            }
            if (!collision) {
                for (int i = 0; i < 4; i++) py[i] += Block.SIZE;
            }
        } while (!collision);

        // Desenha a sombra da peça
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(new Color(180, 210, 255, 180));
        for (int i = 0; i < 4; i++) {
            g2.fillRect(px[i] + 2, py[i] + 2, Block.SIZE - 4, Block.SIZE - 4);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public int getButtonsY() {
        int pmScoreY = leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET;
        return pmScoreY + 60;
    }
    public int getScoreTop() { return leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET; }
    public int getInfoBottom() {
        int pmScoreY = getScoreTop();
        int labelY = pmScoreY + 22;
        int statsY = labelY + 20;
        int keysStartY = statsY + 56;
        int keyLineStep = 20;
        int keysCount = 7;
        return keysStartY + (keysCount - 1) * keyLineStep;
    }
    public int getScore() { return score; }
    public void setAltPieceColor(boolean pastel) {
        Block.setPastelColorMode(pastel);
        // Atualiza as peças atuais e próximas para garantir que usem o modo correto
        if (currentMino != null && currentMino.b != null) {
            for (Block b : currentMino.b) {
                b.c = pastel ? b.c : b.c; // Não altera a cor, pois o draw já usa pastelColorMode
            }
        }
        if (nextMino != null && nextMino.b != null) {
            for (Block b : nextMino.b) {
                b.c = pastel ? b.c : b.c;
            }
        }
        // Atualiza todos os blocos estáticos também
        for (Block b : staticBlocks) {
            b.c = pastel ? b.c : b.c;
        }
    }

    private void drawLeftPanel(Graphics2D g2) {
        GradientPaint panelGP = new GradientPaint(leftPanelX, leftPanelY, new Color(40,43,50,235), leftPanelX, leftPanelY + leftPanelH, new Color(25,27,30,235));
        g2.setPaint(panelGP);
        RoundRectangle2D rr = new RoundRectangle2D.Double(leftPanelX, leftPanelY, leftPanelW, leftPanelH, 28, 28);
        g2.fill(rr);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(160,160,170,180));
        g2.draw(rr);
        g2.setStroke(new BasicStroke(1f));

        String title = "Tetris_Game";
        Font titleFont = new Font("Verdana", Font.BOLD, 28);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics(titleFont);
        int tx = leftPanelX + (leftPanelW - fm.stringWidth(title)) / 2;
        int ty = leftPanelY + 44;
        g2.setColor(new Color(255,255,255,45));
        g2.drawString(title, tx+2, ty+2);
        g2.setColor(new Color(255,255,255));
        g2.drawString(title, tx, ty);

        int pbx = leftPanelX + (leftPanelW - PREVIEW_BOX_SIZE) / 2;
        int pby = leftPanelY + PREVIEW_BOX_TOP_OFFSET;
        GradientPaint pbGP = new GradientPaint(pbx, pby, new Color(55,60,70), pbx, pby + PREVIEW_BOX_SIZE, new Color(30,32,36));
        g2.setPaint(pbGP);
        RoundRectangle2D preview = new RoundRectangle2D.Double(pbx, pby, PREVIEW_BOX_SIZE, PREVIEW_BOX_SIZE, 22, 22);
        g2.fill(preview);
        g2.setColor(new Color(200,200,210));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(preview);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String nextLabel = "Próxima peça";
        int nx = pbx + (PREVIEW_BOX_SIZE - g2.getFontMetrics().stringWidth(nextLabel)) / 2;
        int ny = pby + 28;
        g2.setColor(new Color(230,230,230));
        g2.drawString(nextLabel, nx, ny);

        if (nextMino != null) {
            int centerPX = pbx + (PREVIEW_BOX_SIZE / 2);
            int centerPY = pby + (PREVIEW_BOX_SIZE / 2) + 6;
            nextMino.setXY(centerPX - Block.SIZE, centerPY - Block.SIZE);
        }
    }

    private void drawRightPanel(Graphics2D g2) {
        RoundRectangle2D rr = new RoundRectangle2D.Double(rightPanelX, rightPanelY, rightPanelW, rightPanelH, 28, 28);
        GradientPaint panelGP = new GradientPaint(rightPanelX, rightPanelY, new Color(40,43,50,235), rightPanelX, rightPanelY + rightPanelH, new Color(25,27,30,235));
        g2.setPaint(panelGP);
        g2.fill(rr);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(160,160,170,180));
        g2.draw(rr);
        g2.setStroke(new BasicStroke(1f));
    }

    public int getRightPanelX() { return rightPanelX; }
    public int getRightPanelY() { return rightPanelY; }
    public int getRightPanelW() { return rightPanelW; }
    public int getRightPanelH() { return rightPanelH; }
    public int getLeftPanelX() { return leftPanelX; }
    public int getLeftPanelY() { return leftPanelY; }
    public int getLeftPanelW() { return leftPanelW; }
    public int getLeftPanelH() { return leftPanelH; }
}