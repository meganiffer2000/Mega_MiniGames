package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Ligue 4 contra uma IA que procura vitórias e bloqueios imediatos. */
public final class ConnectFourScreen extends Screen {
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private final int[][] board = new int[ROWS][COLUMNS];
    private int cell = 42;
    private int boardLeft;
    private int boardTop;
    private int result;
    private int lastMoveColumn;
    private int lastMoveRow = -1;
    private boolean statsCounted;
    private static int wins;
    private static int losses;
    private static int draws;

    public ConnectFourScreen() {
        super(Text.literal("Ligue 4"));
    }

    @Override
    protected void init() {
        cell = Math.max(32, Math.min(52, Math.min((width - 56) / COLUMNS, (height - 168) / ROWS)));
        boardLeft = (width - COLUMNS * cell) / 2;
        boardTop = Math.max(82, (height - ROWS * cell - 110) / 2 + 16);
        int buttonY = Math.min(height - 26, boardTop + ROWS * cell + 20);
        int buttonWidth = Math.min(112, Math.max(76, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Nova partida"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
    }

    private void newGame() {
        for (int[] row : board) {
            java.util.Arrays.fill(row, 0);
        }
        result = 0;
        lastMoveRow = -1;
        statsCounted = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        int accent = ArcadeTheme.SKY;
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, accent);
        String status = switch (result) {
            case 1 -> "Você conectou quatro!";
            case 2 -> "O computador venceu.";
            case 3 -> "Empate! Tabuleiro cheio.";
            default -> "Você é vermelho  •  clique em uma coluna";
        };
        int statusColor = result == 1 ? ArcadeTheme.SUCCESS : result == 2 ? ArcadeTheme.DANGER
                : result == 3 ? ArcadeTheme.GOLD : ArcadeTheme.MUTED;
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 34, statusColor);
        ArcadeTheme.pill(context, textRenderer, "VITÓRIAS " + wins, boardLeft, 50, ArcadeTheme.TEAL);
        ArcadeTheme.pillRight(context, textRenderer, "DERROTAS " + losses, boardLeft + COLUMNS * cell, 50,
                ArcadeTheme.CORAL);

        int hoveredColumn = result == 0 && mouseY < boardTop && mouseX >= boardLeft
                && mouseX < boardLeft + COLUMNS * cell ? (int) ((mouseX - boardLeft) / cell) : -1;

        int pad = 8;
        ArcadeTheme.rounded(context, boardLeft - pad + 2, boardTop - pad + 5, boardLeft + COLUMNS * cell + pad + 2,
                boardTop + ROWS * cell + pad + 5, 14, 0x55000000);
        ArcadeTheme.rounded(context, boardLeft - pad, boardTop - pad, boardLeft + COLUMNS * cell + pad,
                boardTop + ROWS * cell + pad, 14, 0x882872C9);
        ArcadeTheme.rounded(context, boardLeft - pad + 1, boardTop - pad + 1, boardLeft + COLUMNS * cell + pad - 1,
                boardTop + ROWS * cell + pad - 1, 13, 0xF01B4E92);
        context.fillGradient(boardLeft, boardTop, boardLeft + COLUMNS * cell, boardTop + ROWS * cell,
                0xFF2C74C4, 0xFF1A3F79);

        int[][] winning = winningCells();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int centerX = boardLeft + column * cell + cell / 2;
                int centerY = boardTop + row * cell + cell / 2;
                int radius = cell / 2 - 4;
                ArcadeTheme.disc(context, centerX, centerY, radius + 1, 0xFF0B2340);
                int value = board[row][column];
                int piece = value == 1 ? 0xFFEF4D4D : 0xFFFFD447;
                if (value == 0) {
                    ArcadeTheme.disc(context, centerX, centerY, radius, 0xFF0E1B30);
                    ArcadeTheme.disc(context, centerX, centerY - 1, radius - 2, 0xFF15263E);
                } else {
                    ArcadeTheme.disc(context, centerX, centerY, radius, ArcadeTheme.darken(piece, 45));
                    ArcadeTheme.disc(context, centerX, centerY - 1, radius - 1, piece);
                    ArcadeTheme.disc(context, centerX - radius / 3, centerY - radius / 3, Math.max(2, radius / 4), 0x55FFFFFF);
                    if (row == lastMoveRow && column == lastMoveColumn) {
                        ArcadeTheme.ring(context, centerX, centerY, radius - 1, 2, 0xAAFFFFFF);
                    }
                    if (winning != null && contains(winning, row, column)) {
                        ArcadeTheme.ring(context, centerX, centerY, radius + 1, 2, ArcadeTheme.GOLD);
                    }
                }
            }
        }
        if (hoveredColumn >= 0) {
            int row = firstEmptyRow(hoveredColumn);
            if (row >= 0) {
                int centerX = boardLeft + hoveredColumn * cell + cell / 2;
                int centerY = boardTop + row * cell + cell / 2;
                context.fill(boardLeft + hoveredColumn * cell, boardTop - 8, boardLeft + (hoveredColumn + 1) * cell,
                        boardTop - 4, 0x88FFD66B);
                ArcadeTheme.disc(context, centerX, centerY, cell / 2 - 6, 0x55EF4D4D);
                ArcadeTheme.ring(context, centerX, centerY, cell / 2 - 5, 2, 0xAAFFFFFF);
            }
        }
        if (result != 0) {
            String title = result == 1 ? "VOCÊ VENCEU!" : result == 2 ? "CPU VENCEU" : "EMPATE";
            String subtitle = result == 1 ? "Quatro em linha!" : result == 2 ? "Tente outra coluna" : "Tabuleiro cheio";
            ArcadeTheme.banner(context, textRenderer, title, subtitle, width / 2,
                    boardTop + ROWS * cell / 2, Math.min(width - 24, COLUMNS * cell + 40),
                    result == 1 ? ArcadeTheme.SUCCESS : result == 2 ? ArcadeTheme.DANGER : ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private boolean contains(int[][] cells, int row, int column) {
        for (int[] piece : cells) {
            if (piece[0] == row && piece[1] == column) return true;
        }
        return false;
    }

    private int[][] winningCells() {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int player = board[row][column];
                if (player == 0) continue;
                for (int[] direction : directions) {
                    int[][] line = new int[4][2];
                    boolean ok = true;
                    for (int step = 0; step < 4; step++) {
                        int nextRow = row + direction[0] * step;
                        int nextColumn = column + direction[1] * step;
                        if (nextRow < 0 || nextRow >= ROWS || nextColumn < 0 || nextColumn >= COLUMNS
                                || board[nextRow][nextColumn] != player) {
                            ok = false;
                            break;
                        }
                        line[step] = new int[]{nextRow, nextColumn};
                    }
                    if (ok) return line;
                }
            }
        }
        return null;
    }

    private void drawDisc(DrawContext context, int centerX, int centerY, int radius, int color) {
        ArcadeTheme.disc(context, centerX, centerY, radius, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || result != 0 || mouseX < boardLeft || mouseX >= boardLeft + COLUMNS * cell
                || mouseY < boardTop || mouseY >= boardTop + ROWS * cell) {
            return false;
        }
        int column = (int) ((mouseX - boardLeft) / cell);
        if (!drop(column, 1)) {
            return true;
        }
        result = checkResult(column);
        if (result == 0) {
            aiMove();
            result = checkResult(lastMoveColumn);
        }
        countStats();
        return true;
    }

    private void countStats() {
        if (result == 0 || statsCounted) {
            return;
        }
        statsCounted = true;
        if (result == 1) wins++;
        else if (result == 2) losses++;
        else draws++;
    }

    private int firstEmptyRow(int column) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) return row;
        }
        return -1;
    }

    private boolean drop(int column, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = player;
                lastMoveColumn = column;
                lastMoveRow = row;
                return true;
            }
        }
        return false;
    }

    private void aiMove() {
        int column = findWinningColumn(2);
        if (column < 0) column = findWinningColumn(1);
        if (column < 0) {
            int[] preference = {3, 2, 4, 1, 5, 0, 6};
            for (int candidate : preference) {
                if (board[0][candidate] == 0) {
                    column = candidate;
                    break;
                }
            }
        }
        if (column >= 0) {
            drop(column, 2);
        }
    }

    private int findWinningColumn(int player) {
        for (int column = 0; column < COLUMNS; column++) {
            int row = ROWS - 1;
            while (row >= 0 && board[row][column] != 0) row--;
            if (row >= 0) {
                board[row][column] = player;
                boolean wins = isFour(row, column, player);
                board[row][column] = 0;
                if (wins) return column;
            }
        }
        return -1;
    }

    private int checkResult(int column) {
        for (int row = 0; row < ROWS; row++) {
            if (board[row][column] != 0 && isFour(row, column, board[row][column])) {
                return board[row][column];
            }
        }
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (board[row][col] != 0 && isFour(row, col, board[row][col])) {
                    return board[row][col];
                }
            }
        }
        for (int col = 0; col < COLUMNS; col++) {
            if (board[0][col] == 0) return 0;
        }
        return 3;
    }

    private boolean isFour(int row, int column, int player) {
        return connected(row, column, 1, 0, player) >= 4
                || connected(row, column, 0, 1, player) >= 4
                || connected(row, column, 1, 1, player) >= 4
                || connected(row, column, 1, -1, player) >= 4;
    }

    private int connected(int row, int column, int rowStep, int columnStep, int player) {
        return 1 + count(row, column, rowStep, columnStep, player)
                + count(row, column, -rowStep, -columnStep, player);
    }

    private int count(int row, int column, int rowStep, int columnStep, int player) {
        int found = 0;
        int nextRow = row + rowStep;
        int nextColumn = column + columnStep;
        while (nextRow >= 0 && nextRow < ROWS && nextColumn >= 0 && nextColumn < COLUMNS
                && board[nextRow][nextColumn] == player) {
            found++;
            nextRow += rowStep;
            nextColumn += columnStep;
        }
        return found;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
