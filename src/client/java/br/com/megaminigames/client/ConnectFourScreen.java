package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Ligue 4 contra uma IA que procura vitórias e bloqueios imediatos. */
public final class ConnectFourScreen extends Screen {
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private static final int CELL = 42;
    private final int[][] board = new int[ROWS][COLUMNS];
    private int boardLeft;
    private int boardTop;
    private int result;

    public ConnectFourScreen() {
        super(Text.literal("Ligue 4"));
    }

    @Override
    protected void init() {
        boardLeft = (width - COLUMNS * CELL) / 2;
        boardTop = Math.max(54, (height - ROWS * CELL - 60) / 2);
        int buttonY = Math.min(height - 24, boardTop + ROWS * CELL + 14);
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova partida"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
    }

    private void newGame() {
        for (int[] row : board) {
            java.util.Arrays.fill(row, 0);
        }
        result = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        String status = switch (result) {
            case 1 -> "Você conectou quatro!";
            case 2 -> "O computador venceu.";
            case 3 -> "Empate!";
            default -> "Você é vermelho • Clique em uma coluna";
        };
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 34, 0xFFE8F0FF);

        context.fill(boardLeft, boardTop, boardLeft + COLUMNS * CELL,
                boardTop + ROWS * CELL, 0xFF2872C9);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int centerX = boardLeft + column * CELL + CELL / 2;
                int centerY = boardTop + row * CELL + CELL / 2;
                int color = switch (board[row][column]) {
                    case 1 -> 0xFFEF4D4D;
                    case 2 -> 0xFFFFD447;
                    default -> 0xFF172434;
                };
                drawDisc(context, centerX, centerY, CELL / 2 - 5, color);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawDisc(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = (int) Math.sqrt(radius * radius - dy * dy);
            context.fill(centerX - halfWidth, centerY + dy, centerX + halfWidth + 1, centerY + dy + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || result != 0 || mouseX < boardLeft || mouseX >= boardLeft + COLUMNS * CELL
                || mouseY < boardTop || mouseY >= boardTop + ROWS * CELL) {
            return false;
        }
        int column = (int) ((mouseX - boardLeft) / CELL);
        if (!drop(column, 1)) {
            return true;
        }
        result = checkResult(column);
        if (result == 0) {
            aiMove();
            result = checkResult(lastMoveColumn);
        }
        return true;
    }

    private int lastMoveColumn;

    private boolean drop(int column, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = player;
                lastMoveColumn = column;
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
