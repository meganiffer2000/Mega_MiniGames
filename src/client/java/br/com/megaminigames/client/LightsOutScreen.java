package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Lights Out: apague todas as luzes; cada clique também alterna as vizinhas. */
public final class LightsOutScreen extends Screen {
    private static final int SIDE = 5;
    private final boolean[][] lights = new boolean[SIDE][SIDE];
    private int cell = 54;
    private int left;
    private int top;
    private int moves;
    private boolean initialized;

    public LightsOutScreen() {
        super(Text.literal("Luzes"));
    }

    @Override
    protected void init() {
        cell = Math.max(28, Math.min(56, Math.min((width - 52) / SIDE, (height - 160) / SIDE)));
        left = (width - SIDE * cell) / 2;
        top = Math.max(80, (height - SIDE * cell - 108) / 2 + 16);
        int buttonY = Math.min(height - 26, top + SIDE * cell + 20);
        int buttonWidth = Math.min(140, Math.max(80, (width - 44) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, buttonY, buttonWidth, 20,
                Text.literal("Novo quebra-cabeça"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, buttonY, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
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
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.GOLD);
        boolean solved = isSolved();
        String status = solved ? "Muito bem! Você apagou todas as luzes!"
                : "Cliques: " + moves + "  •  cada clique alterna a casa e as vizinhas";
        ArcadeTheme.status(context, textRenderer, Text.literal(status), width / 2, 33,
                solved ? ArcadeTheme.SUCCESS : ArcadeTheme.MUTED);

        int pad = 8;
        ArcadeTheme.rounded(context, left - pad + 2, top - pad + 4, left + SIDE * cell + pad + 2,
                top + SIDE * cell + pad + 4, 14, 0x55000000);
        ArcadeTheme.rounded(context, left - pad, top - pad, left + SIDE * cell + pad,
                top + SIDE * cell + pad, 14, 0x55FFD66B);
        ArcadeTheme.rounded(context, left - pad + 1, top - pad + 1, left + SIDE * cell + pad - 1,
                top + SIDE * cell + pad - 1, 13, 0xF00A1422);

        for (int row = 0; row < SIDE; row++) {
            for (int column = 0; column < SIDE; column++) {
                int x = left + column * cell;
                int y = top + row * cell;
                int inset = 3;
                boolean hovered = mouseX >= x && mouseX < x + cell && mouseY >= y && mouseY < y + cell;
                if (lights[row][column]) {
                    ArcadeTheme.glow(context, x + cell / 2, y + cell / 2, cell / 2 + 2, ArcadeTheme.GOLD);
                    ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell - inset, 8, 0xFFB8862B);
                    ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cell - inset - 1,
                            y + cell - inset - 1, 7, 0xFFFFD66B);
                    ArcadeTheme.rounded(context, x + inset + 2, y + inset + 2, x + cell - inset - 2,
                            y + cell / 2, 7, 0x55FFFFFF);
                    ArcadeTheme.disc(context, x + cell / 2, y + cell / 2, Math.max(2, cell / 10), 0xAAFFF3C4);
                } else {
                    ArcadeTheme.rounded(context, x + inset, y + inset, x + cell - inset, y + cell - inset, 8,
                            hovered ? 0xFF22374E : 0xFF162536);
                    ArcadeTheme.rounded(context, x + inset + 1, y + inset + 1, x + cell - inset - 1,
                            y + cell - inset - 1, 7, 0x08FFFFFF);
                    ArcadeTheme.disc(context, x + cell / 2, y + cell / 2, Math.max(2, cell / 12), 0xFF2B4056);
                }
                if (hovered && !solved) {
                    ArcadeTheme.ring(context, x + cell / 2, y + cell / 2, cell / 2 - 3, 1, 0x66FFFFFF);
                }
            }
        }

        if (solved) {
            int panelWidth = Math.min(width - 40, 260);
            int panelHeight = 46;
            int cx = width / 2;
            int cy = top + SIDE * cell / 2;
            ArcadeTheme.rounded(context, cx - panelWidth / 2, cy - panelHeight / 2, cx + panelWidth / 2,
                    cy + panelHeight / 2, 12, 0xEE0A1422);
            ArcadeTheme.rounded(context, cx - panelWidth / 2, cy - panelHeight / 2, cx + panelWidth / 2,
                    cy - panelHeight / 2 + 2, 12, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("TODAS AS LUZES APAGADAS"),
                    cx, cy - 11, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("em " + moves + " cliques"), cx, cy + 3,
                    ArcadeTheme.TEXT);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || isSolved() || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * cell || mouseY >= top + SIDE * cell) {
            return false;
        }
        int column = (int) ((mouseX - left) / cell);
        int row = (int) ((mouseY - top) / cell);
        toggle(row, column);
        moves++;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
