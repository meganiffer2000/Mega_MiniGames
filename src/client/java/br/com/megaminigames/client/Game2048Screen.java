package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** 2048: deslize e una peças iguais para alcançar 2048. */
public final class Game2048Screen extends Screen {
    private static final int SIDE = 4;
    private static final int CELL = 68;
    private final int[][] board = new int[SIDE][SIDE];
    private int score;
    private boolean gameOver;
    private boolean reached2048;
    private boolean initialized;

    public Game2048Screen() {
        super(Text.literal("2048"));
    }

    @Override
    protected void init() {
        int top = Math.max(55, (height - SIDE * CELL - 70) / 2);
        int buttonY = Math.min(height - 24, top + SIDE * CELL + 20);
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo jogo (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        for (int[] row : board) {
            java.util.Arrays.fill(row, 0);
        }
        score = 0;
        gameOver = false;
        reached2048 = false;
        initialized = true;
        spawnTile();
        spawnTile();
    }

    private void spawnTile() {
        List<int[]> empty = new ArrayList<>();
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == 0) {
                    empty.add(new int[]{row, column});
                }
            }
        }
        if (!empty.isEmpty()) {
            int[] cell = empty.get(ThreadLocalRandom.current().nextInt(empty.size()));
            board[cell[0]][cell[1]] = ThreadLocalRandom.current().nextInt(10) == 0 ? 4 : 2;
        }
    }

    private void move(int direction) {
        if (gameOver) {
            return;
        }
        boolean changed = false;
        for (int line = 0; line < SIDE; line++) {
            int[] values = new int[SIDE];
            for (int position = 0; position < SIDE; position++) {
                values[position] = getCell(line, position, direction);
            }
            int[] merged = new int[SIDE];
            int write = 0;
            for (int position = 0; position < SIDE; position++) {
                if (values[position] == 0) {
                    continue;
                }
                if (position + 1 < SIDE && values[position] == values[position + 1]) {
                    merged[write] = values[position] * 2;
                    score += merged[write];
                    if (merged[write] >= 2048) {
                        reached2048 = true;
                    }
                    position++;
                } else {
                    merged[write] = values[position];
                }
                write++;
            }
            for (int position = 0; position < SIDE; position++) {
                if (merged[position] != values[position]) {
                    changed = true;
                }
                setCell(line, position, direction, merged[position]);
            }
        }
        if (changed) {
            spawnTile();
            gameOver = !hasMoves();
        }
    }

    private int getCell(int line, int position, int direction) {
        return switch (direction) {
            case 0 -> board[line][position]; // esquerda
            case 1 -> board[line][SIDE - 1 - position]; // direita
            case 2 -> board[position][line]; // cima
            default -> board[SIDE - 1 - position][line]; // baixo
        };
    }

    private void setCell(int line, int position, int direction, int value) {
        switch (direction) {
            case 0 -> board[line][position] = value;
            case 1 -> board[line][SIDE - 1 - position] = value;
            case 2 -> board[position][line] = value;
            default -> board[SIDE - 1 - position][line] = value;
        }
    }

    private boolean hasMoves() {
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                if (board[row][column] == 0) {
                    return true;
                }
                if (row + 1 < SIDE && board[row][column] == board[row + 1][column]) {
                    return true;
                }
                if (column + 1 < SIDE && board[row][column] == board[row][column + 1]) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        String instruction = reached2048 ? "2048 alcançado! Continue jogando ou reinicie."
                : "Pontos: " + score + "  •  Use as setas para mover as peças";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(instruction),
                width / 2, 31, reached2048 ? 0xFF61CC75 : 0xFFE7E7E7);
        int left = (width - SIDE * CELL) / 2;
        int top = Math.max(55, (height - SIDE * CELL - 70) / 2);
        context.fill(left - 6, top - 6, left + SIDE * CELL + 6, top + SIDE * CELL + 6, 0xFF8D7868);
        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int value = board[row][column];
                int x = left + column * CELL;
                int y = top + row * CELL;
                context.fill(x + 2, y + 2, x + CELL - 2, y + CELL - 2, tileColor(value));
                if (value > 0) {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(value)),
                            x + CELL / 2, y + CELL / 2 - 4, value <= 4 ? 0xFF62594F : 0xFFFFFFFF);
                }
            }
        }
        if (gameOver) {
            context.fill(left, top + 110, left + SIDE * CELL, top + 162, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Sem movimentos! Pressione R"),
                    width / 2, top + 132, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private int tileColor(int value) {
        return switch (value) {
            case 0 -> 0xFFBBAE9F;
            case 2 -> 0xFFEDE4D8;
            case 4 -> 0xFFE9D9B9;
            case 8 -> 0xFFF1B67B;
            case 16 -> 0xFFF28F62;
            case 32 -> 0xFFEB6C54;
            case 64 -> 0xFFE84C36;
            case 128 -> 0xFFE6D05B;
            case 256 -> 0xFFE5C842;
            case 512 -> 0xFFE5BE2E;
            case 1024 -> 0xFFDDB523;
            default -> 0xFFD6A913;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            newGame();
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> move(0);
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> move(1);
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> move(2);
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> move(3);
            default -> { return super.keyPressed(keyCode, scanCode, modifiers); }
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
