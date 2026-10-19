package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Acerte a toupeira antes que o tempo acabe. */
public final class WhackAMoleScreen extends Screen {
    private static final int SIDE = 3;
    private static final int MAX_CELL_SIZE = 82;
    private int cellSize;
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
        int widthLimit = (width - 36) / SIDE;
        int heightLimit = (height - 120) / SIDE;
        cellSize = Math.max(30, Math.min(MAX_CELL_SIZE, Math.min(widthLimit, heightLimit)));
        left = (width - SIDE * cellSize) / 2;
        top = 50;
        int buttonY = height - 25;
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
        moleCell = ThreadLocalRandom.current().nextInt(SIDE * SIDE);
        moleTicks = 24;
        gameOver = false;
        initialized = true;
    }

    @Override
    public void tick() {
        if (gameOver) return;
        ticksLeft--;
        if (moleCell < 0) {
            if (--moleTicks <= 0) spawnMole();
        } else if (--moleTicks <= 0) {
            spawnMole();
        }
        if (ticksLeft <= 0) {
            gameOver = true;
            moleCell = -1;
        }
    }

    private void spawnMole() {
        int previous = moleCell;
        do {
            moleCell = ThreadLocalRandom.current().nextInt(SIDE * SIDE);
        } while (moleCell == previous);
        moleTicks = Math.max(12, 28 - score / 3);
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
            int x = left + column * cellSize;
            int y = top + row * cellSize;
            context.fill(x + 3, y + 3, x + cellSize - 3, y + cellSize - 3, 0xFF69472F);
            context.fill(x + 8, y + 8, x + cellSize - 8, y + cellSize - 8, 0xFF49301F);
            if (index == moleCell && !gameOver) {
                drawMole(context, x + cellSize / 2, y + cellSize / 2 + 3);
            }
        }
        if (gameOver) {
            int centerY = top + SIDE * cellSize / 2;
            context.fill(left, centerY - 18, left + SIDE * cellSize, centerY + 18, 0xAA000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Tempo esgotado! Pontos: " + score), width / 2,
                    centerY - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawMole(DrawContext context, int centerX, int centerY) {
        int radius = Math.max(8, Math.min(22, cellSize / 2 - 5));
        drawDisc(context, centerX, centerY, radius, 0xFF9A674A);
        int eye = Math.max(2, radius / 5);
        context.fill(centerX - radius / 2 - eye, centerY - radius / 3 - eye,
                centerX - radius / 2, centerY - radius / 3, 0xFF17120F);
        context.fill(centerX + radius / 2, centerY - radius / 3 - eye,
                centerX + radius / 2 + eye, centerY - radius / 3, 0xFF17120F);
        context.fill(centerX - radius / 4, centerY + radius / 4,
                centerX + radius / 4 + 1, centerY + radius / 4 + eye, 0xFFFFA9A2);
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
                || mouseX >= left + SIDE * cellSize || mouseY >= top + SIDE * cellSize) {
            return false;
        }
        int column = (int) ((mouseX - left) / cellSize);
        int row = (int) ((mouseY - top) / cellSize);
        int clickedCell = row * SIDE + column;
        if (clickedCell == moleCell) {
            score++;
            spawnMole();
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
