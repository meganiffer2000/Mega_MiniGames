package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    private final int[][] solution = new int[SIDE][SIDE];
    private int cellSize = 48;
    private int left;
    private int top;
    private int padY;
    private int padSize = 26;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private boolean won;
    private boolean initialized;

    public MiniSudokuScreen() {
        super(Text.literal("Mini Sudoku"));
    }

    @Override
    protected void init() {
        cellSize = Math.max(24, Math.min(MAX_CELL, Math.min((width - 44) / SIDE, (height - 178) / SIDE)));
        left = (width - SIDE * cellSize) / 2;
        top = Math.max(78, (height - SIDE * cellSize - 110) / 2 + 14);
        padSize = Math.max(20, Math.min(30, cellSize / 2));
        padY = top + SIDE * cellSize + 12;
        if (padY + padSize > height - 34) {
            padY = 0;
        }
        int buttonY = height - 26;
        int buttonWidth = Math.min(112, Math.max(74, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo jogo"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            newGame();
            initialized = true;
        }
    }

    private void newGame() {
        int[] digits = {1, 2, 3, 4};
        for (int i = digits.length - 1; i > 0; i--) {
            int other = ThreadLocalRandom.current().nextInt(i + 1);
            int value = digits[i];
            digits[i] = digits[other];
            digits[other] = value;
        }
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                solution[row][column] = digits[SOLUTION[row][column] - 1];
            }
        }
        int[][] puzzle = {
                {1, 0, 0, 4}, {0, 4, 1, 0}, {2, 0, 0, 3}, {0, 3, 2, 0}
        };
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                board[row][column] = puzzle[row][column] == 0 ? 0 : digits[puzzle[row][column] - 1];
                fixed[row][column] = board[row][column] != 0;
            }
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
                if (board[row][column] != solution[row][column]) return;
            }
        }
        won = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.status(context, textRenderer,
                Text.literal(won ? "Correto! Sudoku resolvido!" : "Clique em uma casa vazia e digite 1–4"),
                width / 2, 33, won ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);

        int pad = 7;
        ArcadeTheme.rounded(context, left - pad + 2, top - pad + 4, left + SIDE * cellSize + pad + 2,
                top + SIDE * cellSize + pad + 4, 12, 0x55000000);
        ArcadeTheme.rounded(context, left - pad, top - pad, left + SIDE * cellSize + pad,
                top + SIDE * cellSize + pad, 12, 0x666CC9F5);
        ArcadeTheme.rounded(context, left - pad + 1, top - pad + 1, left + SIDE * cellSize + pad - 1,
                top + SIDE * cellSize + pad - 1, 11, 0xF00B1626);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cellSize;
                int y = top + row * cellSize;
                boolean selected = row == selectedRow && column == selectedColumn;
                boolean sameRow = row == selectedRow;
                boolean sameColumn = column == selectedColumn;
                boolean sameBlock = selectedRow >= 0 && row / 2 == selectedRow / 2 && column / 2 == selectedColumn / 2;
                int fill = ((row / 2 + column / 2) % 2 == 0 ? 0xFF1A2C42 : 0xFF16263A);
                if (sameRow || sameColumn || sameBlock) fill = 0xFF203A56;
                if (selected) fill = 0xFF2E6C8C;
                ArcadeTheme.rounded(context, x + 2, y + 2, x + cellSize - 2, y + cellSize - 2, 4, fill);
                if (board[row][column] != 0) {
                    int color = fixed[row][column] ? 0xFFF2F7FF : (won ? ArcadeTheme.SUCCESS : ArcadeTheme.SKY);
                    context.drawCenteredTextWithShadow(textRenderer,
                            Text.literal(Integer.toString(board[row][column])),
                            x + cellSize / 2, y + cellSize / 2 - 4, color);
                } else if (sameRow || sameColumn || sameBlock) {
                    ArcadeTheme.disc(context, x + cellSize / 2, y + cellSize / 2, 1, 0x44FFFFFF);
                }
            }
        }
        for (int index = 0; index <= SIDE; index++) {
            int thickness = index % 2 == 0 ? 3 : 1;
            int lineX = left + index * cellSize;
            int lineY = top + index * cellSize;
            context.fill(lineX - thickness / 2, top, lineX + (thickness + 1) / 2,
                    top + SIDE * cellSize, 0xFF7FD0F0);
            context.fill(left, lineY - thickness / 2, left + SIDE * cellSize,
                    lineY + (thickness + 1) / 2, 0xFF7FD0F0);
        }

        if (padY > 0) {
            int total = SIDE * padSize + (SIDE - 1) * 6 + 8 + padSize + 12;
            int startX = (width - total) / 2;
            for (int number = 1; number <= SIDE; number++) {
                int x = startX + (number - 1) * (padSize + 6);
                boolean hovered = mouseX >= x && mouseX < x + padSize && mouseY >= padY && mouseY < padY + padSize;
                ArcadeTheme.rounded(context, x, padY, x + padSize, padY + padSize, 6,
                        hovered ? 0xFF2E4B69 : 0xFF1B2E44);
                ArcadeTheme.rounded(context, x + 1, padY + 1, x + padSize - 1, padY + padSize - 1, 5, 0x10FFFFFF);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(number)),
                        x + padSize / 2, padY + padSize / 2 - 4, ArcadeTheme.SKY);
            }
            int eraseX = startX + SIDE * (padSize + 6) + 8;
            boolean hovered = mouseX >= eraseX && mouseX < eraseX + padSize && mouseY >= padY && mouseY < padY + padSize;
            ArcadeTheme.rounded(context, eraseX, padY, eraseX + padSize, padY + padSize, 6,
                    hovered ? 0xFF4A2B35 : 0xFF2A2230);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"), eraseX + padSize / 2,
                    padY + padSize / 2 - 4, ArcadeTheme.DANGER);
        }

        if (won) {
            int bannerWidth = Math.min(width - 40, 230);
            int cx = width / 2;
            int cy = top + SIDE * cellSize / 2;
            ArcadeTheme.rounded(context, cx - bannerWidth / 2, cy - 18, cx + bannerWidth / 2, cy + 18, 10, 0xEE0A1422);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("SUDOKU RESOLVIDO!"), cx, cy - 8, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Pressione R para outro"), cx, cy + 4, ArcadeTheme.TEXT);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (padY > 0 && mouseY >= padY && mouseY < padY + padSize) {
            int total = SIDE * padSize + (SIDE - 1) * 6 + 8 + padSize + 12;
            int startX = (width - total) / 2;
            for (int number = 1; number <= SIDE; number++) {
                int x = startX + (number - 1) * (padSize + 6);
                if (mouseX >= x && mouseX < x + padSize) {
                    enterNumber(number);
                    return true;
                }
            }
            int eraseX = startX + SIDE * (padSize + 6) + 8;
            if (mouseX >= eraseX && mouseX < eraseX + padSize) {
                enterNumber(0);
                return true;
            }
        }
        if (mouseX >= left && mouseX < left + SIDE * cellSize
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
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            enterNumber(0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
