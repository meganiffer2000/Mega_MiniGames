package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Bomba!: abra caminho entre caixotes, elimine os monstros e sobreviva a três fases. */
public final class BombermanScreen extends ArcadeScreen {
    private static final int COLS = 13;
    private static final int ROWS = 9;
    private static final int MAX_LEVEL = 3;
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final int[][] fire = new int[ROWS][COLS];
    private boolean[][] solid;
    private boolean[][] crates;
    private int playerCol;
    private int playerRow;
    private int lives;
    private int level;
    private int score;
    private int cratesLeft;
    private int invulnerable;
    private int moveCooldown;
    private int heldHorizontal;
    private int heldVertical;
    private int explodeTicks;
    private int clearedTicks;
    private int ticks;
    private boolean levelCleared;
    private boolean gameOver;
    private boolean won;
    private int cellSize;
    private int boardLeft;
    private int boardTop;

    public BombermanScreen() {
        super("Bomba!");
    }

    @Override
    protected void init() {
        cellSize = Math.max(12, Math.min(34, Math.min((width - 36) / COLS, (height - 128) / ROWS)));
        boardLeft = (width - COLS * cellSize) / 2;
        boardTop = 64 + Math.max(0, (height - 128 - ROWS * cellSize) / 2);
        addFooterButtons(height - 26, this::restartMatch);
        startLevel();
    }

    private void restartMatch() {
        lives = 3;
        level = 1;
        score = 0;
        gameOver = false;
        won = false;
        startLevel();
    }

    private void startLevel() {
        solid = new boolean[ROWS][COLS];
        crates = new boolean[ROWS][COLS];
        bombs.clear();
        enemies.clear();
        levelCleared = false;
        clearedTicks = 0;
        invulnerable = 60;
        moveCooldown = 0;
        heldHorizontal = 0;
        heldVertical = 0;
        explodeTicks = 0;
        cratesLeft = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                solid[row][col] = row == 0 || col == 0 || row == ROWS - 1 || col == COLS - 1
                        || (row % 2 == 0 && col % 2 == 0);
                fire[row][col] = 0;
            }
        }
        for (int row = 1; row < ROWS - 1; row++) {
            for (int col = 1; col < COLS - 1; col++) {
                if (solid[row][col]) continue;
                if (col <= 2 && row <= 2) continue;
                if (ThreadLocalRandom.current().nextInt(100) < 52) {
                    crates[row][col] = true;
                    cratesLeft++;
                }
            }
        }
        playerCol = 1;
        playerRow = 1;
        int enemyCount = 2 + level;
        for (int index = 0; index < enemyCount; index++) {
            for (int attempt = 0; attempt < 80; attempt++) {
                int col = 1 + ThreadLocalRandom.current().nextInt(COLS - 2);
                int row = 1 + ThreadLocalRandom.current().nextInt(ROWS - 2);
                if (solid[row][col] || crates[row][col]) continue;
                if (Math.abs(col - playerCol) + Math.abs(row - playerRow) < 5) continue;
                Enemy enemy = new Enemy();
                enemy.col = col;
                enemy.row = row;
                enemy.cooldown = 8 + ThreadLocalRandom.current().nextInt(20);
                enemies.add(enemy);
                break;
            }
        }
    }

    private boolean walkable(int col, int row) {
        return col >= 0 && row >= 0 && col < COLS && row < ROWS
                && !solid[row][col] && !crates[row][col] && bombAt(col, row) == null;
    }

    private Bomb bombAt(int col, int row) {
        for (Bomb bomb : bombs) {
            if (bomb.col == col && bomb.row == row) return bomb;
        }
        return null;
    }

    private void movePlayer(int dCol, int dRow) {
        if (levelCleared || gameOver) return;
        int targetCol = playerCol + dCol;
        int targetRow = playerRow + dRow;
        if (!walkable(targetCol, targetRow)) return;
        playerCol = targetCol;
        playerRow = targetRow;
        touchCheck();
    }

    private void placeBomb() {
        if (levelCleared || gameOver) return;
        if (bombAt(playerCol, playerRow) != null) return;
        if (bombs.size() >= 1 + (level > 2 ? 1 : 0)) return;
        Bomb bomb = new Bomb();
        bomb.col = playerCol;
        bomb.row = playerRow;
        bomb.fuse = 62;
        bomb.range = 2 + (level >= 3 ? 1 : 0);
        bombs.add(bomb);
    }

    @Override
    public void tick() {
        ticks++;
        if (gameOver) return;
        if (levelCleared) {
            clearedTicks++;
            if (clearedTicks > 55) {
                level++;
                score += 100;
                if (level > MAX_LEVEL) {
                    won = true;
                    gameOver = true;
                } else {
                    startLevel();
                }
            }
            return;
        }
        if (invulnerable > 0) invulnerable--;
        if (explodeTicks > 0) explodeTicks--;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (fire[row][col] > 0) fire[row][col]--;
            }
        }
        if (moveCooldown > 0) moveCooldown--;
        if (moveCooldown == 0 && (heldHorizontal != 0 || heldVertical != 0)) {
            movePlayer(heldHorizontal, heldVertical);
            moveCooldown = 6;
        }
        for (Bomb bomb : bombs) {
            bomb.fuse--;
        }
        Iterator<Bomb> iterator = bombs.iterator();
        List<Bomb> exploding = new ArrayList<>();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            if (bomb.fuse <= 0) {
                iterator.remove();
                exploding.add(bomb);
            }
        }
        for (Bomb bomb : exploding) {
            explode(bomb);
        }
        for (Enemy enemy : enemies) {
            enemy.cooldown--;
            if (enemy.cooldown > 0) continue;
            enemy.cooldown = 13 + ThreadLocalRandom.current().nextInt(12);
            if (ThreadLocalRandom.current().nextInt(100) < 70) {
                int[] dir = DIRS[ThreadLocalRandom.current().nextInt(DIRS.length)];
                if (walkable(enemy.col + dir[0], enemy.row + dir[1])) {
                    enemy.col += dir[0];
                    enemy.row += dir[1];
                }
            }
        }
        touchCheck();
        if (cratesLeft <= 0 && enemies.isEmpty() && !levelCleared) {
            levelCleared = true;
            clearedTicks = 0;
            score += 60;
        }
    }

    private void touchCheck() {
        if (invulnerable > 0 || gameOver) return;
        for (Enemy enemy : enemies) {
            if (enemy.col == playerCol && enemy.row == playerRow) {
                hitPlayer();
                return;
            }
        }
        if (fire[playerRow][playerCol] > 0) {
            hitPlayer();
        }
    }

    private void hitPlayer() {
        lives--;
        invulnerable = 70;
        bombs.clear();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) fire[row][col] = 0;
        }
        playerCol = 1;
        playerRow = 1;
        if (lives <= 0) {
            gameOver = true;
        }
    }

    private void explode(Bomb bomb) {
        explodeTicks = 6;
        addFire(bomb.row, bomb.col);
        for (int[] dir : DIRS) {
            for (int step = 1; step <= bomb.range; step++) {
                int col = bomb.col + dir[0] * step;
                int row = bomb.row + dir[1] * step;
                if (solid[row][col]) break;
                addFire(row, col);
                if (crates[row][col]) {
                    crates[row][col] = false;
                    cratesLeft--;
                    score += 20;
                    break;
                }
                Bomb chained = bombAt(col, row);
                if (chained != null) {
                    chained.fuse = 1;
                    break;
                }
            }
        }
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (fire[enemy.row][enemy.col] > 0) {
                iterator.remove();
                score += 50;
            }
        }
        if (fire[playerRow][playerCol] > 0) {
            hitPlayer();
        }
    }

    private void addFire(int row, int col) {
        if (row < 0 || col < 0 || row >= ROWS || col >= COLS) return;
        fire[row][col] = 16;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restartMatch();
            return true;
        }
        if (keyCode == 32) {
            placeBomb();
            return true;
        }
        int horizontal = 0;
        int vertical = 0;
        switch (keyCode) {
            case 263, 65 -> horizontal = -1;
            case 262, 68 -> horizontal = 1;
            case 265, 87 -> vertical = -1;
            case 264, 83 -> vertical = 1;
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        heldHorizontal = horizontal;
        heldVertical = vertical;
        moveCooldown = 0;
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263, 65, 262, 68 -> heldHorizontal = 0;
            case 265, 87, 264, 83 -> heldVertical = 0;
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (won ? "Você dominou a mina!" : "Os monstros venceram")
                        : "Setas/WASD movem  •  Espaço planta bomba  •  R reinicia"),
                width / 2, 32, gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER) : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "FASE " + level + "/" + MAX_LEVEL, boardLeft, 47, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "PONTOS " + score, boardLeft + COLS * cellSize, 47,
                ArcadeTheme.GOLD);
        for (int index = 0; index < lives; index++) {
            ArcadeTheme.heart(context, width / 2 - 8 + index * 12, 53, 4, ArcadeTheme.CORAL);
        }

        int boardWidth = COLS * cellSize;
        int boardHeight = ROWS * cellSize;
        ArcadeTheme.rounded(context, boardLeft - 5, boardTop - 5, boardLeft + boardWidth + 5,
                boardTop + boardHeight + 5, 10, 0x5535C8B9);
        ArcadeTheme.rounded(context, boardLeft - 4, boardTop - 4, boardLeft + boardWidth + 4,
                boardTop + boardHeight + 4, 9, 0xF0091424);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = boardLeft + col * cellSize;
                int y = boardTop + row * cellSize;
                context.fill(x, y, x + cellSize, y + cellSize,
                        (row + col) % 2 == 0 ? 0xFF122437 : 0xFF0E1D2D);
                if (solid[row][col]) {
                    drawSteel(context, x, y);
                } else if (crates[row][col]) {
                    drawCrate(context, x, y);
                }
            }
        }
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (fire[row][col] <= 0) continue;
                int x = boardLeft + col * cellSize;
                int y = boardTop + row * cellSize;
                int centerX = x + cellSize / 2;
                int centerY = y + cellSize / 2;
                ArcadeTheme.glow(context, centerX, centerY, cellSize / 2 + 3, 0x66FF8A2B);
                ArcadeTheme.rounded(context, x + 2, y + 2, x + cellSize - 2, y + cellSize - 2,
                        Math.max(2, cellSize / 4), 0xFFFFA53C);
                ArcadeTheme.disc(context, centerX, centerY, Math.max(2, cellSize / 5), 0xFFFFF0B4);
            }
        }
        for (Bomb bomb : bombs) {
            drawBomb(context, bomb);
        }
        for (Enemy enemy : enemies) {
            drawEnemy(context, enemy);
        }
        drawPlayer(context);

        if (levelCleared) {
            ArcadeTheme.banner(context, textRenderer, "FASE LIMPA!",
                    level >= MAX_LEVEL ? "Última fase concluída" : "Preparando a fase " + (level + 1),
                    width / 2, boardTop + boardHeight / 2, Math.min(width - 20, boardWidth),
                    ArcadeTheme.SUCCESS);
        } else if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, won ? "VOCÊ VENCEU!" : "FIM DE JOGO",
                    "Pontos: " + score, width / 2, boardTop + boardHeight / 2,
                    Math.min(width - 20, boardWidth), won ? ArcadeTheme.GOLD : ArcadeTheme.DANGER);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSteel(DrawContext context, int x, int y) {
        ArcadeTheme.rounded(context, x + 1, y + 1, x + cellSize - 1, y + cellSize - 1,
                Math.max(2, cellSize / 6), 0xFF34506E);
        ArcadeTheme.rounded(context, x + 1, y + 1, x + cellSize - 1, y + cellSize / 2,
                Math.max(1, cellSize / 7), 0xFF4C6E92);
        ArcadeTheme.rounded(context, x + 3, y + 3, x + cellSize - 3, y + 4, 1, 0xFF7B9CBD);
        ArcadeTheme.disc(context, x + cellSize / 3, y + cellSize / 3, 1, 0xFF243B53);
        ArcadeTheme.disc(context, x + cellSize * 2 / 3, y + cellSize * 2 / 3, 1, 0xFF243B53);
    }

    private void drawCrate(DrawContext context, int x, int y) {
        int inset = Math.max(1, cellSize / 8);
        ArcadeTheme.rounded(context, x + inset, y + inset, x + cellSize - inset, y + cellSize - inset,
                Math.max(1, cellSize / 8), 0xFF8A5A2E);
        ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                y + cellSize - inset - 1, Math.max(1, cellSize / 9), 0xFFB87A42);
        context.fillGradient(x + inset + 2, y + inset + 1, x + cellSize - inset - 2, y + cellSize / 2,
                0x44FFE0B0, 0x00000000);
        context.fill(x + inset + 2, y + cellSize / 2, x + cellSize - inset - 2, y + cellSize / 2 + 1,
                0xFF7A4A24);
        context.fill(x + cellSize / 2, y + inset + 2, x + cellSize / 2 + 1, y + cellSize - inset - 2,
                0xFF7A4A24);
    }

    private void drawBomb(DrawContext context, Bomb bomb) {
        int x = boardLeft + bomb.col * cellSize;
        int y = boardTop + bomb.row * cellSize;
        int centerX = x + cellSize / 2;
        int centerY = y + cellSize / 2;
        boolean blink = (bomb.fuse / 5) % 2 == 0;
        ArcadeTheme.glow(context, centerX, centerY, cellSize / 2 + 2, blink ? 0x66FF6E6E : 0x33FF6E6E);
        ArcadeTheme.disc(context, centerX, centerY + 1, Math.max(4, cellSize / 3), 0xFF08111E);
        ArcadeTheme.disc(context, centerX, centerY, Math.max(3, cellSize / 3 - 1), 0xFF1B2230);
        ArcadeTheme.disc(context, centerX - Math.max(1, cellSize / 8), centerY - Math.max(1, cellSize / 8),
                Math.max(1, cellSize / 9), 0x88FFFFFF);
        context.fill(centerX, y + 2, centerX + 1, centerY - cellSize / 4, 0xFFB98A5A);
        if (blink) {
            ArcadeTheme.glow(context, centerX, y + 2, 4, 0xAAFFD66B);
            ArcadeTheme.disc(context, centerX, y + 2, 1, 0xFFFFF3C4);
        }
    }

    private void drawPlayer(DrawContext context) {
        if (gameOver) return;
        if (invulnerable > 0 && (invulnerable / 5) % 2 == 0) return;
        int x = boardLeft + playerCol * cellSize;
        int y = boardTop + playerRow * cellSize;
        int centerX = x + cellSize / 2;
        int centerY = y + cellSize / 2;
        ArcadeTheme.glow(context, centerX, centerY, cellSize / 2, 0x446CC9F5);
        ArcadeTheme.disc(context, centerX, centerY + 1, Math.max(4, cellSize / 3), 0xFF112A44);
        ArcadeTheme.disc(context, centerX, centerY, Math.max(3, cellSize / 3 - 1), 0xFFE8F3FF);
        int helmet = 0xFF3A9BE0;
        ArcadeTheme.rounded(context, centerX - Math.max(3, cellSize / 3), centerY - cellSize / 3,
                centerX + Math.max(3, cellSize / 3), centerY - cellSize / 8, 3, helmet);
        context.fill(centerX - Math.max(2, cellSize / 5), centerY - cellSize / 4,
                centerX + Math.max(2, cellSize / 5), centerY - cellSize / 4 + 1, 0xFF0A1A2A);
        if (cellSize >= 18) {
            context.fill(centerX - 3, centerY + 2, centerX - 2, centerY + 4, 0xFF0A1A2A);
            context.fill(centerX + 2, centerY + 2, centerX + 3, centerY + 4, 0xFF0A1A2A);
        }
    }

    private void drawEnemy(DrawContext context, Enemy enemy) {
        int x = boardLeft + enemy.col * cellSize;
        int y = boardTop + enemy.row * cellSize;
        int centerX = x + cellSize / 2;
        int centerY = y + cellSize / 2;
        int radius = Math.max(4, cellSize / 3);
        int bob = (int) (Math.sin((ticks + enemy.col * 7) * 0.15) * 1.5);
        ArcadeTheme.glow(context, centerX, centerY + bob, radius + 2, 0x44B75CF0);
        ArcadeTheme.disc(context, centerX, centerY + bob + 1, radius, 0xFF401E6B);
        ArcadeTheme.disc(context, centerX, centerY + bob, radius - 1, 0xFF9B5CE0);
        ArcadeTheme.disc(context, centerX - radius / 3, centerY + bob - radius / 4, Math.max(1, radius / 3),
                0xFFFFFFFF);
        ArcadeTheme.disc(context, centerX + radius / 3, centerY + bob - radius / 4, Math.max(1, radius / 3),
                0xFFFFFFFF);
        ArcadeTheme.disc(context, centerX - radius / 3, centerY + bob - radius / 4, 1, 0xFF1A0B2B);
        ArcadeTheme.disc(context, centerX + radius / 3, centerY + bob - radius / 4, 1, 0xFF1A0B2B);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Bomb {
        private int col;
        private int row;
        private int fuse;
        private int range;
    }

    private static final class Enemy {
        private int col;
        private int row;
        private int cooldown;
    }
}
