package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Campo minado local: clique esquerdo revela e clique direito coloca/remove uma bandeira. */
public final class MinesweeperScreen extends Screen {
    private static final int COLUMNS = 10;
    private static final int ROWS = 10;
    private static final int MINE_COUNT = 15;
    private static final int MAX_CELL_SIZE = 26;
    private int cellSize;
    private int gridTop = 78;
    private int timeTicks;

    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int[][] neighborCounts;
    private boolean gameOver;
    private boolean won;
    private boolean firstReveal = true;
    private int gridLeft;

    public MinesweeperScreen() {
        super(Text.literal("Campo Minado"));
    }

    @Override
    protected void init() {
        int availableHeight = Math.max(ROWS * 10, height - 158);
        int heightLimitedCell = availableHeight / ROWS;
        int widthLimitedCell = Math.max(10, (width - 30) / COLUMNS);
        cellSize = Math.max(10, Math.min(MAX_CELL_SIZE, Math.min(heightLimitedCell, widthLimitedCell)));
        gridLeft = (width - COLUMNS * cellSize) / 2;
        gridTop = Math.max(80, (height - ROWS * cellSize - 96) / 2 + 14);
        int buttonY = height - 26;
        int buttonWidth = Math.min(112, Math.max(76, (width - 44) / 2));
        int startX = (width - buttonWidth * 2 - 8) / 2;

        addDrawableChild(ArcadeTheme.button(startX, buttonY, buttonWidth, 20, Text.literal("Novo jogo"), () -> newGame()));
        addDrawableChild(ArcadeTheme.button(startX + buttonWidth + 8, buttonY, buttonWidth, 20, Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));

        if (mines == null) {
            newGame();
        }
    }

    private void newGame() {
        mines = new boolean[ROWS][COLUMNS];
        revealed = new boolean[ROWS][COLUMNS];
        flagged = new boolean[ROWS][COLUMNS];
        neighborCounts = new int[ROWS][COLUMNS];
        gameOver = false;
        won = false;
        firstReveal = true;
        timeTicks = 0;

        int placed = 0;
        while (placed < MINE_COUNT) {
            int row = ThreadLocalRandom.current().nextInt(ROWS);
            int column = ThreadLocalRandom.current().nextInt(COLUMNS);
            if (!mines[row][column]) {
                mines[row][column] = true;
                placed++;
            }
        }
        calculateNeighborCounts();
    }

    private void calculateNeighborCounts() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int count = 0;
                for (int neighborRow = row - 1; neighborRow <= row + 1; neighborRow++) {
                    for (int neighborColumn = column - 1; neighborColumn <= column + 1; neighborColumn++) {
                        if (isInside(neighborRow, neighborColumn) && mines[neighborRow][neighborColumn]) {
                            count++;
                        }
                    }
                }
                neighborCounts[row][column] = count;
            }
        }
    }

    private void reveal(int row, int column) {
        if (gameOver || revealed[row][column] || flagged[row][column]) {
            return;
        }

        if (firstReveal) {
            firstReveal = false;
            if (mines[row][column]) {
                moveMine(row, column);
                calculateNeighborCounts();
            }
        }

        if (mines[row][column]) {
            gameOver = true;
            return;
        }

        revealed[row][column] = true;
        if (neighborCounts[row][column] == 0) {
            for (int neighborRow = row - 1; neighborRow <= row + 1; neighborRow++) {
                for (int neighborColumn = column - 1; neighborColumn <= column + 1; neighborColumn++) {
                    if (isInside(neighborRow, neighborColumn)
                            && !revealed[neighborRow][neighborColumn]
                            && !flagged[neighborRow][neighborColumn]) {
                        reveal(neighborRow, neighborColumn);
                    }
                }
            }
        }
        checkForWin();
    }

    private void moveMine(int row, int column) {
        mines[row][column] = false;
        while (true) {
            int newRow = ThreadLocalRandom.current().nextInt(ROWS);
            int newColumn = ThreadLocalRandom.current().nextInt(COLUMNS);
            if (!mines[newRow][newColumn] && (newRow != row || newColumn != column)) {
                mines[newRow][newColumn] = true;
                return;
            }
        }
    }

    private void toggleFlag(int row, int column) {
        if (!gameOver && !revealed[row][column]) {
            flagged[row][column] = !flagged[row][column];
        }
    }

    private void checkForWin() {
        int safeCellsRevealed = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (revealed[row][column]) {
                    safeCellsRevealed++;
                }
            }
        }
        if (safeCellsRevealed == ROWS * COLUMNS - MINE_COUNT) {
            won = true;
            gameOver = true;
        }
    }

    private boolean isInside(int row, int column) {
        return row >= 0 && row < ROWS && column >= 0 && column < COLUMNS;
    }

    @Override
    public void tick() {
        if (!gameOver && !firstReveal) {
            timeTicks++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.TEAL);

        int boardRight = gridLeft + COLUMNS * cellSize;
        int boardBottom = gridTop + ROWS * cellSize;
        String minesLeft = "MINAS " + Math.max(0, MINE_COUNT - countFlags());
        ArcadeTheme.pill(context, textRenderer, minesLeft, gridLeft, 31, ArcadeTheme.MAGENTA);
        String timer = "TEMPO " + (timeTicks / 20);
        ArcadeTheme.pillRight(context, textRenderer, timer, boardRight, 31, ArcadeTheme.GOLD);
        String status;
        int statusColor;
        if (won) {
            status = "Você venceu! Todas as casas seguras foram reveladas.";
            statusColor = ArcadeTheme.SUCCESS;
        } else if (gameOver) {
            status = "Você encontrou uma mina! Tente novamente.";
            statusColor = ArcadeTheme.DANGER;
        } else {
            status = "Esquerdo revela  •  Direito marca  •  Meio abre vizinhos";
            statusColor = ArcadeTheme.MUTED;
        }
        ArcadeTheme.status(context, textRenderer, Text.literal(textRenderer.trimToWidth(status, width - 20)),
                width / 2, 52, statusColor);

        int pad = 6;
        ArcadeTheme.rounded(context, gridLeft - pad + 2, gridTop - pad + 4, boardRight + pad + 2,
                boardBottom + pad + 4, 12, 0x55000000);
        ArcadeTheme.rounded(context, gridLeft - pad, gridTop - pad, boardRight + pad, boardBottom + pad,
                12, 0x5535C8B9);
        ArcadeTheme.rounded(context, gridLeft - pad + 1, gridTop - pad + 1, boardRight + pad - 1,
                boardBottom + pad - 1, 11, 0xF00A1422);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                drawCell(context, row, column, mouseX, mouseY);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    /** Abre os vizinhos quando o número de bandeiras ao redor já bate com a pista. */
    private void chord(int row, int column) {
        if (gameOver || !revealed[row][column] || mines[row][column]) return;
        int flags = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int neighborRow = row + dy;
                int neighborColumn = column + dx;
                if (isInside(neighborRow, neighborColumn) && flagged[neighborRow][neighborColumn]) {
                    flags++;
                }
            }
        }
        if (flags != neighborCounts[row][column]) return;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int neighborRow = row + dy;
                int neighborColumn = column + dx;
                if (isInside(neighborRow, neighborColumn) && !flagged[neighborRow][neighborColumn]) {
                    reveal(neighborRow, neighborColumn);
                    if (gameOver) return;
                }
            }
        }
    }

    private int countFlags() {
        int count = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (flagged[row][column]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void drawCell(DrawContext context, int row, int column, int mouseX, int mouseY) {
        int x = gridLeft + column * cellSize;
        int y = gridTop + row * cellSize;
        int inset = 1;
        boolean hovered = mouseX >= x && mouseX < x + cellSize && mouseY >= y && mouseY < y + cellSize;
        boolean isMine = mines[row][column];
        boolean open = revealed[row][column] || (gameOver && isMine);

        if (open) {
            ArcadeTheme.rounded(context, x + inset, y + inset, x + cellSize - inset, y + cellSize - inset, 4,
                    isMine ? 0xFF6E2230 : 0xFF16283C);
            if (isMine) {
                int cx = x + cellSize / 2;
                int cy = y + cellSize / 2;
                int radius = Math.max(2, cellSize / 4);
                ArcadeTheme.disc(context, cx, cy, radius, 0xFF0A1422);
                for (int d = -radius; d <= radius; d += 2) {
                    context.fill(cx + d, cy - radius - 2, cx + d + 1, cy + radius + 3, 0xFF0A1422);
                    context.fill(cx - radius - 2, cy + d, cx + radius + 3, cy + d + 1, 0xFF0A1422);
                }
                context.fill(cx - radius / 2, cy - radius / 2, cx - radius / 2 + 1, cy - radius / 2 + 1, 0xFFFFFFFF);
            } else if (neighborCounts[row][column] > 0) {
                context.drawCenteredTextWithShadow(textRenderer,
                        Text.literal(Integer.toString(neighborCounts[row][column])),
                        x + cellSize / 2, y + cellSize / 2 - 4, numberColor(neighborCounts[row][column]));
            } else {
                ArcadeTheme.disc(context, x + cellSize / 2, y + cellSize / 2, Math.max(1, cellSize / 12), 0xFF2A3F57);
            }
        } else {
            ArcadeTheme.rounded(context, x + inset, y + inset, x + cellSize - inset, y + cellSize - inset, 4,
                    hovered ? 0xFF3A6285 : 0xFF2B5878);
            ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                    y + cellSize / 2, 3, 0x1FFFFFFF);
            ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cellSize - inset - 1,
                    y + cellSize - inset - 1, 3, hovered ? 0x0DFFFFFF : 0x00000000);
            if (flagged[row][column]) {
                int cx = x + cellSize / 2;
                int cy = y + cellSize / 2;
                boolean wrong = gameOver && !isMine;
                context.fill(cx, cy - cellSize / 4, cx + 2, cy + cellSize / 4, 0xFFE8EDF5);
                for (int i = 0; i < Math.max(3, cellSize / 3); i++) {
                    context.fill(cx - i, cy - cellSize / 4 + i, cx - i + 2, cy - cellSize / 4 + i + 1,
                            wrong ? ArcadeTheme.DANGER : ArcadeTheme.MAGENTA);
                }
                if (wrong) {
                    ArcadeTheme.drawX(context, cx + 1, cy + cellSize / 6, Math.max(2, cellSize / 8), 1, ArcadeTheme.DANGER);
                }
            }
        }
    }

    private int numberColor(int number) {
        return switch (number) {
            case 1 -> 0xFF2050DD;
            case 2 -> 0xFF25852A;
            case 3 -> 0xFFCC3030;
            case 4 -> 0xFF542B9A;
            case 5 -> 0xFF802323;
            case 6 -> 0xFF238A8A;
            case 7 -> 0xFF222222;
            default -> 0xFF777777;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int column = (int) ((mouseX - gridLeft) / cellSize);
        int row = (int) ((mouseY - gridTop) / cellSize);
        if (mouseX >= gridLeft && mouseY >= gridTop && isInside(row, column)) {
            if (button == 0) {
                reveal(row, column);
                return true;
            }
            if (button == 1) {
                toggleFlag(row, column);
                return true;
            }
            if (button == 2) {
                chord(row, column);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
