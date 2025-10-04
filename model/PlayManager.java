package model;

import pieces.Mino_L1;
import pieces.Mino_L2;
import pieces.Mino_Square;
import pieces.Mino_T;
import pieces.Mino_Z1;
import pieces.Mino_Z2;
import view.GamePanel;

import java.awt.*;
import java.util.ArrayList;

import java.util.Random;

import controller.KeyHandler;
import java.awt.geom.RoundRectangle2D;

public class PlayManager {


    final int WIDTH = Block.SIZE * 10; 
    final int HEIGHT = Block.SIZE * 20; 

    public static int right_x;
    public static int left_x;
    public static int top_y;
    public static int bottom_y;


    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;

    Mino nextMino;  
    final int NEXT_MINO_X;
    final int NEXT_MINO_Y;

    // Dimensões do painel lateral esquerdo (informações e próxima peça)
    int leftPanelX;
    int leftPanelY;
    int leftPanelW;
    int leftPanelH;
    final int LEFT_PANEL_GAP = 24; // espaço entre painel e tabuleiro
    // Dimensões da caixa de preview da próxima peça
    final int PREVIEW_BOX_SIZE = 170; // aumentado (antes 130)
    final int PREVIEW_BOX_TOP_OFFSET = 70; // distância do topo do painel
    // deslocamento adicional para o bloco de informações (pontuação / controles)
    final int INFO_TOP_OFFSET = 70; // aumente para empurrar as infos para baixo
    // Painel direito (apenas moldura vazia)
    int rightPanelX;
    int rightPanelY;
    int rightPanelW;
    int rightPanelH;
    public static ArrayList<Block> staticBlocks = new ArrayList<>(); 

    public static int dropInterval = 40;
    public boolean gameOver;



    boolean effectsCounterOn;

    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    int level = 1;
    int lines;
    int score;


    public PlayManager() {
        // Centralização vertical do tabuleiro
        top_y = (GamePanel.HEIGHT - HEIGHT) / 2;
        if (top_y < 20) top_y = 20;

        // Painel esquerdo maior com margem da borda da janela
        leftPanelX = 40;
        leftPanelY = top_y;
    leftPanelW = 250; // largura aumentada (antes 210)
        leftPanelH = HEIGHT;

        // Painel direito (somente moldura) com mesma largura e margens simétricas
        rightPanelW = leftPanelW;
        rightPanelH = HEIGHT;
        int rightMargin = 40; // margem direita
        rightPanelX = GamePanel.WIDTH - rightMargin - rightPanelW;
        rightPanelY = top_y;

        // Centralização do tabuleiro levando em conta os dois painéis
        int innerLeftLimit = leftPanelX + leftPanelW + LEFT_PANEL_GAP;
        int innerRightLimit = rightPanelX - LEFT_PANEL_GAP; // começo onde painel direito inicia
        int availableWidth = innerRightLimit - innerLeftLimit;
        left_x = innerLeftLimit + (availableWidth - WIDTH) / 2;
        right_x = left_x + WIDTH;
        bottom_y = top_y + HEIGHT;

        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        // Centraliza a preview dentro do painel
    NEXT_MINO_X = leftPanelX + (leftPanelW / 2) - Block.SIZE; // continua centralizado horizontalmente no painel
    // Centraliza verticalmente dentro da caixa de preview
    NEXT_MINO_Y = leftPanelY + PREVIEW_BOX_TOP_OFFSET + (PREVIEW_BOX_SIZE / 2) - Block.SIZE;


        currentMino = minoPicker();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = new Mino_Bar();
        nextMino.setXY(NEXT_MINO_X, NEXT_MINO_Y);

        // sem ícones de painel (pausa/perfil) — elementos removidos conforme solicitado
    }

    // Retorna uma Y adequada para posicionar botões abaixo da pontuação/preview
    public int getButtonsY() {
        // corresponde ao cálculo usado para desenhar a pontuação em draw()
        int pmScoreY = leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET;
        // colocar os botões algumas linhas abaixo do número para evitar sobreposição
        return pmScoreY + 60;
    }

    // Retorna o Y (topo / baseline) onde a pontuação é desenhada (usado para calcular posicionamento de UI externo)
    public int getScoreTop() {
        return leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET;
    }

    // Retorna a Y do final do bloco de informações (pontuação + label + stats + mapeamento de teclas)
    public int getInfoBottom() {
        int pmScoreY = getScoreTop();
        int labelY = pmScoreY + 22;
        int statsY = labelY + 20;
        int keysStartY = statsY + 56; // mover o mapeamento para baixo
        int keyLineStep = 20;
        int keysCount = 7; // número de linhas do mapeamento de teclas
        int lastKeyY = keysStartY + (keysCount - 1) * keyLineStep;
        return lastKeyY; // já é o y da linha final
    }

    private Mino minoPicker() {
        Mino mino = null;
        int i = new Random().nextInt(7);

        mino = switch (i) {
            case 0 -> new Mino_L1();
            case 1 -> new Mino_L2();
            case 2 -> new Mino_Square();
            case 3 -> new Mino_Bar();
            case 4 -> new Mino_T();
            case 5 -> new Mino_Z1();
            case 6 -> new Mino_Z2();
            default -> mino;
        };
        return mino;
    }

    public void update() {

        if(!KeyHandler.gamestart)
        {

        }
        else{
            if (!currentMino.active) {

                staticBlocks.add(currentMino.b[0]);
                staticBlocks.add(currentMino.b[1]);
                staticBlocks.add(currentMino.b[2]);
                staticBlocks.add(currentMino.b[3]);

                if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
                    gameOver = true;
                    GamePanel.music.stop();
                    GamePanel.se.play(2,false);
                }

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
        int cols = WIDTH / Block.SIZE;
        int rows = HEIGHT / Block.SIZE;

        if (staticBlocks.isEmpty()) return;

        // Construir grade lógica de ocupação
        boolean[][] occupied = new boolean[rows][cols];
        for (Block b : staticBlocks) {
            int col = (b.x - left_x) / Block.SIZE;
            int row = (b.y - top_y) / Block.SIZE;
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                occupied[row][col] = true;
            }
        }

        ArrayList<Integer> fullRows = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            boolean full = true;
            for (int c = 0; c < cols; c++) {
                if (!occupied[r][c]) { full = false; break; }
            }
            if (full) fullRows.add(r);
        }

        if (fullRows.isEmpty()) return;

        // Marcar para efeito visual
        effectsCounterOn = true;
        effectY.clear();
        for (int r : fullRows) {
            effectY.add(top_y + r * Block.SIZE);
        }

        // Remover blocos de linhas completas
        for (int i = staticBlocks.size() - 1; i >= 0; i--) {
            Block b = staticBlocks.get(i);
            int r = (b.y - top_y) / Block.SIZE;
            if (r >= 0 && fullRows.contains(r)) {
                staticBlocks.remove(i);
            }
        }

        // Descer blocos acima
        int clearedCount = fullRows.size();
        // Ordena as linhas limpas para processar deslocamento corretamente
        fullRows.sort(Integer::compareTo);
        for (int clearedIndex = 0; clearedIndex < fullRows.size(); clearedIndex++) {
            int clearedRow = fullRows.get(clearedIndex);
            for (Block b : staticBlocks) {
                int r = (b.y - top_y) / Block.SIZE;
                if (r < clearedRow) {
                    b.y += Block.SIZE; // desce 1 por linha removida abaixo
                }
            }
            // Ajusta linhas subsequentes (porque após descer, próximas referências mudam de posição relativa)
            for (int j = clearedIndex + 1; j < fullRows.size(); j++) {
                fullRows.set(j, fullRows.get(j) + 1); // compensa deslocamento
            }
        }

        lines += clearedCount;
        if (lines > 0 && lines % 10 == 0 && dropInterval > 1) {
            level++;
            if (dropInterval > 10) dropInterval -= 10; else dropInterval -= 1;
        }

        GamePanel.se.play(1, false);
        int add = switch (clearedCount) {
            case 1 -> 40;
            case 2 -> 100;
            case 3 -> 300;
            case 4 -> 1200;
            default -> 0;
        };
        score += add;
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2.setColor(Color.BLACK);
        g2.fillRect(left_x + 6, top_y + 8, WIDTH, HEIGHT);
        g2.setComposite(oldComp);

        GradientPaint playBG = new GradientPaint(left_x, top_y, new Color(60, 63, 65), right_x, bottom_y, new Color(25, 28, 30));
        g2.setPaint(playBG);
        g2.fillRect(left_x, top_y, WIDTH, HEIGHT);

        GradientPaint highlight = new GradientPaint(left_x, top_y, new Color(255,255,255,30), left_x, top_y + HEIGHT/2, new Color(255,255,255,6));
        g2.setPaint(highlight);
        g2.fillRect(left_x + 4, top_y + 4, WIDTH - 8, HEIGHT/2);

        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(5f));
        GradientPaint borderGP = new GradientPaint(left_x, top_y, new Color(200,200,200,200), left_x, top_y + HEIGHT, new Color(120,120,120,200));
        g2.setPaint(borderGP);
        g2.drawRect(left_x - 4, top_y - 2, WIDTH + 8, HEIGHT + 8);
        g2.setStroke(oldStroke);

        // Grade interna do tabuleiro
        int cols = WIDTH / Block.SIZE;
        int rows = HEIGHT / Block.SIZE;
        g2.setColor(new Color(255,255,255,30));
        Stroke gridOld = g2.getStroke();
        g2.setStroke(new BasicStroke(1f));
        for (int ci = 1; ci < cols; ci++) {
            int gx = left_x + ci * Block.SIZE;
            g2.drawLine(gx, top_y, gx, bottom_y);
        }
        for (int ri = 1; ri < rows; ri++) {
            int gy = top_y + ri * Block.SIZE;
            g2.drawLine(left_x, gy, right_x, gy);
        }
        g2.setStroke(gridOld);

    // Painéis laterais
    drawLeftPanel(g2);
    drawRightPanel(g2);

        // Pontuação grande e centralizada abaixo da caixa de preview
    Font pmScoreFont = new Font("SansSerif", Font.BOLD, 56);
    g2.setFont(pmScoreFont);
    String scoreStr = String.valueOf(score);
    FontMetrics pmFmScore = g2.getFontMetrics(pmScoreFont);
    int centerX = leftPanelX + (leftPanelW / 2);
    int pmScoreY = leftPanelY + PREVIEW_BOX_TOP_OFFSET + PREVIEW_BOX_SIZE + INFO_TOP_OFFSET; // posição vertical abaixo da preview, deslocada para baixo
    int pmScoreX = centerX - (pmFmScore.stringWidth(scoreStr) / 2);
    g2.setColor(new Color(255,215,0)); // cor da pontuação
    g2.drawString(scoreStr, pmScoreX, pmScoreY);

        // Rótulo "Pontuação" abaixo do número
        Font labelFont = new Font("SansSerif", Font.PLAIN, 16);
        g2.setFont(labelFont);
        String scoreLabel = "Pontuação";
        int labelX = centerX - (g2.getFontMetrics(labelFont).stringWidth(scoreLabel) / 2);
    int labelY = pmScoreY + 22;
        g2.setColor(new Color(230,230,230));
        g2.drawString(scoreLabel, labelX, labelY);

    // Nível e Linhas em texto maior, centrados abaixo do rótulo
    Font smallFont = new Font("SansSerif", Font.BOLD, 18);
        g2.setFont(smallFont);
        String stats = "Nível: " + level + "   Linhas: " + lines;
        int statsX = centerX - (g2.getFontMetrics(smallFont).stringWidth(stats) / 2);
    int statsY = labelY + 20;
        g2.setColor(new Color(185,195,205));
        g2.drawString(stats, statsX, statsY);

        // Mapeamento de teclas (centralizado abaixo das estatísticas)
        Font keysFont = new Font("SansSerif", Font.PLAIN, 14);
        g2.setFont(keysFont);
        g2.setColor(new Color(220,220,220));
        String[] keys = new String[] {
                "Controles:",
                "Mover esquerda: A",
                "Mover direita:  D",
                "Descer: S",
                "Girar: W",
                "Pausar: Espaço",
                "Música: M"
        };
    int keysStartY = statsY + 56; // mais espaço entre estatísticas e mapeamento de teclas
        int keyLineStep = 20;
        for (int k = 0; k < keys.length; k++) {
            String line = keys[k];
            int kx = centerX - (g2.getFontMetrics(keysFont).stringWidth(line) / 2);
            int ky = keysStartY + (k * keyLineStep);
            // destacar o título
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

        if (currentMino != null) {
            currentMino.draw(g2);
        }
        nextMino.draw(g2);
        for (Block staticBlock : staticBlocks) {
            staticBlock.draw(g2);
        }

        if (effectsCounterOn) {
            effectCounter++;
            g2.setColor(Color.RED);
            for (Integer integer : effectY) {
                g2.fillRect(left_x, integer, WIDTH, Block.SIZE);
            }
            if (effectCounter == 10) {
                effectsCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }
        }

        g2.setColor(Color.YELLOW);
        if (gameOver) {
            Paint oldPaint = g2.getPaint();
            Composite oldComposite = g2.getComposite();

            GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 0, 200), 0, GamePanel.HEIGHT, new Color(40, 40, 40, 220));
            g2.setPaint(gp);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.88f));
            g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

            g2.setPaint(oldPaint);
            g2.setComposite(oldComposite);

            String title = "Você perdeu";
            Font titleFont = new Font("Verdana", Font.BOLD, 64);
            g2.setFont(titleFont);
            FontMetrics fmTitle = g2.getFontMetrics(titleFont);
            int titleWidth = fmTitle.stringWidth(title);

            String scoreText = "Pontuação: " + score;
            Font scoreFont = new Font("SansSerif", Font.BOLD, 36);
            g2.setFont(scoreFont);
            FontMetrics fmScore = g2.getFontMetrics(scoreFont);
            int scoreWidth = fmScore.stringWidth(scoreText);

            int titleX = (GamePanel.WIDTH - titleWidth) / 2;
            int titleY = (GamePanel.HEIGHT / 2);

            g2.setColor(Color.YELLOW);
            g2.setFont(titleFont);
            g2.drawString(title, titleX, titleY);

            int scoreX = (GamePanel.WIDTH - scoreWidth) / 2;
            int scoreY = titleY + fmTitle.getDescent() + 40 + fmScore.getAscent();
            g2.setFont(scoreFont);
            g2.drawString(scoreText, scoreX, scoreY);
        } else if (KeyHandler.pausePressed) {
            String paused = "Pausado";
            Font pausedFont = new Font("Verdana", Font.BOLD, 50);
            g2.setFont(pausedFont);
            int pausedWidth = g2.getFontMetrics().stringWidth(paused);
            int pausedX = left_x + (WIDTH - pausedWidth) / 2;
            int pausedY = top_y + (HEIGHT / 2);
            g2.drawString(paused, pausedX, pausedY);
        }

        // O título agora é desenhado dentro do painel (já desenhado em drawLeftPanel)

    }

    private void drawLeftPanel(Graphics2D g2) {
        // Fundo com gradiente
    GradientPaint panelGP = new GradientPaint(leftPanelX, leftPanelY, new Color(40,43,50,235), leftPanelX, leftPanelY + leftPanelH, new Color(25,27,30,235));
        g2.setPaint(panelGP);
        RoundRectangle2D rr = new RoundRectangle2D.Double(leftPanelX, leftPanelY, leftPanelW, leftPanelH, 28, 28);
        g2.fill(rr);

        // Borda
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(160,160,170,180));
        g2.draw(rr);
        g2.setStroke(new BasicStroke(1f));

        // Título estilizado
        String title = "Tetris_Game";
    Font titleFont = new Font("Verdana", Font.BOLD, 28);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics(titleFont);
        int tx = leftPanelX + (leftPanelW - fm.stringWidth(title)) / 2;
    int ty = leftPanelY + 44;
        // Glow simples
        g2.setColor(new Color(255,255,255,45));
        g2.drawString(title, tx+2, ty+2);
        g2.setColor(new Color(255,255,255));
        g2.drawString(title, tx, ty);

        // Caixa da próxima peça (ampliada)
        int previewBoxSize = PREVIEW_BOX_SIZE;
        int pbx = leftPanelX + (leftPanelW - previewBoxSize) / 2;
        int pby = leftPanelY + PREVIEW_BOX_TOP_OFFSET;
        GradientPaint pbGP = new GradientPaint(pbx, pby, new Color(55,60,70), pbx, pby + previewBoxSize, new Color(30,32,36));
        g2.setPaint(pbGP);
        RoundRectangle2D preview = new RoundRectangle2D.Double(pbx, pby, previewBoxSize, previewBoxSize, 22, 22);
        g2.fill(preview);
        g2.setColor(new Color(200,200,210));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(preview);
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String nextLabel = "Próxima peça";
        int nx = pbx + (previewBoxSize - g2.getFontMetrics().stringWidth(nextLabel)) / 2;
        int ny = pby + 28;
        g2.setColor(new Color(230,230,230));
        g2.drawString(nextLabel, nx, ny);

        // Centralizar a próxima peça dentro da caixa de preview
        if (nextMino != null) {
            int centerPX = pbx + (previewBoxSize / 2);
            int centerPY = pby + (previewBoxSize / 2) + 6; // pequeno ajuste vertical
            // ajusta considerando o tamanho de um bloco
            nextMino.setXY(centerPX - Block.SIZE, centerPY - Block.SIZE);
        }

    }

    // Painel direito apenas com moldura vazia
    private void drawRightPanel(Graphics2D g2) {
        RoundRectangle2D rr = new RoundRectangle2D.Double(rightPanelX, rightPanelY, rightPanelW, rightPanelH, 28, 28);
        // Fundo com o mesmo gradiente do painel esquerdo
        GradientPaint panelGP = new GradientPaint(rightPanelX, rightPanelY, new Color(40,43,50,235), rightPanelX, rightPanelY + rightPanelH, new Color(25,27,30,235));
        g2.setPaint(panelGP);
        g2.fill(rr);
        // Borda
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(160,160,170,180));
        g2.draw(rr);
        g2.setStroke(new BasicStroke(1f));

    // Espaçamento superior do painel direito (ícones removidos)
    // (margem superior do painel direita mantida implicitamente)

        // (ícone de perfil removido)
    }
    // painel direito sem áreas clicáveis para pausa/perfil
    // getters públicos para uso pelo UI (centralização de controles)
    public int getRightPanelX() { return rightPanelX; }
    public int getRightPanelY() { return rightPanelY; }
    public int getRightPanelW() { return rightPanelW; }
    public int getRightPanelH() { return rightPanelH; }

    // getters do painel esquerdo para permitir posicionamento de componentes dentro dele
    public int getLeftPanelX() { return leftPanelX; }
    public int getLeftPanelY() { return leftPanelY; }
    public int getLeftPanelW() { return leftPanelW; }
    public int getLeftPanelH() { return leftPanelH; }

}
