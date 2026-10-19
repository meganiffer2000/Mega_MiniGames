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
    private static final int GRID_TOP = 52;
    private int cellSize;

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
        int availableHeight = Math.max(ROWS * 10, height - GRID_TOP - 62);
        int heightLimitedCell = availableHeight / ROWS;
        int widthLimitedCell = Math.max(10, (width - 24) / COLUMNS);
        cellSize = Math.max(10, Math.min(MAX_CELL_SIZE, Math.min(heightLimitedCell, widthLimitedCell)));
        gridLeft = (width - COLUMNS * cellSize) / 2;
        int buttonY = height - 25;
        int buttonWidth = 112;
        int startX = (width - buttonWidth * 2 - 8) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("Novo jogo"), button -> newGame())
                .dimensions(startX, buttonY, buttonWidth, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(startX + buttonWidth + 8, buttonY, buttonWidth, 20)
                .build());

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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);

        String status;
        int statusColor;
        if (won) {
            status = "Você venceu! Todas as casas seguras foram reveladas.";
            statusColor = 0xFF77FF77;
        } else if (gameOver) {
            status = "Você encontrou uma mina! Tente novamente.";
            statusColor = 0xFFFF7777;
        } else {
            status = "Minas " + MINE_COUNT + "  |  Bandeiras " + countFlags() + "  •  Esq. abre / Dir. marca";
            statusColor = 0xFFFFFFFF;
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 39, statusColor);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                drawCell(context, row, column);
            }
        }
        super.render(context, mouseX, mouseY, delta);
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

    private void drawCell(DrawContext context, int row, int column) {
        int x = gridLeft + column * cellSize;
        int y = GRID_TOP + row * cellSize;
        int right = x + cellSize - 1;
        int bottom = y + cellSize - 1;

        if (revealed[row][column] || (gameOver && mines[row][column])) {
            int color = mines[row][column] ? 0xFFC84B4B : 0xFFB9B9B9;
            context.fill(x, y, right, bottom, color);
            if (mines[row][column]) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"),
                        x + cellSize / 2, y + cellSize / 2 - 4, 0xFFFFFFFF);
            } else if (neighborCounts[row][column] > 0) {
                context.drawCenteredTextWithShadow(textRenderer,
                        Text.literal(Integer.toString(neighborCounts[row][column])),
                        x + cellSize / 2, y + cellSize / 2 - 4, numberColor(neighborCounts[row][column]));
            }
        } else {
            context.fill(x, y, right, bottom, 0xFF6E7781);
            context.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFF969FA8);
            if (flagged[row][column]) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("F"),
                        x + cellSize / 2, y + cellSize / 2 - 4, 0xFFFF5555);
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
        int row = (int) ((mouseY - GRID_TOP) / cellSize);
        if (mouseX >= gridLeft && mouseY >= GRID_TOP && isInside(row, column)) {
            if (button == 0) {
                reveal(row, column);
                return true;
            }
            if (button == 1) {
                toggleFlag(row, column);
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
