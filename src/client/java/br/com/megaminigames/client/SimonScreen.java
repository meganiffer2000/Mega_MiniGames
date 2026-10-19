package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Simon: observe a sequência de cores e repita-a na ordem correta. */
public final class SimonScreen extends Screen {
    private static final int PAD_SIZE = 118;
    private static final int PAD_GAP = 8;
    private static final int SHOWING = 0;
    private static final int INPUT = 1;
    private static final int LOST = 2;
    private static final int[] PAD_COLORS = {
            0xFF41B85A, 0xFFE64949, 0xFFE6C33D, 0xFF4386E8
    };
    private static final String[] PAD_NAMES = {"1 • VERDE", "2 • VERMELHO", "3 • AMARELO", "4 • AZUL"};
    private final List<Integer> sequence = new ArrayList<>();

    private int boardLeft;
    private int boardTop;
    private int boardSize;
    private int mode;
    private int showPosition;
    private int phaseTicks;
    private int inputPosition;

    public SimonScreen() {
        super(Text.literal("Simon"));
    }

    @Override
    protected void init() {
        boardSize = Math.min(PAD_SIZE * 2 + PAD_GAP, Math.min(width - 40, height - 122));
        boardSize = Math.max(120, boardSize);
        boardLeft = (width - boardSize) / 2;
        boardTop = Math.max(56, (height - boardSize - 70) / 2);
        int buttonY = Math.min(height - 24, boardTop + boardSize + 18);
        addDrawableChild(ButtonWidget.builder(Text.literal("Reiniciar (R)"), button -> newGame())
                .dimensions(width / 2 - 116, buttonY, 112, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar ao menu"), button ->
                        client.setScreen(new GamesHubScreen()))
                .dimensions(width / 2 + 4, buttonY, 112, 20).build());
        if (sequence.isEmpty()) {
            newGame();
        }
    }

    private void newGame() {
        sequence.clear();
        inputPosition = 0;
        nextRound();
    }

    private void nextRound() {
        sequence.add(ThreadLocalRandom.current().nextInt(4));
        showPosition = 0;
        phaseTicks = 0;
        inputPosition = 0;
        mode = SHOWING;
    }

    @Override
    public void tick() {
        if (mode != SHOWING) {
            return;
        }
        phaseTicks++;
        if (phaseTicks >= 17) {
            phaseTicks = 0;
            showPosition++;
            if (showPosition >= sequence.size()) {
                mode = INPUT;
            }
        }
    }

    private int litPad() {
        return mode == SHOWING && showPosition < sequence.size() && phaseTicks < 10
                ? sequence.get(showPosition) : -1;
    }

    private void choose(int pad) {
        if (mode != INPUT) {
            return;
        }
        if (sequence.get(inputPosition) != pad) {
            mode = LOST;
            return;
        }
        inputPosition++;
        if (inputPosition == sequence.size()) {
            nextRound();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        String instructions = switch (mode) {
            case SHOWING -> "Observe a sequência...";
            case INPUT -> "Sua vez! Clique nas cores ou pressione 1, 2, 3 e 4";
            default -> "Sequência errada! Pressione R para tentar novamente";
        };
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Rodada " + sequence.size() + "  |  " + instructions),
                width / 2, 31, mode == LOST ? 0xFFFF7777 : 0xFFFFFFFF);

        int gap = Math.max(4, boardSize * PAD_GAP / (PAD_SIZE * 2 + PAD_GAP));
        int cellSize = (boardSize - gap) / 2;
        int lit = litPad();
        for (int pad = 0; pad < 4; pad++) {
            int row = pad / 2;
            int column = pad % 2;
            int x = boardLeft + column * (cellSize + gap);
            int y = boardTop + row * (cellSize + gap);
            int color = pad == lit ? brighten(PAD_COLORS[pad]) : PAD_COLORS[pad];
            context.fill(x, y, x + cellSize, y + cellSize, color);
            context.fill(x + 4, y + 4, x + cellSize - 4, y + cellSize - 4, color);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(PAD_NAMES[pad]),
                    x + cellSize / 2, y + cellSize / 2 - 4, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private int brighten(int color) {
        int red = Math.min(255, ((color >> 16) & 0xFF) + 55);
        int green = Math.min(255, ((color >> 8) & 0xFF) + 55);
        int blue = Math.min(255, (color & 0xFF) + 55);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0 || mode != INPUT) {
            return false;
        }
        int gap = Math.max(4, boardSize * PAD_GAP / (PAD_SIZE * 2 + PAD_GAP));
        int cellSize = (boardSize - gap) / 2;
        for (int pad = 0; pad < 4; pad++) {
            int row = pad / 2;
            int column = pad % 2;
            int x = boardLeft + column * (cellSize + gap);
            int y = boardTop + row * (cellSize + gap);
            if (mouseX >= x && mouseX < x + cellSize && mouseY >= y && mouseY < y + cellSize) {
                choose(pad);
                return true;
            }
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
            choose(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
