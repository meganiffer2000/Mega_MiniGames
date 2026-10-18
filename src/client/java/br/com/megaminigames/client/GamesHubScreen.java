package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Menu principal com a lista de minijogos disponíveis. */
public final class GamesHubScreen extends Screen {
    public GamesHubScreen() {
        super(Text.literal("Mega MiniGames"));
    }

    @Override
    protected void init() {
        int buttonWidth = 220;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 28;

        addDrawableChild(ButtonWidget.builder(Text.literal("Flappy Bird"), button ->
                        client.setScreen(new FlappyBirdScreen()))
                .dimensions(x, y, buttonWidth, 24)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Campo Minado"), button ->
                        client.setScreen(new MinesweeperScreen()))
                .dimensions(x, y + 32, buttonWidth, 24)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), button -> client.setScreen(null))
                .dimensions(x, y + 66, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 70, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Escolha um minijogo para jogar"), width / 2, height / 2 - 42, 0xD8E8FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
