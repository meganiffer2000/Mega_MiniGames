package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Acerte a toupeira antes que o tempo acabe. */
public final class WhackAMoleScreen extends Screen {
    private static final int SIDE = 3;
    private static final int CELL = 82;
    private int left;
    private int top;
    private int score;
    private int ticksLeft;
    private int moleTicks;
    private int moleCell = -1;
    private boolean gameOver;
    private boolean initialized;

    public WhackAMoleScreen() {
        super(Text.literal("Acerte a Toupeira"));
    }

    @Override
    protected void init() {
        left = (width - SIDE * CELL) / 2;
        top = Math.max(60, (height - SIDE * CELL - 68) / 2);
        int buttonY = Math.min(height - 24, top + SIDE * CELL + 14);
        addDrawableChild(ButtonWidget.builder(Text.literal("Jogar novamente"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (!initialized) newGame();
    }

    private void newGame() {
        score = 0;
        ticksLeft = 600;
        moleTicks = 1;
        moleCell = -1;
        gameOver = false;
        initialized = true;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticksLeft--;
        moleTicks--;
        if (moleTicks <= 0) {
            moleCell = ThreadLocalRandom.current().nextInt(SIDE * SIDE);
            moleTicks = Math.max(9, 24 - score / 3);
        }
        if (ticksLeft <= 0) {
            gameOver = true;
            moleCell = -1;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Pontos: " + score + "  •  Tempo: " + (ticksLeft / 20) + "s  •  Clique na toupeira!"),
                width / 2, 35, 0xFFE8E8E8);
        for (int index = 0; index < SIDE * SIDE; index++) {
            int row = index / SIDE;
            int column = index % SIDE;
            int x = left + column * CELL;
            int y = top + row * CELL;
            context.fill(x + 4, y + 4, x + CELL - 4, y + CELL - 4, 0xFF69472F);
            context.fill(x + 10, y + 10, x + CELL - 10, y + CELL - 10, 0xFF49301F);
            if (index == moleCell && !gameOver) {
                drawMole(context, x + CELL / 2, y + CELL / 2 + 3);
            }
        }
        if (gameOver) {
            context.fill(left, top + CELL, left + SIDE * CELL, top + CELL * 2, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Tempo esgotado! Sua pontuação: " + score), width / 2,
                    top + CELL + CELL / 2 - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawMole(DrawContext context, int centerX, int centerY) {
        drawDisc(context, centerX, centerY, 22, 0xFF9A674A);
        context.fill(centerX - 11, centerY - 9, centerX - 6, centerY - 4, 0xFF17120F);
        context.fill(centerX + 6, centerY - 9, centerX + 11, centerY - 4, 0xFF17120F);
        context.fill(centerX - 4, centerY + 2, centerX + 5, centerY + 6, 0xFFFFA9A2);
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
        if (button != 0 || gameOver || mouseX < left || mouseY < top
                || mouseX >= left + SIDE * CELL || mouseY >= top + SIDE * CELL) {
            return false;
        }
        int column = (int) ((mouseX - left) / CELL);
        int row = (int) ((mouseY - top) / CELL);
        int clickedCell = row * SIDE + column;
        if (clickedCell == moleCell) {
            score++;
            moleCell = -1;
            moleTicks = 0;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
