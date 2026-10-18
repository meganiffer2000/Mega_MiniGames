package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Menu principal com os minijogos disponíveis. */
public final class GamesHubScreen extends Screen {
    private static final int BUTTON_WIDTH = 184;
    private static final int BUTTON_HEIGHT = 24;
    private static final int COLUMN_GAP = 12;
    private static final int ROW_GAP = 8;

    public GamesHubScreen() {
        super(Text.literal("Mega MiniGames"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(BUTTON_WIDTH, (width - 36) / 2);
        int totalWidth = buttonWidth * 2 + COLUMN_GAP;
        int left = (width - totalWidth) / 2;
        int top = height / 2 - 62;
        int rowStep = BUTTON_HEIGHT + ROW_GAP;

        addGameButton("Flappy Bird", left, top, buttonWidth,
                () -> client.setScreen(new FlappyBirdScreen()));
        addGameButton("Campo Minado", left + buttonWidth + COLUMN_GAP, top, buttonWidth,
                () -> client.setScreen(new MinesweeperScreen()));
        addGameButton("Snake", left, top + rowStep, buttonWidth,
                () -> client.setScreen(new SnakeScreen()));
        addGameButton("Pong", left + buttonWidth + COLUMN_GAP, top + rowStep, buttonWidth,
                () -> client.setScreen(new PongScreen()));
        addGameButton("Jogo da Memória", left, top + rowStep * 2, buttonWidth,
                () -> client.setScreen(new MemoryScreen()));
        addGameButton("Simon", left + buttonWidth + COLUMN_GAP, top + rowStep * 2, buttonWidth,
                () -> client.setScreen(new SimonScreen()));

        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), button -> client.setScreen(null))
                .dimensions((width - 120) / 2, top + rowStep * 3 + 4, 120, 20)
                .build());
    }

    private void addGameButton(String name, int x, int y, int buttonWidth, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name), button -> action.run())
                .dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 104, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Escolha um dos seis minijogos"), width / 2, height / 2 - 84, 0xD8E8FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
