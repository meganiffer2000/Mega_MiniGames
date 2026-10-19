package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Damas contra uma IA simples; capturas obrigatórias e coroação de damas incluídas. */
public final class CheckersScreen extends Screen {
    private static final int SIDE = 8;
    private int cell = 42;
    private final int[][] board = new int[SIDE][SIDE];
    private int left;
    private int top;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private int result;

    public CheckersScreen() {
        super(Text.literal("Damas"));
    }

    @Override
    protected void init() {
        cell = Math.max(26, Math.min(44, Math.min((width - 44) / SIDE, (height - 156) / SIDE)));
        left = (width - SIDE * cell) / 2;
        top = Math.max(78, (height - SIDE * cell - 110) / 2 + 14);
        int buttonY = Math.min(height - 26, top + SIDE * cell + 16);
        int buttonWidth = Math.min(112, Math.max(76, (width - 44) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20, Text.literal("Nova partida"), () -> newGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20, Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        newGame();
    }

    private void newGame() {
        for (int[] row : board) java.util.Arrays.fill(row, 0);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < SIDE; column++) {
                if ((row + column) % 2 == 1) board[row][column] = -1;
            }
        }
        for (int row = 5; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if ((row + column) % 2 == 1) board[row][column] = 1;
            }
        }
        selectedRow = -1;
        selectedColumn = -1;
        result = 0;
    }

    private List<Move> legalMoves(int player) {
        List<Move> captures = new ArrayList<>();
        List<Move> simple = new ArrayList<>();
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int piece = board[row][column];
                if (piece == 0 || Integer.signum(piece) != player) continue;
                int[] rowDirections = Math.abs(piece) == 2 ? new int[]{-1, 1} : new int[]{-player};
                for (int rowStep : rowDirections) {
                    for (int columnStep : new int[]{-1, 1}) {
                        int nextRow = row + rowStep;
                        int nextColumn = column + columnStep;
                        if (!inside(nextRow, nextColumn)) continue;
                        if (board[nextRow][nextColumn] == 0) {
                            simple.add(new Move(row, column, nextRow, nextColumn, -1, -1));
                        } else if (Integer.signum(board[nextRow][nextColumn]) == -player) {
                            int landingRow = nextRow + rowStep;
                            int landingColumn = nextColumn + columnStep;
                            if (inside(landingRow, landingColumn) && board[landingRow][landingColumn] == 0) {
                                captures.add(new Move(row, column, landingRow, landingColumn, nextRow, nextColumn));
                            }
                        }
                    }
                }
            }
        }
        return captures.isEmpty() ? simple : captures;
    }

    private boolean inside(int row, int column) {
        return row >= 0 && row < SIDE && column >= 0 && column < SIDE;
    }

    private void apply(Move move) {
        int piece = board[move.fromRow][move.fromColumn];
        board[move.fromRow][move.fromColumn] = 0;
        board[move.toRow][move.toColumn] = piece;
        if (move.captureRow >= 0) board[move.captureRow][move.captureColumn] = 0;
        if (piece == 1 && move.toRow == 0) board[move.toRow][move.toColumn] = 2;
        if (piece == -1 && move.toRow == SIDE - 1) board[move.toRow][move.toColumn] = -2;
    }

    private void aiMove() {
        List<Move> moves = legalMoves(-1);
        if (!moves.isEmpty()) apply(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
    }

    private boolean hasPieces(int player) {
        for (int[] row : board) for (int piece : row) if (Integer.signum(piece) == player) return true;
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, ArcadeTheme.GOLD);
        String status = switch (result) {
            case 1 -> "Você venceu!";
            case 2 -> "A IA venceu.";
            case 3 -> "Empate.";
            default -> "Peças claras  •  clique para selecionar e mover";
        };
        int statusColor = result == 1 ? ArcadeTheme.SUCCESS : result == 2 ? ArcadeTheme.DANGER
                : result == 3 ? ArcadeTheme.GOLD : ArcadeTheme.MUTED;
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 31, statusColor);

        int pad = 7;
        ArcadeTheme.rounded(context, left - pad + 2, top - pad + 5, left + SIDE * cell + pad + 2,
                top + SIDE * cell + pad + 5, 12, 0x55000000);
        ArcadeTheme.rounded(context, left - pad, top - pad, left + SIDE * cell + pad, top + SIDE * cell + pad,
                12, 0x99FFD66B);
        ArcadeTheme.rounded(context, left - pad + 1, top - pad + 1, left + SIDE * cell + pad - 1,
                top + SIDE * cell + pad - 1, 11, 0xF00B1626);

        List<Move> targets = selectedRow >= 0 ? legalMoves(1) : List.of();
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cell;
                int y = top + row * cell;
                boolean dark = (row + column) % 2 == 1;
                ArcadeTheme.rounded(context, x + 1, y + 1, x + cell - 1, y + cell - 1, 3,
                        dark ? 0xFF5E4433 : 0xFFE8D6B4);
                if (dark) {
                    ArcadeTheme.rounded(context, x + 1, y + 1, x + cell - 1, y + cell / 2, 3, 0x10FFFFFF);
                }
                if (row == selectedRow && column == selectedColumn) {
                    ArcadeTheme.rounded(context, x + 2, y + 2, x + cell - 2, y + cell - 2, 4, 0xAA35C8B9);
                }
                for (Move move : targets) {
                    if (move.fromRow == selectedRow && move.fromColumn == selectedColumn
                            && move.toRow == row && move.toColumn == column) {
                        ArcadeTheme.disc(context, x + cell / 2, y + cell / 2, Math.max(3, cell / 8), 0xCCFFD66B);
                        ArcadeTheme.ring(context, x + cell / 2, y + cell / 2, Math.max(5, cell / 5), 1, 0x88FFD66B);
                    }
                }
                int piece = board[row][column];
                if (piece != 0) {
                    int centerX = x + cell / 2;
                    int centerY = y + cell / 2;
                    int radius = cell / 2 - 4;
                    boolean light = piece > 0;
                    int base = light ? 0xFFF4EFE2 : 0xFF2B3038;
                    ArcadeTheme.disc(context, centerX, centerY + 1, radius, 0x55000000);
                    ArcadeTheme.disc(context, centerX, centerY, radius, ArcadeTheme.darken(base, 35));
                    ArcadeTheme.disc(context, centerX, centerY - 1, radius - 2, base);
                    ArcadeTheme.ring(context, centerX, centerY - 1, radius - 2, 2, light ? 0xFFCDBE9D : 0xFF444C58);
                    if (Math.abs(piece) == 2) {
                        ArcadeTheme.star(context, centerX, centerY - 1, Math.max(2, cell / 9), ArcadeTheme.GOLD);
                    }
                }
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
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0 || result != 0 || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * cell || mouseY >= top + SIDE * cell) return false;
        int row = (int) ((mouseY - top) / cell);
        int column = (int) ((mouseX - left) / cell);
        if (selectedRow < 0) {
            if (board[row][column] == 1 || board[row][column] == 2) {
                selectedRow = row;
                selectedColumn = column;
            }
            return true;
        }
        if (board[row][column] == 1 || board[row][column] == 2) {
            selectedRow = row;
            selectedColumn = column;
            return true;
        }

        for (Move move : legalMoves(1)) {
            if (move.fromRow == selectedRow && move.fromColumn == selectedColumn
                    && move.toRow == row && move.toColumn == column) {
                apply(move);
                selectedRow = -1;
                selectedColumn = -1;
                if (!hasPieces(-1) || legalMoves(-1).isEmpty()) {
                    result = 1;
                } else {
                    aiMove();
                    if (!hasPieces(1) || legalMoves(1).isEmpty()) result = 2;
                }
                return true;
            }
        }
        selectedRow = -1;
        selectedColumn = -1;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Move(int fromRow, int fromColumn, int toRow, int toColumn,
                        int captureRow, int captureColumn) { }
}
