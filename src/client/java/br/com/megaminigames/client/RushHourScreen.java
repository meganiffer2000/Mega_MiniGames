package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Trânsito Travado: empurre os carros do estacionamento até liberar a saída
 * para o carro vermelho. Clique no carro e use as setas para deslizá-lo.
 */
public final class RushHourScreen extends ArcadeScreen {
    private static final int SIZE = 6;
    private static final int[][][] LEVELS = {
            {{2, 0, 2, 1}, {4, 1, 2, 0}, {0, 5, 2, 0}, {3, 0, 2, 0}, {5, 2, 3, 1}, {1, 4, 3, 0},
                    {1, 1, 2, 1}, {3, 1, 2, 1}, {0, 2, 2, 1}},
            {{2, 0, 2, 1}, {0, 1, 2, 0}, {2, 2, 2, 0}, {1, 3, 3, 0}, {5, 4, 2, 1}, {4, 1, 2, 1},
                    {1, 5, 3, 0}, {0, 2, 2, 1}},
            {{2, 0, 2, 1}, {5, 0, 2, 1}, {1, 5, 3, 0}, {5, 3, 3, 1}, {1, 0, 2, 1}, {2, 2, 3, 0},
                    {4, 3, 2, 1}, {0, 4, 2, 0}, {0, 0, 3, 1}},
            {{2, 0, 2, 1}, {2, 5, 2, 0}, {1, 2, 2, 0}, {4, 3, 3, 1}, {5, 3, 3, 1}, {0, 4, 3, 0},
                    {4, 1, 2, 0}, {3, 0, 3, 0}, {0, 1, 2, 0}},
            {{2, 0, 2, 1}, {0, 4, 2, 1}, {3, 0, 2, 0}, {2, 4, 2, 0}, {0, 0, 2, 0}, {5, 3, 3, 1},
                    {4, 1, 2, 1}, {2, 2, 2, 0}, {0, 2, 2, 1}, {5, 0, 3, 1}},
            {{2, 0, 2, 1}, {3, 2, 2, 0}, {0, 5, 2, 0}, {4, 4, 2, 1}, {0, 4, 3, 0}, {0, 1, 3, 1},
                    {5, 0, 2, 1}, {3, 3, 3, 1}, {1, 2, 2, 1}, {4, 3, 2, 0}},
            {{2, 0, 2, 1}, {1, 5, 2, 0}, {1, 4, 2, 0}, {0, 2, 3, 0}, {0, 4, 2, 1}, {3, 1, 3, 0},
                    {4, 2, 3, 1}, {3, 5, 2, 0}, {3, 2, 2, 1}, {0, 0, 2, 0}},
            {{2, 0, 2, 1}, {4, 1, 3, 1}, {0, 1, 2, 1}, {1, 3, 2, 1}, {4, 4, 2, 0}, {1, 5, 2, 0},
                    {1, 2, 3, 0}, {3, 5, 2, 0}, {4, 0, 2, 0}}
    };
    private static final int[] CAR_COLORS = {
            0xFFE05263, 0xFF4FB3D9, 0xFFE5B984, 0xFF8BCB6A, 0xFFB48EE8,
            0xFFEFD45A, 0xFF5ED3BB, 0xFFE08FC4, 0xFF9AAFC7, 0xFF7FA8E8
    };

    private int cols[][];
    private int rows[][];
    private int levelIndex;
    private int selected;
    private int moves;
    private int totalMoves;
    private int cell;
    private int boardLeft;
    private int boardTop;
    private int hovered = -1;
    private boolean cleared;
    private boolean campaignWon;
    private int clearTicks;
    private int dragRow;
    private int dragColumn;

    public RushHourScreen() {
        super("Trânsito Travado");
    }

    @Override
    protected void init() {
        cell = Math.max(28, Math.min(58, Math.min((width - 60) / SIZE, (height - 168) / SIZE)));
        boardLeft = (width - cell * SIZE) / 2;
        boardTop = 66;
        addFooterButtons(height - 26, this::restartLevel);
        levelIndex = 0;
        totalMoves = 0;
        campaignWon = false;
        loadLevel();
    }

    private void loadLevel() {
        int[][] layout = LEVELS[levelIndex];
        rows = new int[layout.length][];
        cols = new int[layout.length][];
        for (int index = 0; index < layout.length; index++) {
            rows[index] = new int[] {layout[index][0]};
            cols[index] = new int[] {layout[index][1]};
        }
        selected = 0;
        moves = 0;
        cleared = false;
        clearTicks = 0;
    }

    private void restartLevel() {
        campaignWon = false;
        totalMoves = 0;
        levelIndex = 0;
        loadLevel();
    }

    private boolean horizontal(int index) {
        return LEVELS[levelIndex][index][3] == 1;
    }

    private int length(int index) {
        return LEVELS[levelIndex][index][2];
    }

    private boolean blocked(int ignore, int row, int column) {
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) return true;
        for (int index = 0; index < rows.length; index++) {
            if (index == ignore) continue;
            int otherRow = rows[index][0];
            int otherColumn = cols[index][0];
            int otherLength = length(index);
            for (int step = 0; step < otherLength; step++) {
                int cellRow = horizontal(index) ? otherRow : otherRow + step;
                int cellColumn = horizontal(index) ? otherColumn + step : otherColumn;
                if (cellRow == row && cellColumn == column) return true;
            }
        }
        return false;
    }

    private boolean slide(int index, int deltaRow, int deltaColumn) {
        int newRow = rows[index][0] + deltaRow;
        int newColumn = cols[index][0] + deltaColumn;
        int steps = length(index);
        for (int step = 0; step < steps; step++) {
            int cellRow = horizontal(index) ? newRow : newRow + step;
            int cellColumn = horizontal(index) ? newColumn + step : newColumn;
            if (blocked(index, cellRow, cellColumn)) return false;
        }
        rows[index][0] = newRow;
        cols[index][0] = newColumn;
        return true;
    }

    private boolean tryMove(int index, int deltaRow, int deltaColumn) {
        if (cleared || campaignWon) return false;
        if (horizontal(index) && deltaRow != 0) return false;
        if (!horizontal(index) && deltaColumn != 0) return false;
        if (!slide(index, deltaRow, deltaColumn)) return false;
        moves++;
        totalMoves++;
        if (cols[0][0] + length(0) >= SIZE) {
            cleared = true;
            clearTicks = 0;
        }
        return true;
    }

    private int carAt(double mouseX, double mouseY) {
        int column = (int) ((mouseX - boardLeft) / cell);
        int row = (int) ((mouseY - boardTop) / cell);
        for (int index = 0; index < rows.length; index++) {
            int carRow = rows[index][0];
            int carColumn = cols[index][0];
            int steps = length(index);
            for (int step = 0; step < steps; step++) {
                int cellRow = horizontal(index) ? carRow : carRow + step;
                int cellColumn = horizontal(index) ? carColumn + step : carColumn;
                if (cellRow == row && cellColumn == column) return index;
            }
        }
        return -1;
    }

    @Override
    public void tick() {
        if (!cleared) return;
        clearTicks++;
        if (clearTicks < 45) return;
        if (levelIndex + 1 >= LEVELS.length) {
            campaignWon = true;
            cleared = false;
            return;
        }
        levelIndex++;
        loadLevel();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        int index = carAt(mouseX, mouseY);
        if (index < 0) return false;
        selected = index;
        dragRow = (int) ((mouseY - boardTop) / cell);
        dragColumn = (int) ((mouseX - boardLeft) / cell);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        int row = (int) ((mouseY - boardTop) / cell);
        int column = (int) ((mouseX - boardLeft) / cell);
        if (horizontal(selected)) {
            while (column > dragColumn) {
                if (!tryMove(selected, 0, 1)) {
                    dragColumn = column;
                    break;
                }
                dragColumn++;
            }
            while (column < dragColumn) {
                if (!tryMove(selected, 0, -1)) {
                    dragColumn = column;
                    break;
                }
                dragColumn--;
            }
        } else {
            while (row > dragRow) {
                if (!tryMove(selected, 1, 0)) {
                    dragRow = row;
                    break;
                }
                dragRow++;
            }
            while (row < dragRow) {
                if (!tryMove(selected, -1, 0)) {
                    dragRow = row;
                    break;
                }
                dragRow--;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            loadLevel();
            return true;
        }
        if (campaignWon) return super.keyPressed(keyCode, scanCode, modifiers);
        switch (keyCode) {
            case 263, 65 -> tryMove(selected, 0, -1);
            case 262, 68 -> tryMove(selected, 0, 1);
            case 265, 87 -> tryMove(selected, -1, 0);
            case 264, 83 -> tryMove(selected, 1, 0);
            case 258, 257, 9 -> {
                selected = (selected + 1) % rows.length;
                return true;
            }
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.CORAL);
        int carX = boardLeft + cell * 5;
        int carY = boardTop + cell * 2;
        ArcadeTheme.status(context, textRenderer, Text.literal(cleared || campaignWon
                ? "Caminho livre!" : "Clique em um carro e use as setas (ou arraste)"), width / 2, 30,
                cleared ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "FASE " + (levelIndex + 1) + "/" + LEVELS.length,
                boardLeft, 44, ArcadeTheme.CORAL);
        ArcadeTheme.pillRight(context, textRenderer,
                "JOGADAS " + moves + "  •  TOTAL " + totalMoves, boardLeft + cell * SIZE, 44, ArcadeTheme.GOLD);
        hovered = carAt(mouseX, mouseY);

        ArcadeTheme.rounded(context, boardLeft - 8, boardTop - 8, boardLeft + cell * SIZE + 8,
                boardTop + cell * SIZE + 8, 10, 0x66000000);
        ArcadeTheme.rounded(context, boardLeft - 6, boardTop - 6, boardLeft + cell * SIZE + 6,
                boardTop + cell * SIZE + 6, 9, 0xFF1B2536);
        context.fillGradient(boardLeft, boardTop, boardLeft + cell * SIZE, boardTop + cell * SIZE,
                0xFF233046, 0xFF161F2E);
        for (int step = 0; step <= SIZE; step++) {
            context.fill(boardLeft + step * cell, boardTop, boardLeft + step * cell + 1,
                    boardTop + cell * SIZE, 0x33FFFFFF);
            context.fill(boardLeft, boardTop + step * cell, boardLeft + cell * SIZE,
                    boardTop + step * cell + 1, 0x33FFFFFF);
        }
        for (int step = 1; step < SIZE; step++) {
            for (int line = 0; line < 3; line++) {
                int y = boardTop + step * cell - 6 + line * 5;
                context.fill(boardLeft + 4, y, boardLeft + 10, y + 1, 0x33FFE9B0);
                context.fill(boardLeft + cell * SIZE - 10, y, boardLeft + cell * SIZE - 4, y + 1, 0x33FFE9B0);
            }
        }
        drawExit(context);
        for (int index = 0; index < rows.length; index++) {
            drawCar(context, index);
        }

        if (cleared || campaignWon) {
            ArcadeTheme.banner(context, textRenderer,
                    campaignWon ? "ESTACIONAMENTO LIBERADO" : "CARRO NA SAÍDA!",
                    campaignWon ? "Todas as fases concluídas  •  R para recomeçar"
                            : "Fase " + (levelIndex + 1) + " resolvida em " + moves + " jogadas",
                    width / 2, boardTop + cell * SIZE / 2, Math.min(width - 20, 300),
                    campaignWon ? ArcadeTheme.GOLD : ArcadeTheme.SUCCESS);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawExit(DrawContext context) {
        int y1 = boardTop + cell * 2;
        int y2 = y1 + cell;
        int x = boardLeft + cell * SIZE;
        context.fill(x, y1, x + 6, y2, 0x88E05263);
        for (int step = 0; step < 3; step++) {
            int arrowX = x + 24 + step * 5;
            context.fill(arrowX, y1 + cell / 2 - 6 + step, arrowX + 4, y1 + cell / 2 + 6 - step,
                    ArcadeTheme.alpha(ArcadeTheme.LIME, 200 - step * 40));
        }
        context.drawTextWithShadow(textRenderer, Text.literal("SAÍDA"), x + 8, y1 - 12, ArcadeTheme.LIME);
    }

    private void drawCar(DrawContext context, int index) {
        int padding = Math.max(2, cell / 12);
        int x1 = boardLeft + cols[index][0] * cell + padding;
        int y1 = boardTop + rows[index][0] * cell + padding;
        int width = horizontal(index) ? length(index) * cell - padding * 2 : cell - padding * 2;
        int height = horizontal(index) ? cell - padding * 2 : length(index) * cell - padding * 2;
        int x2 = x1 + width;
        int y2 = y1 + height;
        boolean isRed = index == 0;
        boolean active = index == selected || index == hovered;
        int color = isRed ? 0xFFE05263 : CAR_COLORS[index % CAR_COLORS.length];
        if (active) {
            ArcadeTheme.glow(context, x1 + width / 2, y1 + height / 2, Math.max(width, height) / 2 + 4,
                    ArcadeTheme.alpha(isRed ? ArcadeTheme.GOLD : ArcadeTheme.SKY, 90));
        }
        ArcadeTheme.rounded(context, x1, y1 + 2, x2, y2 + 2, 5, 0x55000000);
        ArcadeTheme.rounded(context, x1, y1, x2, y2, 5, color);
        ArcadeTheme.rounded(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 4, ArcadeTheme.lighten(color, 24));
        if (horizontal(index)) {
            context.fillGradient(x1 + 4, y1 + 4, x2 - 4, y1 + height / 2, 0x44FFFFFF, 0x00FFFFFF);
            int windowX = isRed ? x2 - 12 : x1 + 6;
            context.fill(windowX, y1 + 4, windowX + 6, y2 - 4, 0xAA0F1A28);
            context.fill(x1 + 3, y2 - 4, x1 + 7, y2 - 2, 0xFFFFE9A8);
            context.fill(x2 - 7, y2 - 4, x2 - 3, y2 - 2, 0xFFFFE9A8);
        } else {
            context.fillGradient(x1 + 4, y1 + 4, x1 + width / 2, y2 - 4, 0x44FFFFFF, 0x00FFFFFF);
            int windowY = isRed ? y2 - 12 : y1 + 6;
            context.fill(x1 + 4, windowY, x2 - 4, windowY + 6, 0xAA0F1A28);
            context.fill(x1 + 3, y1 + 3, x1 + 5, y1 + 7, 0xFFFFE9A8);
            context.fill(x2 - 5, y2 - 7, x2 - 3, y2 - 3, 0xFFFFE9A8);
        }
        context.fill(x1 + 2, y1 + 2, x2 - 2, y1 + 3, 0x55FFFFFF);
        if (isRed) {
            ArcadeTheme.ring(context, x1 + width / 2, y1 + height / 2, Math.max(4, Math.min(width, height) / 4),
                    1, 0x77FFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
