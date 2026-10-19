package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Jogo da velha contra uma IA que avalia todas as possibilidades. */
public final class TicTacToeScreen extends Screen {
    private final int[][] board = new int[3][3];
    private int cell = 82;
    private int boardLeft;
    private int boardTop;
    private int result;
    private int lastRow = -1;
    private int lastColumn = -1;
    private boolean statsCounted;
    private static int wins;
    private static int losses;
    private static int draws;

    public TicTacToeScreen() {
        super(Text.literal("Jogo da Velha"));
    }

    @Override
    protected void init() {
        cell = Math.max(44, Math.min(92, Math.min((width - 60) / 3, (height - 150) / 3)));
        boardLeft = (width - cell * 3) / 2;
        boardTop = Math.max(76, (height - cell * 3 - 110) / 2 + 18);
        int buttonY = Math.min(height - 26, boardTop + cell * 3 + 24);
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
        lastRow = -1;
        lastColumn = -1;
        statsCounted = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        int accent = ArcadeTheme.TEAL;
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, accent);

        String status = switch (result) {
            case 1 -> "Você venceu! Que jogada!";
            case 2 -> "O computador venceu. Revanche?";
            case 3 -> "Empate! Ninguém cedeu.";
            default -> "Você é X  •  clique em uma casa";
        };
        int statusColor = result == 1 ? ArcadeTheme.SUCCESS : result == 2 ? ArcadeTheme.DANGER
                : result == 3 ? ArcadeTheme.GOLD : ArcadeTheme.MUTED;
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 34, statusColor);

        int statsY = 50;
        String stats = "V " + wins + "   •   E " + draws + "   •   D " + losses;
        ArcadeTheme.pill(context, textRenderer, stats, (width - textRenderer.getWidth(stats) - 18) / 2, statsY, ArcadeTheme.GOLD);

        // Moldura do tabuleiro.
        int pad = 8;
        ArcadeTheme.rounded(context, boardLeft - pad + 2, boardTop - pad + 4, boardLeft + cell * 3 + pad + 2,
                boardTop + cell * 3 + pad + 4, 12, 0x55000000);
        ArcadeTheme.rounded(context, boardLeft - pad, boardTop - pad, boardLeft + cell * 3 + pad,
                boardTop + cell * 3 + pad, 12, 0x66FFFFFF);
        ArcadeTheme.rounded(context, boardLeft - pad + 1, boardTop - pad + 1, boardLeft + cell * 3 + pad - 1,
                boardTop + cell * 3 + pad - 1, 11, 0xF00B1626);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int x = boardLeft + column * cell;
                int y = boardTop + row * cell;
                int inset = 4;
                boolean hovered = result == 0 && board[row][column] == 0
                        && mouseX >= x && mouseX < x + cell && mouseY >= y && mouseY < y + cell;
                int fill = hovered ? 0xFF1D3B4B : 0xFF122334;
                ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell - inset, 8, fill);
                ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell / 2, 8, 0x08FFFFFF);
                if ((row == lastRow && column == lastColumn) && board[row][column] != 0) {
                    ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell - inset, 8, 0x22FFD66B);
                }
                if (board[row][column] != 0) {
                    boolean player = board[row][column] == 1;
                    int color = player ? ArcadeTheme.TEAL : ArcadeTheme.CORAL;
                    if (player) {
                        ArcadeTheme.drawX(context, x + cell / 2, y + cell / 2, cell / 2 - 16,
                                Math.max(3, cell / 14), color);
                    } else {
                        ArcadeTheme.drawO(context, x + cell / 2, y + cell / 2, cell / 2 - 14,
                                Math.max(3, cell / 14), color);
                    }
                } else if (hovered) {
                    ArcadeTheme.drawX(context, x + cell / 2, y + cell / 2, cell / 2 - 16,
                            Math.max(2, cell / 18), 0x44FFFFFF);
                }
            }
        }

        if (result != 0 && result != 3) {
            int[][] line = winningLine();
            if (line != null) {
                int startX = boardLeft + line[0][1] * cell + cell / 2;
                int startY = boardTop + line[0][0] * cell + cell / 2;
                int endX = boardLeft + line[2][1] * cell + cell / 2;
                int endY = boardTop + line[2][0] * cell + cell / 2;
                int steps = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
                for (int i = 0; i <= steps; i++) {
                    int px = startX + (endX - startX) * i / Math.max(1, steps);
                    int py = startY + (endY - startY) * i / Math.max(1, steps);
                    ArcadeTheme.disc(context, px, py, 4, ArcadeTheme.GOLD);
                }
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private int[][] winningLine() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return new int[][]{{i, 0}, {i, 1}, {i, 2}};
            }
            if (board[0][i] != 0 && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return new int[][]{{0, i}, {1, i}, {2, i}};
            }
        }
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return new int[][]{{0, 0}, {1, 1}, {2, 2}};
        }
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return new int[][]{{0, 2}, {1, 1}, {2, 0}};
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || result != 0 || mouseX < boardLeft || mouseY < boardTop
                || mouseX >= boardLeft + cell * 3 || mouseY >= boardTop + cell * 3) {
            return false;
        }
        int row = (int) ((mouseY - boardTop) / cell);
        int column = (int) ((mouseX - boardLeft) / cell);
        if (board[row][column] != 0) {
            return true;
        }
        board[row][column] = 1;
        lastRow = row;
        lastColumn = column;
        result = winner(board);
        if (result == 0) {
            aiMove();
            result = winner(board);
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
        else if (result == 3) draws++;
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
            lastRow = bestRow;
            lastColumn = bestColumn;
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
            for (int cellValue : row) {
                if (cellValue == 0) return 0;
            }
        }
        return 3;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
