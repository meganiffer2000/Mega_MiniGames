package br.com.megaminigames.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Base dos minigames: nasce de {@link Screen} e concentra os botões de rodapé,
 * para que cada jogo novo mantenha o mesmo padrão visual e de navegação.
 */
abstract class ArcadeScreen extends Screen {
    protected ArcadeScreen(String title) {
        super(Text.literal(title));
    }

    /** Adiciona os botões "Recomeçar" e "Voltar ao menu" centralizados em {@code y}. */
    protected void addFooterButtons(int y, Runnable restart) {
        int buttonWidth = Math.min(112, Math.max(74, (width - 40) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, y, buttonWidth, 20,
                Text.literal("Recomeçar"), restart));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, y, buttonWidth, 20,
                Text.literal("Voltar ao menu"), this::backToHub));
    }

    protected void backToHub() {
        client.setScreen(new GamesHubScreen());
    }
}
