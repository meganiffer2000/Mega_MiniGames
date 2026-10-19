package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Sudoku compacto 4x4 com teclado numérico e casas pré-preenchidas. */
public final class MiniSudokuScreen extends Screen {
    private static final int SIDE = 4;
    private static final int MAX_CELL = 62;
    private static final int[][] SOLUTION = {
            {1, 2, 3, 4}, {3, 4, 1, 2}, {2, 1, 4, 3}, {4, 3, 2, 1}
    };
    private final int[][] board = new int[SIDE][SIDE];
    private final boolean[][] fixed = new boolean[SIDE][SIDE];
    private int cellSize;
    private int left;
    private int top;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private boolean won;

    public MiniSudokuScreen() {
        super(Text.literal("Mini Sudoku"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(28, Math.min(MAX_CELL, Math.min((width - 36) / SIDE, (height - 112) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(56, (height - SIDE * cellSize - 65) / 2);
        int buttonY = height - 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo jogo"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private boolean initialized;

    private void newGame() {
        int[][] puzzle = {
                {1, 0, 0, 4}, {0, 4, 1, 0}, {2, 0, 0, 3}, {0, 3, 2, 0}
        };
        for (int row = 0; row < SIDE; row++) {
            System.arraycopy(puzzle[row], 0, board[row], 0, SIDE);
            for (int column = 0; column < SIDE; column++) fixed[row][column] = board[row][column] != 0;
        }
        selectedRow = -1;
        selectedColumn = -1;
        won = false;
    }

    private void enterNumber(int number) {
        if (selectedRow >= 0 && !fixed[selectedRow][selectedColumn] && !won) {
            board[selectedRow][selectedColumn] = number;
            checkWin();
        }
    }

    private void checkWin() {
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] != SOLUTION[row][column]) return;
            }
        }
        won = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(won ? "Correto! Sudoku resolvido!" : "Clique em uma casa vazia e digite 1–4"),
                width / 2, 36, won ? 0xFF71E98A : 0xFFE8E8E8);
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                int background = row == selectedRow && column == selectedColumn
                        ? 0xFF526C89 : ((row / 2 + column / 2) % 2 == 0 ? 0xFFE9E5DC : 0xFFD7D1C5);
                context.fill(x, y, x + cellSize, y + cellSize, background);
                if (board[row][column] != 0) {
                    context.drawCenteredTextWithShadow(textRenderer,
                            Text.literal(Integer.toString(board[row][column])),
                            x + cellSize / 2, y + cellSize / 2 - 4,
                            fixed[row][column] ? 0xFF202832 : 0xFF62B8FF);
                }
            }
        }
        for (int index = 0; index <= SIDE; index++) {
            int thickness = index % 2 == 0 ? 3 : 1;
            int lineX = left + index * cellSize;
            int lineY = top + index * cellSize;
            context.fill(lineX - thickness / 2, top, lineX + (thickness + 1) / 2,
                    top + SIDE * cellSize, 0xFF273440);
            context.fill(left, lineY - thickness / 2, left + SIDE * cellSize,
                    lineY + (thickness + 1) / 2, 0xFF273440);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && mouseX >= left && mouseX < left + SIDE * cellSize
                && mouseY >= top && mouseY < top + SIDE * cellSize) {
            selectedColumn = (int) ((mouseX - left) / cellSize);
            selectedRow = (int) ((mouseY - top) / cellSize);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_4) {
            enterNumber(keyCode - GLFW.GLFW_KEY_0);
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE)
                && selectedRow >= 0 && !fixed[selectedRow][selectedColumn]) {
            board[selectedRow][selectedColumn] = 0;
            won = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
