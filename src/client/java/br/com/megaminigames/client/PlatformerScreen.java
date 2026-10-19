package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Mini Plataforma: corra, pule e colete moedas até a bandeira em três fases. */
public final class PlatformerScreen extends ArcadeScreen {
    private static final String[][] LEVELS = {
            {
                    "..............................................",
                    "..............................................",
                    "..............................................",
                    "................o.o.o.........................",
                    "...............=======........................",
                    "......o.......................................",
                    ".....===......................................",
                    ".....................o..............o.........",
                    "....................===............===........",
                    "..P.........E...........................F.....",
                    "#########...#########...######################",
                    "#########...#########...######################"
            },
            {
                    "..............................................",
                    "..............................................",
                    "..............................................",
                    "..............o.o.o...........................",
                    ".............=======..........................",
                    ".........o.....................o..............",
                    "........===...................===.............",
                    "....o.....................o.........o.........",
                    "...===...................===.......===........",
                    "..P.............E...........E............F....",
                    "########...##########...##########...#########",
                    "########...##########...##########...#########"
            },
            {
                    "..............................................",
                    "..............................................",
                    "..............................o.o.............",
                    ".............................=====............",
                    "..............o.o.............................",
                    ".............=====............................",
                    "....o...................o.....................",
                    "...===.................===....................",
                    "........o.........o..............o............",
                    "..P..........E......E.....E..............F....",
                    "#######...#######...#######...################",
                    "#######...#######...#######...################"
            }
    };

    private final List<Enemy> enemies = new ArrayList<>();
    private boolean[][] solid;
    private boolean[][] coins;
    private int levelIndex;
    private int columns;
    private int rows;
    private float playerX;
    private float playerY;
    private float playerWidth;
    private float playerHeight;
    private float velocityX;
    private float velocityY;
    private float startX;
    private float startY;
    private float flagX;
    private float flagY;
    private int tile;
    private int fieldTop;
    private int fieldBottom;
    private float cameraX;
    private int lives;
    private int coinsCollected;
    private int score;
    private int invulnerable;
    private int clearTicks;
    private int ticks;
    private boolean onGround;
    private boolean leftHeld;
    private boolean rightHeld;
    private boolean levelCleared;
    private boolean victory;
    private boolean defeat;
    private String message = "Setas/A-D movem  •  Espaço pula";

    public PlatformerScreen() {
        super("Mini Plataforma");
    }

    @Override
    protected void init() {
        fieldTop = 58;
        fieldBottom = height - 34;
        tile = Math.max(9, Math.min(18, (fieldBottom - fieldTop) / 12));
        addFooterButtons(height - 26, this::restartMatch);
        lives = 3;
        coinsCollected = 0;
        score = 0;
        levelIndex = 0;
        victory = false;
        defeat = false;
        loadLevel();
    }

    private void restartMatch() {
        lives = 3;
        coinsCollected = 0;
        score = 0;
        levelIndex = 0;
        victory = false;
        defeat = false;
        loadLevel();
    }

    private void loadLevel() {
        String[] map = LEVELS[levelIndex];
        rows = map.length;
        columns = map[0].length();
        solid = new boolean[rows][columns];
        coins = new boolean[rows][columns];
        enemies.clear();
        levelCleared = false;
        clearTicks = 0;
        invulnerable = 40;
        velocityX = 0;
        velocityY = 0;
        playerWidth = Math.max(6, tile * 0.62F);
        playerHeight = Math.max(9, tile * 0.94F);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                char value = map[row].charAt(column);
                solid[row][column] = value == '#' || value == '=';
                coins[row][column] = value == 'o';
                if (value == 'P') {
                    startX = column * tile + (tile - playerWidth) / 2.0F;
                    startY = row * tile + (tile - playerHeight);
                } else if (value == 'F') {
                    flagX = column * tile + tile / 2.0F;
                    flagY = row * tile + tile;
                } else if (value == 'E') {
                    Enemy enemy = new Enemy();
                    enemy.width = tile * 0.78F;
                    enemy.height = tile * 0.62F;
                    enemy.x = column * tile + (tile - enemy.width) / 2.0F;
                    enemy.y = row * tile + (tile - enemy.height);
                    enemy.vx = 0.55F;
                    enemies.add(enemy);
                }
            }
        }
        playerX = startX;
        playerY = startY;
        cameraX = 0;
        message = "Fase " + (levelIndex + 1) + " de " + LEVELS.length;
    }

    private boolean isSolid(int column, int row) {
        if (column < 0 || column >= columns) return true;
        if (row < 0 || row >= rows) return false;
        return solid[row][column];
    }

    private void moveX(float amount) {
        playerX += amount;
        int topRow = (int) (playerY / tile);
        int bottomRow = (int) ((playerY + playerHeight - 0.01F) / tile);
        if (amount > 0) {
            int column = (int) ((playerX + playerWidth) / tile);
            for (int row = topRow; row <= bottomRow; row++) {
                if (isSolid(column, row)) {
                    playerX = column * tile - playerWidth;
                    velocityX = 0;
                    return;
                }
            }
        } else if (amount < 0) {
            int column = (int) (playerX / tile);
            for (int row = topRow; row <= bottomRow; row++) {
                if (isSolid(column, row)) {
                    playerX = (column + 1) * tile;
                    velocityX = 0;
                    return;
                }
            }
        }
    }

    private void moveY(float amount) {
        playerY += amount;
        int leftColumn = (int) (playerX / tile);
        int rightColumn = (int) ((playerX + playerWidth - 0.01F) / tile);
        if (amount > 0) {
            int row = (int) ((playerY + playerHeight) / tile);
            for (int column = leftColumn; column <= rightColumn; column++) {
                if (isSolid(column, row)) {
                    playerY = row * tile - playerHeight;
                    velocityY = 0;
                    onGround = true;
                    return;
                }
            }
        } else if (amount < 0) {
            int row = (int) (playerY / tile);
            for (int column = leftColumn; column <= rightColumn; column++) {
                if (isSolid(column, row)) {
                    playerY = (row + 1) * tile;
                    velocityY = 0;
                    return;
                }
            }
        }
    }

    @Override
    public void tick() {
        ticks++;
        if (defeat || victory) return;
        if (levelCleared) {
            clearTicks++;
            if (clearTicks > 55) {
                if (levelIndex + 1 >= LEVELS.length) {
                    victory = true;
                } else {
                    levelIndex++;
                    loadLevel();
                }
            }
            return;
        }
        if (invulnerable > 0) invulnerable--;
        float speed = tile * 0.155F;
        float target = (leftHeld ? -speed : 0) + (rightHeld ? speed : 0);
        velocityX += (target - velocityX) * 0.35F;
        velocityY += tile * 0.028F;
        if (velocityY > tile * 0.55F) velocityY = tile * 0.55F;
        onGround = false;
        moveX(velocityX);
        moveY(velocityY);
        if (playerX < 0) {
            playerX = 0;
            velocityX = 0;
        }
        float worldWidth = columns * tile;
        if (playerX + playerWidth > worldWidth) {
            playerX = worldWidth - playerWidth;
            velocityX = 0;
        }
        collectCoins();
        updateEnemies();
        if (invulnerable <= 0) {
            for (Enemy enemy : enemies) {
                if (enemy.dead) continue;
                if (!overlaps(enemy.x, enemy.y, enemy.width, enemy.height)) continue;
                if (velocityY > 0 && playerY + playerHeight - enemy.y < Math.max(8, tile * 0.55F)) {
                    enemy.dead = true;
                    velocityY = -tile * 0.28F;
                    score += 25;
                } else {
                    hurt();
                    break;
                }
            }
        }
        if (Math.abs(playerX + playerWidth / 2 - flagX) < tile * 0.6F
                && Math.abs(playerY + playerHeight - flagY) < tile * 1.2F) {
            levelCleared = true;
            clearTicks = 0;
            score += 50;
            message = "Bandeira alcançada!";
        }
        if (playerY > rows * tile + 60) {
            hurt();
        }
        float viewCenter = playerX + playerWidth / 2 - width / 2.0F;
        if (worldWidth <= width) {
            cameraX = (worldWidth - width) / 2.0F;
        } else {
            cameraX = Math.max(0, Math.min(worldWidth - width, viewCenter));
        }
    }

    private void collectCoins() {
        int leftColumn = (int) (playerX / tile);
        int rightColumn = (int) ((playerX + playerWidth) / tile);
        int topRow = (int) (playerY / tile);
        int bottomRow = (int) ((playerY + playerHeight) / tile);
        for (int row = Math.max(0, topRow); row <= Math.min(rows - 1, bottomRow); row++) {
            for (int column = Math.max(0, leftColumn); column <= Math.min(columns - 1, rightColumn); column++) {
                if (!coins[row][column]) continue;
                coins[row][column] = false;
                coinsCollected++;
                score += 10;
            }
        }
    }

    private void updateEnemies() {
        for (Iterator<Enemy> iterator = enemies.iterator(); iterator.hasNext(); ) {
            Enemy enemy = iterator.next();
            if (enemy.dead) {
                iterator.remove();
                continue;
            }
            enemy.x += enemy.vx;
            float aheadX = enemy.vx > 0 ? enemy.x + enemy.width + 1 : enemy.x - 1;
            int column = (int) (aheadX / tile);
            int footRow = (int) ((enemy.y + enemy.height - 2) / tile);
            int groundRow = (int) ((enemy.y + enemy.height + 2) / tile);
            if (isSolid(column, footRow) || !isSolid(column, groundRow)) {
                enemy.vx = -enemy.vx;
                enemy.x += enemy.vx * 2;
            }
            if (enemy.x < 0 || enemy.x + enemy.width > columns * tile) {
                enemy.vx = -enemy.vx;
            }
        }
    }

    private boolean overlaps(float x, float y, float width, float height) {
        return playerX < x + width && playerX + playerWidth > x
                && playerY < y + height && playerY + playerHeight > y;
    }

    private void hurt() {
        lives--;
        invulnerable = 70;
        if (lives <= 0) {
            defeat = true;
            return;
        }
        playerX = startX;
        playerY = startY;
        velocityX = 0;
        velocityY = 0;
        message = "Cuidado! Restam " + lives + " vida(s)";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restartMatch();
            return true;
        }
        if (defeat || victory || levelCleared) return super.keyPressed(keyCode, scanCode, modifiers);
        switch (keyCode) {
            case 263, 65 -> leftHeld = true;
            case 262, 68 -> rightHeld = true;
            case 265, 87, 32 -> {
                if (onGround) {
                    velocityY = -tile * 0.36F;
                    onGround = false;
                }
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
            default -> {
                return super.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.LIME);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 32,
                levelCleared ? ArcadeTheme.SUCCESS : defeat ? ArcadeTheme.DANGER : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "FASE " + (levelIndex + 1) + "/" + LEVELS.length, 12, 47,
                ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer, "MOEDAS " + coinsCollected, width - 12, 47, ArcadeTheme.GOLD);
        for (int index = 0; index < lives; index++) {
            ArcadeTheme.heart(context, width / 2 - 8 + index * 12, 53, 4, ArcadeTheme.CORAL);
        }

        context.fillGradient(0, fieldTop, width, fieldBottom, 0xFF163B63, 0xFF3D7FB5);
        drawBackdrop(context);
        int offset = (int) cameraX;
        int firstColumn = Math.max(0, offset / tile - 1);
        int lastColumn = Math.min(columns - 1, (offset + width) / tile + 1);
        for (int row = 0; row < rows; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (solid[row][column]) {
                    drawTile(context, column, row, offset, row < rows && mapChar(column, row) == '=');
                }
            }
        }
        for (int row = 0; row < rows; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (coins[row][column]) drawCoin(context, column, row, offset);
            }
        }
        drawFlag(context, offset);
        for (Enemy enemy : enemies) {
            drawEnemy(context, enemy, offset);
        }
        drawPlayer(context, offset);
        ArcadeTheme.rounded(context, 6, fieldTop + 4, 8, fieldBottom - 4, 1, 0x22000000);

        if (levelCleared) {
            ArcadeTheme.banner(context, textRenderer, "FASE COMPLETA",
                    "Moedas: " + coinsCollected + "  •  pontos " + score,
                    width / 2, (fieldTop + fieldBottom) / 2, Math.min(width - 20, 280),
                    ArcadeTheme.SUCCESS);
        } else if (victory) {
            ArcadeTheme.banner(context, textRenderer, "VOCÊ VENCEU!",
                    "As três fases foram concluídas  •  " + score + " pontos",
                    width / 2, (fieldTop + fieldBottom) / 2, Math.min(width - 20, 300), ArcadeTheme.GOLD);
        } else if (defeat) {
            ArcadeTheme.banner(context, textRenderer, "FIM DE JOGO",
                    "Pontos: " + score + "  •  moedas: " + coinsCollected,
                    width / 2, (fieldTop + fieldBottom) / 2, Math.min(width - 20, 280), ArcadeTheme.DANGER);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private char mapChar(int column, int row) {
        return LEVELS[levelIndex][row].charAt(column);
    }

    private void drawBackdrop(DrawContext context) {
        int sunX = width - 46;
        int sunY = fieldTop + 22;
        ArcadeTheme.glow(context, sunX, sunY, 22, 0x44FFE9A8);
        ArcadeTheme.disc(context, sunX, sunY, 10, 0xFFFFF3C4);
        for (int index = 0; index < 6; index++) {
            int hash = index * 981723 + 77;
            int cloudX = Math.floorMod(hash - (int) (cameraX * 0.35F), width + 100) - 50;
            int cloudY = fieldTop + 18 + Math.floorMod(hash >> 4, Math.max(1, fieldBottom - fieldTop - 60));
            int size = 8 + index % 3 * 3;
            int color = 0x22FFFFFF;
            ArcadeTheme.disc(context, cloudX, cloudY, size, color);
            ArcadeTheme.disc(context, cloudX + size, cloudY + 2, size - 3, color);
            ArcadeTheme.disc(context, cloudX - size, cloudY + 3, size - 4, color);
        }
        for (int index = 0; index < 4; index++) {
            int hash = index * 459821 + 13;
            int hillX = Math.floorMod(hash - (int) (cameraX * 0.18F), width + 200) - 100;
            int hillY = fieldBottom;
            int hillWidth = 70 + index % 2 * 40;
            ArcadeTheme.disc(context, hillX, hillY, hillWidth, 0x2212382A);
            ArcadeTheme.disc(context, hillX + 30, hillY, hillWidth - 14, 0x2212382A);
        }
    }

    private void drawTile(DrawContext context, int column, int row, int offset, boolean platform) {
        int x = column * tile - offset;
        int y = fieldTop + row * tile;
        if (platform) {
            ArcadeTheme.rounded(context, x, y, x + tile, y + tile, 3, 0xFF3A4B62);
            ArcadeTheme.rounded(context, x, y, x + tile, y + Math.max(2, tile / 3), 3, ArcadeTheme.TEAL);
            context.fill(x + 1, y + 1, x + tile - 1, y + 2, 0x66FFFFFF);
            return;
        }
        ArcadeTheme.rounded(context, x, y, x + tile, y + tile, 2, 0xFF5A3F23);
        context.fill(x + 1, y + 1, x + tile - 1, y + tile / 2, 0xFF6E4E2C);
        boolean openAbove = !isSolid(column, row - 1);
        if (openAbove) {
            context.fill(x, y, x + tile, y + Math.max(2, tile / 4), 0xFF4FA34A);
            context.fill(x, y, x + tile, y + 1, 0xFF78CC6A);
        }
        if (tile >= 12) {
            context.fill(x + tile / 4, y + tile / 2 + 2, x + tile / 4 + 2, y + tile / 2 + 4, 0xFF4A331C);
            context.fill(x + tile * 2 / 3, y + tile * 3 / 4, x + tile * 2 / 3 + 2,
                    y + tile * 3 / 4 + 2, 0xFF4A331C);
        }
    }

    private void drawCoin(DrawContext context, int column, int row, int offset) {
        int centerX = column * tile - offset + tile / 2;
        int centerY = fieldTop + row * tile + tile / 2;
        float phase = (float) Math.sin(ticks * 0.12F + column * 0.7F);
        int radius = Math.max(2, tile / 3);
        int width = Math.max(1, (int) (radius * Math.abs(phase)) + 1);
        ArcadeTheme.glow(context, centerX, centerY, radius + 2, 0x44FFD66B);
        context.fill(centerX - width, centerY - radius, centerX + width + 1, centerY + radius + 1, 0xFFD8A72E);
        context.fill(centerX - width + 1, centerY - radius + 1, centerX + width, centerY + radius,
                ArcadeTheme.GOLD);
        if (tile >= 13) {
            context.fill(centerX, centerY - radius / 2, centerX + 1, centerY + radius / 2, 0xFFB8862B);
        }
    }

    private void drawFlag(DrawContext context, int offset) {
        int x = (int) (flagX - offset);
        int baseY = fieldTop + (int) flagY;
        int heightPixels = tile * 3;
        context.fill(x, baseY - heightPixels, x + 2, baseY, 0xFFD8DEE8);
        ArcadeTheme.disc(context, x + 1, baseY - heightPixels, 2, ArcadeTheme.GOLD);
        for (int row = 0; row < heightPixels / 2; row++) {
            int wave = (int) (Math.sin(ticks * 0.14F + row * 0.35F) * 2);
            int widthPixels = heightPixels / 2 - row / 2;
            context.fill(x + 2, baseY - heightPixels + row, x + 2 + widthPixels + wave,
                    baseY - heightPixels + row + 1, ArcadeTheme.CORAL);
        }
        ArcadeTheme.star(context, x + tile / 2, baseY - heightPixels + heightPixels / 4, 2, ArcadeTheme.GOLD);
    }

    private void drawEnemy(DrawContext context, Enemy enemy, int offset) {
        int x = (int) (enemy.x - offset);
        int y = fieldTop + (int) enemy.y;
        int width = (int) enemy.width;
        int height = (int) enemy.height;
        int squeeze = (int) (Math.sin(ticks * 0.2F + x) * 1.5);
        ArcadeTheme.rounded(context, x, y + squeeze, x + width, y + height, 5, 0xFF7A2DA8);
        ArcadeTheme.rounded(context, x + 1, y + 1 + squeeze, x + width - 1, y + height - 1, 4, 0xFFB85CF0);
        ArcadeTheme.disc(context, x + width / 3, y + height / 2 + squeeze, Math.max(1, width / 8), 0xFFFFFFFF);
        ArcadeTheme.disc(context, x + width * 2 / 3, y + height / 2 + squeeze, Math.max(1, width / 8),
                0xFFFFFFFF);
        ArcadeTheme.disc(context, x + width / 3, y + height / 2 + squeeze, 1, 0xFF2A0B3A);
        ArcadeTheme.disc(context, x + width * 2 / 3, y + height / 2 + squeeze, 1, 0xFF2A0B3A);
    }

    private void drawPlayer(DrawContext context, int offset) {
        if (invulnerable > 0 && (invulnerable / 4) % 2 == 0 && !levelCleared) return;
        int x = (int) (playerX - offset);
        int y = fieldTop + (int) playerY;
        int width = (int) playerWidth;
        int height = (int) playerHeight;
        int step = onGround ? (int) (Math.sin(ticks * 0.4F) * 1.5) : 0;
        ArcadeTheme.glow(context, x + width / 2, y + height / 2, Math.max(width, height), 0x336CC9F5);
        ArcadeTheme.rounded(context, x, y + height / 3, x + width, y + height, 3, 0xFF2F5F9E);
        ArcadeTheme.rounded(context, x + 1, y + height / 3, x + width - 1, y + height - 2, 2, 0xFF4B84C8);
        context.fill(x + 1, y + height - 2, x + width / 3, y + height + step, 0xFF203A5C);
        context.fill(x + width / 3 + 1, y + height - 2, x + width - 1, y + height - step, 0xFF203A5C);
        context.fill(x + 1, y, x + width - 1, y + height / 3, 0xFFE8C9A0);
        context.fill(x + width / 4, y - 2, x + width * 3 / 4, y + 2, 0xFFD94A4A);
        context.fill(x + width * 3 / 4, y, x + width - 1, y + 1, 0xFFD94A4A);
        int eyeX = x + (velocityX >= 0 ? width * 2 / 3 : width / 3);
        context.fill(eyeX, y + height / 6, eyeX + 2, y + height / 6 + 2, 0xFF22334A);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Enemy {
        private float x;
        private float y;
        private float width;
        private float height;
        private float vx;
        private boolean dead;
    }
}
