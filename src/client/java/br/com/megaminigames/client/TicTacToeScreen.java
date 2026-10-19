package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Jogo da velha contra uma IA que avalia todas as possibilidades. */
public final class TicTacToeScreen extends Screen {
    private static final int CELL = 82;
    private final int[][] board = new int[3][3];
    private int boardLeft;
    private int boardTop;
    private int result;

    public TicTacToeScreen() {
        super(Text.literal("Jogo da Velha"));
    }

    @Override
    protected void init() {
        boardLeft = (width - CELL * 3) / 2;
        boardTop = Math.max(58, (height - CELL * 3 - 55) / 2);
        int buttonY = Math.min(height - 24, boardTop + CELL * 3 + 20);
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
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);
        String status = switch (result) {
            case 1 -> "Você venceu!";
            case 2 -> "O computador venceu.";
            case 3 -> "Empate!";
            default -> "Você é X • Clique em uma casa";
        };
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 36,
                result == 1 ? 0xFF6DEE83 : 0xFFFFFFFF);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int x = boardLeft + column * CELL;
                int y = boardTop + row * CELL;
                context.fill(x + 2, y + 2, x + CELL - 2, y + CELL - 2, 0xFF273744);
                if (board[row][column] != 0) {
                    String symbol = board[row][column] == 1 ? "X" : "O";
                    int color = board[row][column] == 1 ? 0xFF55D9E8 : 0xFFFF9A62;
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(symbol),
                            x + CELL / 2, y + CELL / 2 - 9, color);
                }
            }
        }
        for (int i = 1; i < 3; i++) {
            context.fill(boardLeft + i * CELL - 2, boardTop, boardLeft + i * CELL + 2,
                    boardTop + CELL * 3, 0xFFE4EDF0);
            context.fill(boardLeft, boardTop + i * CELL - 2, boardLeft + CELL * 3,
                    boardTop + i * CELL + 2, 0xFFE4EDF0);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || result != 0 || mouseX < boardLeft || mouseY < boardTop
                || mouseX >= boardLeft + CELL * 3 || mouseY >= boardTop + CELL * 3) {
            return false;
        }
        int row = (int) ((mouseY - boardTop) / CELL);
        int column = (int) ((mouseX - boardLeft) / CELL);
        if (board[row][column] != 0) {
            return true;
        }
        board[row][column] = 1;
        result = winner(board);
        if (result == 0) {
            aiMove();
            result = winner(board);
        }
        return true;
    }

    private void aiMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1;
        int bestColumn = -1;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                if (board[row][column] == 0) {
                    board[row][column] = 2;
                    int score = minimax(false);
                    board[row][column] = 0;
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = row;
                        bestColumn = column;
                    }
                }
            }
        }
        if (bestRow >= 0) {
            board[bestRow][bestColumn] = 2;
        }
    }

    private int minimax(boolean aiTurn) {
        int state = winner(board);
        if (state == 2) return 1;
        if (state == 1) return -1;
        if (state == 3) return 0;

        int best = aiTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                if (board[row][column] == 0) {
                    board[row][column] = aiTurn ? 2 : 1;
                    int score = minimax(!aiTurn);
                    board[row][column] = 0;
                    best = aiTurn ? Math.max(best, score) : Math.min(best, score);
                }
            }
        }
        return best;
    }

    private int winner(int[][] state) {
        for (int i = 0; i < 3; i++) {
            if (state[i][0] != 0 && state[i][0] == state[i][1] && state[i][1] == state[i][2]) {
                return state[i][0];
            }
            if (state[0][i] != 0 && state[0][i] == state[1][i] && state[1][i] == state[2][i]) {
                return state[0][i];
            }
        }
        if (state[0][0] != 0 && state[0][0] == state[1][1] && state[1][1] == state[2][2]) {
            return state[0][0];
        }
        if (state[0][2] != 0 && state[0][2] == state[1][1] && state[1][1] == state[2][0]) {
            return state[0][2];
        }
        for (int[] row : state) {
            for (int cell : row) {
                if (cell == 0) return 0;
            }
        }
        return 3;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
