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
    private static final int CELL = 42;
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
        left = (width - SIDE * CELL) / 2;
        top = Math.max(54, (height - SIDE * CELL - 60) / 2);
        int buttonY = Math.min(height - 24, top + SIDE * CELL + 12);
        addDrawableChild(ButtonWidget.builder(Text.literal("Nova partida"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
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
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        String status = switch (result) {
            case 1 -> "Você venceu!";
            case 2 -> "A IA venceu.";
            case 3 -> "Empate.";
            default -> "Você joga com as peças claras • Clique para selecionar e mover";
        };
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 31, 0xFFE8E8E8);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * CELL;
                int y = top + row * CELL;
                boolean dark = (row + column) % 2 == 1;
                context.fill(x, y, x + CELL, y + CELL, dark ? 0xFF76563F : 0xFFE5D1AE);
                if (row == selectedRow && column == selectedColumn) {
                    context.fill(x + 2, y + 2, x + CELL - 2, y + CELL - 2, 0xAA75D66A);
                }
                int piece = board[row][column];
                if (piece != 0) {
                    int color = piece > 0 ? 0xFFF2EDE0 : 0xFF292D35;
                    drawDisc(context, x + CELL / 2, y + CELL / 2, CELL / 2 - 5, color);
                    if (Math.abs(piece) == 2) {
                        context.drawCenteredTextWithShadow(textRenderer, Text.literal("K"),
                                x + CELL / 2, y + CELL / 2 - 4, piece > 0 ? 0xFF333333 : 0xFFFFFFFF);
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
                || mouseX >= left + SIDE * CELL || mouseY >= top + SIDE * CELL) return false;
        int row = (int) ((mouseY - top) / CELL);
        int column = (int) ((mouseX - left) / CELL);
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
