package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Sokoban: empurre todas as caixas até os alvos, sem prender nenhuma. */
public final class SokobanScreen extends ArcadeScreen {
    private static final String[][] LEVELS = {
            {
                    "#########",
                    "#       #",
                    "#  .$@  #",
                    "#       #",
                    "#########"
            },
            {
                    "#########",
                    "#   .   #",
                    "#   $   #",
                    "#  @    #",
                    "#       #",
                    "#########"
            },
            {
                    "##########",
                    "#        #",
                    "#  .$  @ #",
                    "#        #",
                    "##########"
            },
            {
                    "##########",
                    "#        #",
                    "#  ####  #",
                    "#  $  .  #",
                    "#  @     #",
                    "#        #",
                    "##########"
            },
            {
                    "###########",
                    "#         #",
                    "#  . $    #",
                    "#         #",
                    "#  . $ @  #",
                    "#         #",
                    "###########"
            },
            {
                    "###########",
                    "#         #",
                    "#  . . .  #",
                    "#  $ $ $  #",
                    "#    @    #",
                    "#         #",
                    "###########"
            },
            {
                    "###########",
                    "#         #",
                    "#  # # #  #",
                    "#  $ . $  #",
                    "#    .    #",
                    "#    @    #",
                    "#         #",
                    "###########"
            },
            {
                    "##########",
                    "#        #",
                    "#  .  .  #",
                    "#  $  $  #",
                    "#   ##   #",
                    "#   @    #",
                    "#        #",
                    "##########"
            },
            {
                    "############",
                    "#          #",
                    "#  .$ .$   #",
                    "#          #",
                    "#    @     #",
                    "#          #",
                    "############"
            },
            {
                    "###########",
                    "#    #    #",
                    "# $  #  . #",
                    "#    #    #",
                    "#  $   .  #",
                    "#    @    #",
                    "#         #",
                    "###########"
            }
    };
    private static final int[] PAR = {1, 2, 3, 5, 9, 11, 24, 10, 10, 31};

    private int levelIndex;
    private int rows;
    private int columns;
    private boolean[][] walls;
    private boolean[][] goals;
    private boolean[][] boxes;
    private int playerRow;
    private int playerColumn;
    private int moves;
    private int pushes;
    private final List<Snapshot> history = new ArrayList<>();
    private boolean cleared;
    private boolean finished;
    private int clearTicks;
    private int moveCooldown;
    private int heldHorizontal;
    private int heldVertical;
    private int cellSize;
    private int boardLeft;
    private int boardTop;
    private int ticks;

    public SokobanScreen() {
        super("Empurra-Caixas");
    }

    @Override
    protected void init() {
        int buttonY = height - 26;
        int buttonWidth = Math.min(104, Math.max(62, (width - 52) / 3));
        int startX = (width - buttonWidth * 3 - 12) / 2;
        addDrawableChild(ArcadeTheme.button(startX, buttonY, buttonWidth, 20, Text.literal("Desfazer"), this::undo));
        addDrawableChild(ArcadeTheme.button(startX + buttonWidth + 6, buttonY, buttonWidth, 20,
                Text.literal("Reiniciar"), this::restartLevel));
        addDrawableChild(ArcadeTheme.button(startX + (buttonWidth + 6) * 2, buttonY, buttonWidth, 20,
                Text.literal("Menu"), this::backToHub));
        loadLevel();
    }

    private void loadLevel() {
        String[] map = LEVELS[Math.min(levelIndex, LEVELS.length - 1)];
        rows = map.length;
        columns = map[0].length();
        walls = new boolean[rows][columns];
        goals = new boolean[rows][columns];
        boxes = new boolean[rows][columns];
        history.clear();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                char tile = map[row].charAt(column);
                walls[row][column] = tile == '#';
                goals[row][column] = tile == '.' || tile == '*' || tile == '+';
                boxes[row][column] = tile == '$' || tile == '*';
                if (tile == '@' || tile == '+') {
                    playerRow = row;
                    playerColumn = column;
                }
            }
        }
        moves = 0;
        pushes = 0;
        cleared = false;
        finished = false;
        clearTicks = 0;
        moveCooldown = 0;
        int boardTopMost = 66;
        int boardBottomMost = height - 34;
        cellSize = Math.max(9, Math.min(34, Math.min((width - 34) / columns,
                (boardBottomMost - boardTopMost) / rows)));
        boardLeft = (width - columns * cellSize) / 2;
        boardTop = boardTopMost + Math.max(0, (boardBottomMost - boardTopMost - rows * cellSize) / 2);
    }

    private void restartLevel() {
        loadLevel();
    }

    private void undo() {
        if (history.isEmpty() || cleared) return;
        Snapshot snapshot = history.remove(history.size() - 1);
        playerRow = snapshot.playerRow;
        playerColumn = snapshot.playerColumn;
        moves = snapshot.moves;
        pushes = snapshot.pushes;
        for (int row = 0; row < rows; row++) {
            System.arraycopy(snapshot.boxes[row], 0, boxes[row], 0, columns);
        }
    }

    private boolean isFree(int row, int column) {
        return row >= 0 && column >= 0 && row < rows && column < columns
                && !walls[row][column] && !boxes[row][column];
    }

    private void pushHistory() {
        Snapshot snapshot = new Snapshot();
        snapshot.playerRow = playerRow;
        snapshot.playerColumn = playerColumn;
        snapshot.moves = moves;
        snapshot.pushes = pushes;
        snapshot.boxes = new boolean[rows][columns];
        for (int row = 0; row < rows; row++) {
            System.arraycopy(boxes[row], 0, snapshot.boxes[row], 0, columns);
        }
        history.add(snapshot);
        if (history.size() > 250) history.remove(0);
    }

    private void move(int dRow, int dColumn) {
        if (cleared || finished) return;
        int targetRow = playerRow + dRow;
        int targetColumn = playerColumn + dColumn;
        if (walls[targetRow][targetColumn]) return;
        if (boxes[targetRow][targetColumn]) {
            int beyondRow = targetRow + dRow;
            int beyondColumn = targetColumn + dColumn;
            if (!isFree(beyondRow, beyondColumn)) return;
            pushHistory();
            boxes[targetRow][targetColumn] = false;
            boxes[beyondRow][beyondColumn] = true;
            pushes++;
        } else {
            pushHistory();
        }
        playerRow = targetRow;
        playerColumn = targetColumn;
        moves++;
        checkCleared();
    }

    private void checkCleared() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (boxes[row][column] && !goals[row][column]) return;
            }
        }
        cleared = true;
        clearTicks = 0;
    }

    @Override
    public void tick() {
        ticks++;
        if (cleared) {
            clearTicks++;
            if (clearTicks > 46) {
                levelIndex++;
                if (levelIndex >= LEVELS.length) {
                    levelIndex = LEVELS.length - 1;
                    finished = true;
                    cleared = false;
                } else {
                    loadLevel();
                }
            }
            return;
        }
        if (moveCooldown > 0) moveCooldown--;
        if (moveCooldown == 0 && (heldHorizontal != 0 || heldVertical != 0)) {
            move(heldVertical, heldHorizontal);
            moveCooldown = 5;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restartLevel();
            return true;
        }
        if (keyCode == 90) {
            undo();
            return true;
        }
        if (cleared && (keyCode == 32 || keyCode == 257 || keyCode == 335)) {
            clearTicks = 99;
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
        String status = finished ? "Todos os depósitos organizados!"
                : cleared ? "Nível resolvido em " + moves + " movimentos!"
                : "Setas/WASD movem  •  Z desfaz  •  R reinicia";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 33,
                finished || cleared ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "NÍVEL " + (levelIndex + 1) + "/" + LEVELS.length,
                boardLeft, 48, ArcadeTheme.CORAL);
        String right = "MOV " + moves + "  •  EMPUR " + pushes + "  •  PAR " + PAR[levelIndex];
        ArcadeTheme.pillRight(context, textRenderer, right, boardLeft + columns * cellSize, 48, ArcadeTheme.TEAL);

        int boardWidth = columns * cellSize;
        int boardHeight = rows * cellSize;
        ArcadeTheme.glow(context, width / 2, boardTop + boardHeight / 2, boardWidth / 2 + 20, 0x33FF8A65);
        ArcadeTheme.rounded(context, boardLeft - 6, boardTop - 6, boardLeft + boardWidth + 6,
                boardTop + boardHeight + 6, 10, 0xFF23374F);
        ArcadeTheme.rounded(context, boardLeft - 5, boardTop - 5, boardLeft + boardWidth + 5,
                boardTop + boardHeight + 5, 9, 0xFF0A1422);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int x = boardLeft + column * cellSize;
                int y = boardTop + row * cellSize;
                if (walls[row][column]) {
                    ArcadeTheme.rounded(context, x, y, x + cellSize, y + cellSize, Math.max(2, cellSize / 6),
                            0xFF33506F);
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cellSize - 1, y + 2, 1, 0xFF6C92B8);
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cellSize - 2, y + cellSize - 2,
                            Math.max(2, cellSize / 7), 0xFF2A425C);
                    if (cellSize >= 16) {
                        ArcadeTheme.disc(context, x + cellSize / 3, y + cellSize / 3, 1, 0xFF3E5C7A);
                        ArcadeTheme.disc(context, x + cellSize * 2 / 3, y + cellSize * 2 / 3, 1, 0xFF3E5C7A);
                    }
                } else {
                    context.fill(x, y, x + cellSize, y + cellSize,
                            (row + column) % 2 == 0 ? 0xFF0F1D2E : 0xFF0C1827);
                    if (goals[row][column]) {
                        int centerX = x + cellSize / 2;
                        int centerY = y + cellSize / 2;
                        int radius = Math.max(2, cellSize / 5);
                        ArcadeTheme.ring(context, centerX, centerY, radius + 1, 1, 0x99FFD66B);
                        ArcadeTheme.disc(context, centerX, centerY, Math.max(1, radius / 2), 0xBBFFD66B);
                    }
                }
            }
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (!boxes[row][column]) continue;
                drawBox(context, boardLeft + column * cellSize, boardTop + row * cellSize, goals[row][column]);
            }
        }
        drawPlayer(context);
        if (cleared) {
            ArcadeTheme.banner(context, textRenderer, "NÍVEL COMPLETO",
                    "Movimentos: " + moves + "  •  par " + PAR[levelIndex],
                    width / 2, boardTop + boardHeight / 2, Math.min(width - 20, boardWidth + 24),
                    ArcadeTheme.SUCCESS);
        } else if (finished) {
            ArcadeTheme.banner(context, textRenderer, "VOCÊ VENCEU!",
                    "Todos os " + LEVELS.length + " depósitos organizados",
                    width / 2, boardTop + boardHeight / 2, Math.min(width - 20, boardWidth + 24),
                    ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBox(DrawContext context, int x, int y, boolean onGoal) {
        int inset = Math.max(1, cellSize / 8);
        int color = onGoal ? ArcadeTheme.GOLD : 0xFFC58B4E;
        ArcadeTheme.rounded(context, x + inset + 1, y + inset + 2, x + cellSize - inset + 1,
                y + cellSize - inset + 2, Math.max(2, cellSize / 6), 0x55000000);
        ArcadeTheme.rounded(context, x + inset, y + inset, x + cellSize - inset, y + cellSize - inset,
                Math.max(2, cellSize / 6), ArcadeTheme.darken(color, 50));
        ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                y + cellSize - inset - 1, Math.max(1, cellSize / 7), color);
        context.fillGradient(x + inset + 2, y + inset + 1, x + cellSize - inset - 2, y + cellSize / 2,
                ArcadeTheme.lighten(color, 60), 0x00FFFFFF);
        int pad = Math.max(2, cellSize / 4);
        context.fill(x + pad, y + pad, x + cellSize - pad, y + pad + 1, ArcadeTheme.darken(color, 80));
        context.fill(x + pad, y + cellSize - pad - 1, x + cellSize - pad, y + cellSize - pad,
                ArcadeTheme.darken(color, 80));
        context.fill(x + pad, y + pad, x + pad + 1, y + cellSize - pad, ArcadeTheme.darken(color, 80));
        context.fill(x + cellSize - pad - 1, y + pad, x + cellSize - pad, y + cellSize - pad,
                ArcadeTheme.darken(color, 80));
        for (int step = 0; step < Math.max(2, cellSize / 3); step++) {
            context.fill(x + pad + 2 + step, y + pad + 2 + step, x + pad + 3 + step, y + pad + 3 + step,
                    ArcadeTheme.darken(color, 40));
            context.fill(x + cellSize - pad - 3 - step, y + pad + 2 + step,
                    x + cellSize - pad - 2 - step, y + pad + 3 + step, ArcadeTheme.darken(color, 40));
        }
        if (onGoal) {
            ArcadeTheme.glow(context, x + cellSize / 2, y + cellSize / 2, cellSize / 2, 0x44FFD66B);
        }
    }

    private void drawPlayer(DrawContext context) {
        int x = boardLeft + playerColumn * cellSize;
        int y = boardTop + playerRow * cellSize;
        int centerX = x + cellSize / 2;
        int centerY = y + cellSize / 2;
        int radius = Math.max(3, cellSize / 3);
        ArcadeTheme.glow(context, centerX, centerY, radius + 3, 0x446CC9F5);
        ArcadeTheme.disc(context, centerX, centerY + 1, radius, 0xFF1B4A73);
        ArcadeTheme.disc(context, centerX, centerY, radius, ArcadeTheme.SKY);
        ArcadeTheme.disc(context, centerX - 1, centerY - 1, Math.max(1, radius - 2), 0xFF9FE0FF);
        int eye = Math.max(1, radius / 4);
        ArcadeTheme.disc(context, centerX - radius / 3, centerY - radius / 4, eye, 0xFF0A1A2A);
        ArcadeTheme.disc(context, centerX + radius / 3, centerY - radius / 4, eye, 0xFF0A1A2A);
        if (cellSize >= 18) {
            context.fill(centerX - radius / 3, centerY + radius / 3, centerX + radius / 3,
                    centerY + radius / 3 + 1, 0xAA0A1A2A);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Snapshot {
        private int playerRow;
        private int playerColumn;
        private int moves;
        private int pushes;
        private boolean[][] boxes;
    }
}
