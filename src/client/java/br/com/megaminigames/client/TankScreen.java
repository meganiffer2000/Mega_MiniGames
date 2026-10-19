package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Batalha de Tanques: avance pelo labirinto, destrua os tanques inimigos e
 * proteja a base. Paredes de tijolo quebram, paredes de aço não.
 */
public final class TankScreen extends ArcadeScreen {
    private static final int COLS = 13;
    private static final int ROWS = 11;
    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};
    private static final int EMPTY = 0;
    private static final int BRICK = 1;
    private static final int STEEL = 2;

    private final List<EnemyTank> enemies = new ArrayList<>();
    private final List<Shell> shells = new ArrayList<>();
    private final List<Boom> booms = new ArrayList<>();
    private int[][] walls;
    private int cell;
    private int boardLeft;
    private int boardTop;
    private int lives;
    private int score;
    private int level;
    private int playerColumn;
    private int playerRow;
    private float playerX;
    private float playerY;
    private int direction;
    private int moveCooldown;
    private int shootCooldown;
    private int invulnerable;
    private int respawnColumn;
    private int respawnRow;
    private int levelTicks;
    private boolean cleared;
    private boolean gameOver;
    private int ticks;
    private int enemyShots;
    private String message = "Setas/WASD movem  •  Espaço atira";

    public TankScreen() {
        super("Batalha de Tanques");
    }

    @Override
    protected void init() {
        cell = Math.max(14, Math.min(30, Math.min((width - 36) / COLS, (height - 168) / ROWS)));
        boardLeft = (width - cell * COLS) / 2;
        boardTop = 62;
        addFooterButtons(height - 26, this::restart);
        restart();
    }

    private void restart() {
        lives = 3;
        score = 0;
        level = 0;
        gameOver = false;
        levelTicks = 0;
        startLevel();
    }

    private void startLevel() {
        walls = new int[ROWS][COLS];
        enemies.clear();
        shells.clear();
        booms.clear();
        cleared = false;
        levelTicks = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                boolean border = row == 0 || row == ROWS - 1 || column == 0 || column == COLS - 1;
                if (border) {
                    walls[row][column] = STEEL;
                } else if (random.nextInt(100) < 26) {
                    walls[row][column] = BRICK;
                } else if ((row + column) % 7 == 0 && random.nextBoolean()) {
                    walls[row][column] = STEEL;
                }
            }
        }
        respawnColumn = COLS / 2 - 1;
        respawnRow = ROWS - 2;
        for (int row = 2; row <= 4; row++) {
            for (int column = respawnColumn - 1; column <= respawnColumn + 1; column++) {
                walls[row][column] = EMPTY;
            }
        }
        for (int column = respawnColumn - 2; column <= respawnColumn + 2; column++) {
            walls[respawnRow][column] = EMPTY;
            walls[respawnRow - 1][column] = EMPTY;
        }
        playerColumn = respawnColumn;
        playerRow = respawnRow;
        playerX = playerColumn;
        playerY = playerRow;
        direction = 0;
        moveCooldown = 0;
        shootCooldown = 0;
        invulnerable = 50;
        int count = Math.min(5, 2 + level);
        int[][] spawns = {{1, 1}, {COLS - 2, 1}, {1, ROWS / 2}, {COLS - 2, ROWS / 2}, {COLS / 2, 1}};
        for (int index = 0; index < count; index++) {
            EnemyTank enemy = new EnemyTank();
            enemy.column = spawns[index][0];
            enemy.row = spawns[index][1];
            enemy.x = enemy.column;
            enemy.y = enemy.row;
            enemy.direction = 2;
            enemy.shootCooldown = 30 + index * 20;
            enemy.thinkCooldown = 20 + index * 9;
            enemy.speed = Math.max(7, 12 - level);
            walls[enemy.row][enemy.column] = EMPTY;
            enemies.add(enemy);
        }
        message = "Fase " + (level + 1) + "  •  " + count + " inimigos";
    }

    private boolean isWall(int column, int row) {
        return column < 0 || column >= COLS || row < 0 || row >= ROWS || walls[row][column] != EMPTY;
    }

    private boolean canMove(int column, int row) {
        return !isWall(column, row);
    }

    @Override
    public void tick() {
        ticks++;
        if (gameOver) return;
        if (cleared) {
            levelTicks++;
            if (levelTicks > 55) {
                level++;
                startLevel();
            }
            return;
        }
        if (invulnerable > 0) invulnerable--;
        if (shootCooldown > 0) shootCooldown--;
        if (moveCooldown > 0) moveCooldown--;
        updatePlayer();
        updateEnemies();
        updateShells();
        updateBooms();
        if (enemies.isEmpty()) {
            cleared = true;
            levelTicks = 0;
            score += 200;
            message = "Fase limpa!";
        }
    }

    private void updatePlayer() {
        int horizontal = 0;
        int vertical = 0;
        if (leftHeld) horizontal = -1;
        if (rightHeld) horizontal = 1;
        if (upHeld) vertical = -1;
        if (downHeld) vertical = 1;
        if (horizontal != 0 || vertical != 0) {
            direction = horizontal < 0 ? 3 : horizontal > 0 ? 1 : vertical < 0 ? 0 : 2;
            if (moveCooldown <= 0) {
                int nextColumn = playerColumn + horizontal;
                int nextRow = playerRow + vertical;
                if (canMove(nextColumn, nextRow)) {
                    playerColumn = nextColumn;
                    playerRow = nextRow;
                    moveCooldown = 6;
                }
            }
        }
        playerX += (playerColumn - playerX) * 0.4F;
        playerY += (playerRow - playerY) * 0.4F;
        if (shootCooldown <= 0 && spaceQueued) {
            fireShell(playerX, playerY, direction, true);
            shootCooldown = 22;
            spaceQueued = false;
        }
    }

    private void updateEnemies() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (EnemyTank enemy : enemies) {
            enemy.x += (enemy.column - enemy.x) * 0.3F;
            enemy.y += (enemy.row - enemy.y) * 0.3F;
            if (enemy.thinkCooldown > 0) enemy.thinkCooldown--;
            if (enemy.thinkCooldown <= 0) {
                enemy.thinkCooldown = 18 + random.nextInt(30);
                int playerDirection = towardPlayer(enemy);
                if (playerDirection >= 0 && random.nextInt(100) < 65) {
                    enemy.direction = playerDirection;
                    enemy.stepCooldown = 0;
                } else {
                    enemy.direction = random.nextInt(4);
                    enemy.stepCooldown = 0;
                }
            }
            if (enemy.stepCooldown > 0) enemy.stepCooldown--;
            if (enemy.stepCooldown <= 0) {
                int nextColumn = enemy.column + DX[enemy.direction];
                int nextRow = enemy.row + DY[enemy.direction];
                if (canMove(nextColumn, nextRow) && !occupiedByTank(nextColumn, nextRow, enemy)) {
                    enemy.column = nextColumn;
                    enemy.row = nextRow;
                    enemy.stepCooldown = enemy.speed;
                } else {
                    enemy.stepCooldown = 8;
                }
            }
            if (enemy.shootCooldown > 0) enemy.shootCooldown--;
            if (enemy.shootCooldown <= 0) {
                enemy.shootCooldown = Math.max(40, 85 - level * 8) + random.nextInt(30);
                fireShell(enemy.x, enemy.y, enemy.direction, false);
            }
        }
    }

    private int towardPlayer(EnemyTank enemy) {
        if (enemy.column == playerColumn) {
            if (clearLine(enemy.column, enemy.row, playerColumn, playerRow, true)) {
                return playerRow < enemy.row ? 0 : 2;
            }
        }
        if (enemy.row == playerRow) {
            if (clearLine(enemy.column, enemy.row, playerColumn, playerRow, false)) {
                return playerColumn < enemy.column ? 3 : 1;
            }
        }
        if (Math.abs(enemy.column - playerColumn) >= Math.abs(enemy.row - playerRow)) {
            return playerColumn < enemy.column ? 3 : 1;
        }
        return playerRow < enemy.row ? 0 : 2;
    }

    private boolean clearLine(int column, int row, int targetColumn, int targetRow, boolean vertical) {
        if (vertical) {
            int from = Math.min(row, targetRow) + 1;
            int to = Math.max(row, targetRow) - 1;
            for (int step = from; step <= to; step++) {
                if (walls[step][column] != EMPTY) return false;
            }
            return column == targetColumn;
        }
        int from = Math.min(column, targetColumn) + 1;
        int to = Math.max(column, targetColumn) - 1;
        for (int step = from; step <= to; step++) {
            if (walls[row][step] != EMPTY) return false;
        }
        return row == targetRow;
    }

    private boolean occupiedByTank(int column, int row, EnemyTank self) {
        if (column == playerColumn && row == playerRow) return true;
        for (EnemyTank enemy : enemies) {
            if (enemy != self && enemy.column == column && enemy.row == row) return true;
        }
        return false;
    }

    private void fireShell(float x, float y, int dir, boolean fromPlayer) {
        Shell shell = new Shell();
        shell.x = (boardLeft + x * cell + cell / 2.0F) + DX[dir] * cell * 0.55F;
        shell.y = (boardTop + y * cell + cell / 2.0F) + DY[dir] * cell * 0.55F;
        shell.vx = DX[dir] * 3.6F;
        shell.vy = DY[dir] * 3.6F;
        shell.fromPlayer = fromPlayer;
        shells.add(shell);
        booms.add(Boom.muzzle(shell.x, shell.y, fromPlayer ? ArcadeTheme.LIME : ArcadeTheme.CORAL));
    }

    private void updateShells() {
        float boardRight = boardLeft + COLS * cell;
        float boardBottom = boardTop + ROWS * cell;
        for (Iterator<Shell> iterator = shells.iterator(); iterator.hasNext(); ) {
            Shell shell = iterator.next();
            shell.x += shell.vx;
            shell.y += shell.vy;
            int column = (int) ((shell.x - boardLeft) / cell);
            int row = (int) ((shell.y - boardTop) / cell);
            if (shell.x < boardLeft || shell.x > boardRight || shell.y < boardTop || shell.y > boardBottom
                    || column < 0 || column >= COLS || row < 0 || row >= ROWS) {
                iterator.remove();
                continue;
            }
            if (walls[row][column] == BRICK) {
                walls[row][column] = EMPTY;
                booms.add(Boom.burst(shell.x, shell.y, 0xFFE5B984));
                score += 5;
                iterator.remove();
                continue;
            }
            if (walls[row][column] == STEEL) {
                booms.add(Boom.burst(shell.x, shell.y, 0xFFB9C6D8));
                iterator.remove();
                continue;
            }
            float tankX = boardLeft + playerX * cell + cell / 2.0F;
            float tankY = boardTop + playerY * cell + cell / 2.0F;
            if (shell.fromPlayer) {
                EnemyTank hit = null;
                for (EnemyTank enemy : enemies) {
                    float ex = boardLeft + enemy.x * cell + cell / 2.0F;
                    float ey = boardTop + enemy.y * cell + cell / 2.0F;
                    if (Math.abs(ex - shell.x) < cell * 0.45F && Math.abs(ey - shell.y) < cell * 0.45F) {
                        hit = enemy;
                        break;
                    }
                }
                if (hit != null) {
                    enemies.remove(hit);
                    score += 100;
                    booms.add(Boom.burst(shell.x, shell.y, ArcadeTheme.CORAL));
                    iterator.remove();
                }
            } else if (invulnerable <= 0
                    && Math.abs(tankX - shell.x) < cell * 0.42F
                    && Math.abs(tankY - shell.y) < cell * 0.42F) {
                iterator.remove();
                hurtPlayer();
                if (gameOver) return;
            }
        }
    }

    private void hurtPlayer() {
        lives--;
        invulnerable = 90;
        booms.add(Boom.burst(boardLeft + playerX * cell, boardTop + playerY * cell, ArcadeTheme.CORAL));
        if (lives <= 0) {
            gameOver = true;
            message = "Sua base caiu";
            return;
        }
        playerColumn = respawnColumn;
        playerRow = respawnRow;
        playerX = playerColumn;
        playerY = playerRow;
        direction = 0;
        shells.removeIf(shell -> !shell.fromPlayer);
        message = "Tanque recuperado  •  " + lives + " vida(s)";
    }

    private void updateBooms() {
        for (Iterator<Boom> iterator = booms.iterator(); iterator.hasNext(); ) {
            Boom boom = iterator.next();
            if (--boom.life <= 0) iterator.remove();
        }
    }

    private boolean leftHeld;
    private boolean rightHeld;
    private boolean upHeld;
    private boolean downHeld;
    private boolean spaceQueued;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restart();
            return true;
        }
        switch (keyCode) {
            case 263, 65 -> leftHeld = true;
            case 262, 68 -> rightHeld = true;
            case 265, 87 -> upHeld = true;
            case 264, 83 -> downHeld = true;
            case 32 -> {
                if (shootCooldown <= 0) {
                    fireShell(playerX, playerY, direction, true);
                    shootCooldown = 22;
                } else {
                    spaceQueued = true;
                }
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 263, 65 -> leftHeld = false;
            case 262, 68 -> rightHeld = false;
            case 265, 87 -> upHeld = false;
            case 264, 83 -> downHeld = false;
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.CORAL);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 30,
                cleared ? ArcadeTheme.SUCCESS : gameOver ? ArcadeTheme.DANGER : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "FASE " + (level + 1), boardLeft, 44, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer, "PONTOS " + score, boardLeft + COLS * cell, 44,
                ArcadeTheme.GOLD);
        for (int index = 0; index < lives; index++) {
            drawTankIcon(context, boardLeft + 22 + index * 13, boardTop - 12);
        }

        ArcadeTheme.rounded(context, boardLeft - 6, boardTop - 6, boardLeft + COLS * cell + 6,
                boardTop + ROWS * cell + 6, 8, 0x66000000);
        ArcadeTheme.rounded(context, boardLeft - 5, boardTop - 5, boardLeft + COLS * cell + 5,
                boardTop + ROWS * cell + 5, 7, 0xFF23324A);
        context.fillGradient(boardLeft, boardTop, boardLeft + COLS * cell, boardTop + ROWS * cell,
                0xFF101E2C, 0xFF0A1620);
        drawWalls(context);
        drawExitMarks(context);

        for (EnemyTank enemy : enemies) {
            drawTank(context, boardLeft + enemy.x * cell + cell / 2.0F,
                    boardTop + enemy.y * cell + cell / 2.0F, enemy.direction, ArcadeTheme.CORAL, true);
        }
        boolean blink = invulnerable <= 0 || (ticks / 3) % 2 == 0;
        if (blink) {
            drawTank(context, boardLeft + playerX * cell + cell / 2.0F,
                    boardTop + playerY * cell + cell / 2.0F, direction, ArcadeTheme.LIME, false);
        }
        for (Shell shell : shells) {
            ArcadeTheme.glow(context, (int) shell.x, (int) shell.y, 5,
                    shell.fromPlayer ? 0xAAFFE28A : 0xAAFF8877);
            ArcadeTheme.disc(context, (int) shell.x, (int) shell.y, 2,
                    shell.fromPlayer ? 0xFFFFF3C4 : 0xFFFFB3A7);
        }
        for (Boom boom : booms) {
            float progress = 1 - boom.life / (float) boom.maxLife;
            int radius = (int) (3 + progress * cell * 0.8F);
            ArcadeTheme.glow(context, (int) boom.x, (int) boom.y, radius, ArcadeTheme.alpha(boom.color, 90));
            ArcadeTheme.ring(context, (int) boom.x, (int) boom.y, radius, 2,
                    ArcadeTheme.alpha(boom.color, (int) (220 * (1 - progress))));
        }
        if (cleared) {
            ArcadeTheme.banner(context, textRenderer, "FASE " + (level + 1) + " LIMPA",
                    "Prepare-se para a próxima onda", width / 2, boardTop + ROWS * cell / 2,
                    Math.min(width - 20, 280), ArcadeTheme.SUCCESS);
        } else if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, "BASE DESTRUÍDA",
                    "Pontos " + score + "  •  R para recomeçar", width / 2,
                    boardTop + ROWS * cell / 2, Math.min(width - 20, 280), ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawWalls(DrawContext context) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int value = walls[row][column];
                if (value == EMPTY) continue;
                int x = boardLeft + column * cell;
                int y = boardTop + row * cell;
                if (value == BRICK) {
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cell - 1, y + cell - 1, 2, 0xFF8B4A35);
                    context.fill(x + 2, y + cell / 2, x + cell - 2, y + cell / 2 + 1, 0xFF5E2F22);
                    context.fill(x + cell / 2, y + 2, x + cell / 2 + 1, y + cell / 2, 0xFF5E2F22);
                    context.fill(x + 2, y + 2, x + cell - 2, y + 4, 0x33FFD9B8);
                } else {
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cell - 1, y + cell - 1, 2, 0xFF5C6B80);
                    ArcadeTheme.rounded(context, x + 2, y + 2, x + cell - 2, y + cell - 2, 2, 0xFF7C8CA3);
                    context.fill(x + 3, y + 3, x + cell - 3, y + 4, 0x66FFFFFF);
                    context.fill(x + cell / 2 - 2, y + cell / 2 - 2, x + cell / 2 + 2, y + cell / 2 + 2,
                            0xFF4A5769);
                }
            }
        }
    }

    private void drawExitMarks(DrawContext context) {
        for (int step = 0; step < 5; step++) {
            context.fill(boardLeft + COLS * cell / 2 - 24 + step * 5, boardTop + 2,
                    boardLeft + COLS * cell / 2 + 24 - step * 5, boardTop + 3,
                    ArcadeTheme.alpha(ArcadeTheme.LIME, 60));
        }
    }

    private void drawTankIcon(DrawContext context, int x, int y) {
        context.fill(x - 4, y - 2, x + 4, y + 3, ArcadeTheme.LIME);
        context.fill(x - 1, y - 6, x + 1, y - 1, ArcadeTheme.lighten(ArcadeTheme.LIME, 40));
    }

    private void drawTank(DrawContext context, float centerX, float centerY, int dir, int color, boolean enemy) {
        int size = cell;
        int body = Math.max(6, (int) (size * 0.62F));
        int half = body / 2;
        int x = (int) centerX;
        int y = (int) centerY;
        int dark = ArcadeTheme.darken(color, 70);
        if (dir == 0 || dir == 2) {
            context.fill(x - half - 3, y - half + 1, x - half, y + half, dark);
            context.fill(x + half, y - half + 1, x + half + 3, y + half, dark);
        } else {
            context.fill(x - half + 1, y - half - 3, x + half, y - half, dark);
            context.fill(x - half + 1, y + half, x + half, y + half + 3, dark);
        }
        ArcadeTheme.rounded(context, x - half, y - half, x + half, y + half, 3, dark);
        ArcadeTheme.rounded(context, x - half + 1, y - half + 1, x + half - 1, y + half - 1, 3, color);
        context.fillGradient(x - half + 2, y - half + 2, x + half - 2, y,
                ArcadeTheme.lighten(color, 40), color);
        int turret = Math.max(3, body / 3);
        if (dir == 0) {
            context.fill(x - 1, y - half - turret - 2, x + 1, y - 1, ArcadeTheme.lighten(color, 55));
        } else if (dir == 2) {
            context.fill(x - 1, y + 1, x + 1, y + half + turret + 2, ArcadeTheme.lighten(color, 55));
        } else if (dir == 1) {
            context.fill(x + 1, y - 1, x + half + turret + 2, y + 1, ArcadeTheme.lighten(color, 55));
        } else {
            context.fill(x - half - turret - 2, y - 1, x - 1, y + 1, ArcadeTheme.lighten(color, 55));
        }
        ArcadeTheme.disc(context, x, y, Math.max(2, turret), ArcadeTheme.lighten(color, 25));
        if (enemy) {
            ArcadeTheme.disc(context, x - 1, y - 1, 1, 0xFFFFE9A8);
        } else {
            ArcadeTheme.disc(context, x, y, 1, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class EnemyTank {
        private int column;
        private int row;
        private float x;
        private float y;
        private int direction;
        private int shootCooldown;
        private int thinkCooldown;
        private int stepCooldown;
        private int speed;
    }

    private static final class Shell {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private boolean fromPlayer;
    }

    private static final class Boom {
        private final float x;
        private final float y;
        private final int color;
        private final int maxLife;
        private int life;

        private Boom(float x, float y, int color, int life) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.maxLife = life;
            this.life = life;
        }

        private static Boom burst(float x, float y, int color) {
            return new Boom(x, y, color, 18);
        }

        private static Boom muzzle(float x, float y, int color) {
            return new Boom(x, y, color, 7);
        }
    }
}
