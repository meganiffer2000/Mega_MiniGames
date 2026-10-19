package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Come-Come: coma todas as pastilhas do labirinto sem encostar nos fantasmas. */
public final class PacManScreen extends Screen {
    private static final String[] MAZE = {
            "###################",
            "#........#........#",
            "#.##.###.#.###.##.#",
            "#o##.###.#.###.##o#",
            "#.................#",
            "#.##.#.#####.#.##.#",
            "#....#.......#....#",
            "####.#.##.##.#.####",
            "#....#.......#....#",
            "#.##.#.#####.#.##.#",
            "#.................#",
            "#o##.###.#.###.##o#",
            "#.##.###.#.###.##.#",
            "#........#........#",
            "###################"
    };
    private static final int COLS = 19;
    private static final int ROWS = 15;
    private static final int START_COL = 9;
    private static final int START_ROW = 10;
    private static final int[][] GHOST_HOME = {{8, 6}, {10, 6}, {8, 8}, {10, 8}};
    private static final int[] GHOST_COLORS = {ArcadeTheme.CORAL, ArcadeTheme.MAGENTA, ArcadeTheme.SKY, ArcadeTheme.GOLD};
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private final Mover pac = new Mover();
    private final List<Mover> ghosts = new ArrayList<>();
    private boolean[][] pellets;
    private int pelletsLeft;
    private int score;
    private int lives;
    private int powerTicks;
    private int invulnerableTicks;
    private int ticks;
    private int ghostDelay;
    private boolean gameOver;
    private boolean won;
    private int level = 1;
    private int cellSize;
    private int boardLeft;
    private int boardTop;

    public PacManScreen() {
        super(Text.literal("Come-Come"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(7, Math.min(22, Math.min((width - 34) / COLS, (height - 128) / ROWS)));
        boardLeft = (width - COLS * cellSize) / 2;
        boardTop = Math.max(66, (height - ROWS * cellSize - 92) / 2 + 30);
        int buttonY = Math.min(height - 24, boardTop + ROWS * cellSize + 10);
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo jogo"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        pellets = new boolean[ROWS][COLS];
        pelletsLeft = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                char tile = MAZE[row].charAt(col);
                if (tile == '.' || tile == 'o') {
                    pellets[row][col] = true;
                    pelletsLeft++;
                }
            }
        }
        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;
        won = false;
        powerTicks = 0;
        invulnerableTicks = 40;
        resetActors();
    }

    private void resetActors() {
        pac.col = START_COL;
        pac.row = START_ROW;
        pac.fromCol = START_COL;
        pac.fromRow = START_ROW;
        pac.toCol = START_COL;
        pac.toRow = START_ROW;
        pac.dirX = 0;
        pac.dirY = 0;
        pac.wantX = 0;
        pac.wantY = -1;
        pac.progress = 1.0F;
        ghosts.clear();
        for (int index = 0; index < GHOST_HOME.length; index++) {
            Mover ghost = new Mover();
            ghost.col = GHOST_HOME[index][0];
            ghost.row = GHOST_HOME[index][1];
            ghost.fromCol = ghost.col;
            ghost.fromRow = ghost.row;
            ghost.toCol = ghost.col;
            ghost.toRow = ghost.row;
            ghost.wantX = DIRS[index][0];
            ghost.wantY = DIRS[index][1];
            ghost.dirX = ghost.wantX;
            ghost.dirY = ghost.wantY;
            ghost.progress = 1.0F;
            ghosts.add(ghost);
        }
        ghostDelay = 40;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticks++;
        if (powerTicks > 0) powerTicks--;
        if (invulnerableTicks > 0) invulnerableTicks--;
        if (ghostDelay > 0) ghostDelay--;
        float pacSpeed = 1.0F / Math.max(5, 8 - Math.min(3, level - 1));
        step(pac, pacSpeed, true);
        if (ghostDelay <= 0) {
            float ghostSpeed = powerTicks > 0 ? 1.0F / 15 : 1.0F / 10;
            for (int index = 0; index < ghosts.size(); index++) {
                Mover ghost = ghosts.get(index);
                stepGhost(ghost, ghostSpeed);
                if (sameSpot(pac, ghost)) {
                    if (powerTicks > 0) {
                        score += 200;
                        ghost.col = GHOST_HOME[index][0];
                        ghost.row = GHOST_HOME[index][1];
                        ghost.fromCol = ghost.toCol = ghost.col;
                        ghost.fromRow = ghost.toRow = ghost.row;
                        ghost.progress = 1.0F;
                        ghost.dirX = 0;
                        ghost.dirY = 0;
                    } else if (invulnerableTicks <= 0) {
                        loseLife();
                        return;
                    }
                }
            }
        }
    }

    private int desiredX;
    private int desiredY;

    private void loseLife() {
        lives--;
        powerTicks = 0;
        invulnerableTicks = 60;
        if (lives <= 0) {
            gameOver = true;
            won = false;
            return;
        }
        resetActors();
    }

    private void step(Mover mover, float speed, boolean player) {
        if (mover.progress >= 1.0F) {
            mover.progress = 1.0F;
            boolean turned = false;
            if (player) {
                if (walkable(mover.col + desiredX, mover.row + desiredY) && (desiredX != 0 || desiredY != 0)) {
                    mover.dirX = desiredX;
                    mover.dirY = desiredY;
                    turned = true;
                }
            } else {
                chooseGhostDirection(mover);
                turned = true;
            }
            if ((turned || walkable(mover.col + mover.dirX, mover.row + mover.dirY))
                    && walkable(mover.col + mover.dirX, mover.row + mover.dirY)) {
                mover.fromCol = mover.col;
                mover.fromRow = mover.row;
                mover.toCol = mover.col + mover.dirX;
                mover.toRow = mover.row + mover.dirY;
                mover.progress = 0.0F;
            }
        }
        mover.progress += speed;
        if (mover.progress >= 1.0F) {
            mover.progress = 1.0F;
            mover.col = mover.toCol;
            mover.row = mover.toRow;
            if (player) eat(mover.col, mover.row);
        }
    }

    private void stepGhost(Mover ghost, float speed) {
        if (ghost.progress >= 1.0F) {
            ghost.progress = 1.0F;
            chooseGhostDirection(ghost);
            if (walkable(ghost.col + ghost.dirX, ghost.row + ghost.dirY)) {
                ghost.fromCol = ghost.col;
                ghost.fromRow = ghost.row;
                ghost.toCol = ghost.col + ghost.dirX;
                ghost.toRow = ghost.row + ghost.dirY;
                ghost.progress = 0.0F;
            }
        }
        ghost.progress += speed;
        if (ghost.progress >= 1.0F) {
            ghost.progress = 1.0F;
            ghost.col = ghost.toCol;
            ghost.row = ghost.toRow;
        }
    }

    private void chooseGhostDirection(Mover ghost) {
        List<int[]> options = new ArrayList<>();
        int backX = -ghost.dirX;
        int backY = -ghost.dirY;
        for (int[] dir : DIRS) {
            if (dir[0] == backX && dir[1] == backY) continue;
            if (walkable(ghost.col + dir[0], ghost.row + dir[1])) options.add(dir);
        }
        if (options.isEmpty()) {
            ghost.dirX = backX;
            ghost.dirY = backY;
            return;
        }
        if (powerTicks > 0) {
            int[] pick = options.get(ThreadLocalRandom.current().nextInt(options.size()));
            ghost.dirX = pick[0];
            ghost.dirY = pick[1];
            return;
        }
        int[] best = options.get(0);
        int bestDistance = Integer.MAX_VALUE;
        for (int[] dir : options) {
            int targetCol = ghost.col + dir[0];
            int targetRow = ghost.row + dir[1];
            int distance = Math.abs(targetCol - pac.col) + Math.abs(targetRow - pac.row);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = dir;
            }
        }
        if (ThreadLocalRandom.current().nextInt(10) < 2) {
            best = options.get(ThreadLocalRandom.current().nextInt(options.size()));
        }
        ghost.dirX = best[0];
        ghost.dirY = best[1];
    }

    private boolean sameSpot(Mover pacMover, Mover ghost) {
        float gx = renderX(ghost, cellSize) + boardLeft;
        float gy = renderY(ghost, cellSize) + boardTop;
        float px = renderX(pacMover, cellSize) + boardLeft;
        float py = renderY(pacMover, cellSize) + boardTop;
        return Math.abs(gx - px) < cellSize * 0.7F && Math.abs(gy - py) < cellSize * 0.7F;
    }

    private void eat(int col, int row) {
        if (!pellets[row][col]) return;
        pellets[row][col] = false;
        pelletsLeft--;
        char tile = MAZE[row].charAt(col);
        if (tile == 'o') {
            score += 50;
            powerTicks = 140;
        } else {
            score += 10;
        }
        if (pelletsLeft <= 0) {
            if (level < 3) {
                level++;
                newRound();
            } else {
                gameOver = true;
                won = true;
            }
        }
    }

    private void newRound() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                char tile = MAZE[row].charAt(col);
                if (tile == '.' || tile == 'o') {
                    pellets[row][col] = true;
                    pelletsLeft++;
                }
            }
        }
        invulnerableTicks = 40;
        powerTicks = 0;
        resetActors();
    }

    private static boolean walkable(int col, int row) {
        if (col < 0 || row < 0 || col >= COLS || row >= ROWS) return false;
        return MAZE[row].charAt(col) != '#';
    }

    private static float renderX(Mover mover, int cell) {
        float from = (mover.fromCol + 0.5F) * cell;
        float to = (mover.toCol + 0.5F) * cell;
        return from + (to - from) * Math.min(1.0F, mover.progress);
    }

    private static float renderY(Mover mover, int cell) {
        float from = (mover.fromRow + 0.5F) * cell;
        float to = (mover.toRow + 0.5F) * cell;
        return from + (to - from) * Math.min(1.0F, mover.progress);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(gameOver ? (won ? "Labirinto limpo!" : "Os fantasmas pegaram você")
                        : "Setas ou WASD  •  R reinicia"),
                width / 2, 32, gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL) : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "PONTOS " + score, boardLeft, 48, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, "NÍVEL " + level + "  •  VIDAS " + lives,
                boardLeft + COLS * cellSize, 48, ArcadeTheme.TEAL);
        float power = Math.max(0.0F, powerTicks / 140.0F);
        if (powerTicks > 0) {
            ArcadeTheme.progress(context, boardLeft, 59, COLS * cellSize, 3, power, ArcadeTheme.SKY);
        }

        int boardWidth = COLS * cellSize;
        int boardHeight = ROWS * cellSize;
        ArcadeTheme.rounded(context, boardLeft - 6, boardTop - 6, boardLeft + boardWidth + 6,
                boardTop + boardHeight + 6, 12, 0x552050A0);
        ArcadeTheme.rounded(context, boardLeft - 5, boardTop - 5, boardLeft + boardWidth + 5,
                boardTop + boardHeight + 5, 11, 0xFF091426);
        context.fill(boardLeft, boardTop, boardLeft + boardWidth, boardTop + boardHeight, 0xFF060D18);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = boardLeft + col * cellSize;
                int y = boardTop + row * cellSize;
                if (MAZE[row].charAt(col) == '#') {
                    ArcadeTheme.rounded(context, x, y, x + cellSize, y + cellSize, Math.max(2, cellSize / 5),
                            0xFF1B3F7A);
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cellSize - 1, y + 2, 1, 0xFF3F76C9);
                } else if (pellets[row][col]) {
                    int centerX = x + cellSize / 2;
                    int centerY = y + cellSize / 2;
                    if (MAZE[row].charAt(col) == 'o') {
                        int pulse = 1 + (int) (Math.sin(ticks * 0.12) * 1.5);
                        ArcadeTheme.glow(context, centerX, centerY, Math.max(4, cellSize / 3 + pulse), 0x66FFE9A8);
                        ArcadeTheme.disc(context, centerX, centerY, Math.max(2, cellSize / 6 + pulse / 2), 0xFFFFF0C0);
                    } else {
                        ArcadeTheme.disc(context, centerX, centerY, Math.max(1, cellSize / 9), 0xFFF4E4C1);
                    }
                }
            }
        }

        for (int index = 0; index < ghosts.size(); index++) {
            drawGhost(context, ghosts.get(index), GHOST_COLORS[index]);
        }
        drawPac(context);
        if (invulnerableTicks > 0 && (invulnerableTicks / 4) % 2 == 0 && gameOver) {
            ArcadeTheme.banner(context, textRenderer, "FIM DE JOGO", "Pontos: " + score,
                    width / 2, boardTop + boardHeight / 2, Math.min(width - 20, boardWidth + 40),
                    ArcadeTheme.CORAL);
        } else if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, won ? "VOCÊ VENCEU!" : "FIM DE JOGO",
                    "Pontos: " + score, width / 2, boardTop + boardHeight / 2,
                    Math.min(width - 20, boardWidth + 40), won ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawPac(DrawContext context) {
        float centerX = boardLeft + renderX(pac, cellSize);
        float centerY = boardTop + renderY(pac, cellSize);
        int radius = Math.max(3, (int) (cellSize * 0.42F));
        float facing = pac.dirX == 0 && pac.dirY == 0 ? 0.0F : (float) Math.atan2(pac.dirY, pac.dirX);
        float mouth = (0.28F + 0.22F * Math.abs((float) Math.sin(ticks * 0.22F))) * (float) Math.PI;
        ArcadeTheme.glow(context, (int) centerX, (int) centerY, radius + 4, 0x66FFD84A);
        ArcadeTheme.disc(context, (int) centerX, (int) centerY, radius, 0xFFFFD84A);
        ArcadeTheme.disc(context, (int) centerX, (int) centerY, radius - 1, 0xFFFFE877);
        int[] xs = new int[5];
        int[] ys = new int[5];
        xs[0] = (int) centerX;
        ys[0] = (int) centerY;
        for (int index = 0; index < 4; index++) {
            float angle = facing - mouth / 2 + mouth * (index / 3.0F);
            xs[index + 1] = (int) (centerX + Math.cos(angle) * (radius + 2));
            ys[index + 1] = (int) (centerY + Math.sin(angle) * (radius + 2));
        }
        ArcadeTheme.polygon(context, xs, ys, 0xFF060D18);
        int eyeX = (int) (centerX + Math.cos(facing - 1.2F) * radius * 0.45F);
        int eyeY = (int) (centerY + Math.sin(facing - 1.2F) * radius * 0.45F);
        ArcadeTheme.disc(context, eyeX, eyeY, Math.max(1, radius / 5), 0xFF201A05);
    }

    private void drawGhost(DrawContext context, Mover ghost, int bodyColor) {
        int centerX = (int) (boardLeft + renderX(ghost, cellSize));
        int centerY = (int) (boardTop + renderY(ghost, cellSize));
        int radius = Math.max(3, (int) (cellSize * 0.40F));
        boolean scared = powerTicks > 0;
        int color = scared ? (powerTicks < 40 && (powerTicks / 5) % 2 == 0 ? 0xFFFFFFFF : 0xFF4A6BE8) : bodyColor;
        ArcadeTheme.glow(context, centerX, centerY, radius + 3, ArcadeTheme.alpha(color, 70));
        ArcadeTheme.disc(context, centerX, centerY - 1, radius, color);
        context.fill(centerX - radius, centerY - 1, centerX + radius, centerY + radius, color);
        int bob = (int) (Math.sin(ticks * 0.25 + centerX * 0.1) * 1.5);
        for (int index = -1; index <= 1; index++) {
            ArcadeTheme.disc(context, centerX + index * Math.max(2, radius / 2), centerY + radius - 1 + bob,
                    Math.max(1, radius / 3), color);
        }
        int eyeRadius = Math.max(1, radius / 3);
        int lookX = (int) Math.signum(ghost.dirX) * Math.max(1, radius / 3);
        int lookY = (int) Math.signum(ghost.dirY) * Math.max(1, radius / 3);
        if (!scared) {
            ArcadeTheme.disc(context, centerX - radius / 2, centerY - radius / 4, eyeRadius, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX + radius / 2, centerY - radius / 4, eyeRadius, 0xFFFFFFFF);
            ArcadeTheme.disc(context, centerX - radius / 2 + lookX, centerY - radius / 4 + lookY,
                    Math.max(1, eyeRadius / 2), 0xFF1B2740);
            ArcadeTheme.disc(context, centerX + radius / 2 + lookX, centerY - radius / 4 + lookY,
                    Math.max(1, eyeRadius / 2), 0xFF1B2740);
        } else {
            context.fill(centerX - radius / 2 - 1, centerY - radius / 4 - 1, centerX - radius / 2 + 2,
                    centerY - radius / 4 + 2, 0xFF1B2740);
            context.fill(centerX + radius / 2 - 1, centerY - radius / 4 - 1, centerX + radius / 2 + 2,
                    centerY - radius / 4 + 2, 0xFF1B2740);
        }
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            newGame();
            return true;
        }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 265 || keyCode == 87) {
            desiredX = 0;
            desiredY = -1;
        } else if (keyCode == 264 || keyCode == 83) {
            desiredX = 0;
            desiredY = 1;
        } else if (keyCode == 263 || keyCode == 65) {
            desiredX = -1;
            desiredY = 0;
        } else if (keyCode == 262 || keyCode == 68) {
            desiredX = 1;
            desiredY = 0;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (pac.progress >= 1.0F) {
            pac.wantX = desiredX;
            pac.wantY = desiredY;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Mover {
        private int col;
        private int row;
        private int fromCol;
        private int fromRow;
        private int toCol;
        private int toRow;
        private int dirX;
        private int dirY;
        private int wantX;
        private int wantY;
        private float progress = 1.0F;
    }
}
