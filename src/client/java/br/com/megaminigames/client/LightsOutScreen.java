package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Lights Out: apague todas as luzes; cada clique também alterna as vizinhas. */
public final class LightsOutScreen extends Screen {
    private static final int SIDE = 5;
    private static final int CELL = 54;
    private final boolean[][] lights = new boolean[SIDE][SIDE];
    private int left;
    private int top;
    private int moves;
    private boolean initialized;

    public LightsOutScreen() {
        super(Text.literal("Luzes"));
    }

    @Override
    protected void init() {
        left = (width - SIDE * CELL) / 2;
        top = Math.max(56, (height - SIDE * CELL - 65) / 2);
        int buttonY = Math.min(height - 24, top + SIDE * CELL + 18);
        addDrawableChild(ButtonWidget.builder(Text.literal("Novo quebra-cabeça"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) {
            newGame();
        }
    }

    private void newGame() {
        for (boolean[] row : lights) {
            java.util.Arrays.fill(row, false);
        }
        moves = 0;
        initialized = true;
        for (int i = 0; i < 18; i++) {
            toggle(ThreadLocalRandom.current().nextInt(SIDE), ThreadLocalRandom.current().nextInt(SIDE));
        }
        if (isSolved()) {
            toggle(2, 2);
        }
        moves = 0;
    }

    private void toggle(int row, int column) {
        lights[row][column] = !lights[row][column];
        if (row > 0) lights[row - 1][column] = !lights[row - 1][column];
        if (row + 1 < SIDE) lights[row + 1][column] = !lights[row + 1][column];
        if (column > 0) lights[row][column - 1] = !lights[row][column - 1];
        if (column + 1 < SIDE) lights[row][column + 1] = !lights[row][column + 1];
    }

    private boolean isSolved() {
        for (boolean[] row : lights) {
            for (boolean light : row) {
                if (light) return false;
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        String status = isSolved() ? "Muito bem! Você apagou todas as luzes!" : "Cliques: " + moves
                + "  •  Um clique alterna a casa e suas vizinhas";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 35,
                isSolved() ? 0xFF70EE91 : 0xFFE7E7E7);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * CELL;
                int y = top + row * CELL;
                int color = lights[row][column] ? 0xFFFFD65A : 0xFF35424F;
                context.fill(x + 3, y + 3, x + CELL - 3, y + CELL - 3, color);
                if (lights[row][column]) {
                    context.fill(x + 12, y + 12, x + CELL - 12, y + CELL - 12, 0x55FFFFFF);
                }
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || isSolved() || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * CELL || mouseY >= top + SIDE * CELL) {
            return false;
        }
        int column = (int) ((mouseX - left) / CELL);
        int row = (int) ((mouseY - top) / CELL);
        toggle(row, column);
        moves++;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
