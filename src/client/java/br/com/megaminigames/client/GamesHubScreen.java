package br.com.megaminigames.client;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Hub paginado para navegar por todos os minijogos. */
public final class GamesHubScreen extends Screen {
    private static final int BUTTON_WIDTH = 184;
    private static final int BUTTON_HEIGHT = 24;
    private static final int PAGE_SIZE = 6;
    private static final List<Game> GAMES = List.of(
            new Game("Flappy Bird", FlappyBirdScreen::new),
            new Game("Campo Minado", MinesweeperScreen::new),
            new Game("Snake", SnakeScreen::new),
            new Game("Pong", PongScreen::new),
            new Game("Jogo da Memória", MemoryScreen::new),
            new Game("Simon", SimonScreen::new),
            new Game("Tetris", TetrisScreen::new),
            new Game("Quebra-Blocos", BreakoutScreen::new),
            new Game("2048", Game2048Screen::new),
            new Game("Jogo da Velha", TicTacToeScreen::new),
            new Game("Ligue 4", ConnectFourScreen::new),
            new Game("Luzes", LightsOutScreen::new),
            new Game("Acerte a Toupeira", WhackAMoleScreen::new),
            new Game("Adivinhe o Número", NumberGuessScreen::new),
            new Game("Desvie!", DodgeScreen::new)
    );

    private final int page;

    public GamesHubScreen() {
        this(0);
    }

    private GamesHubScreen(int page) {
        super(Text.literal("Mega MiniGames"));
        this.page = page;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(BUTTON_WIDTH, (width - 48) / 2);
        int left = (width - buttonWidth * 2 - 12) / 2;
        int top = height / 2 - 70;
        int rowStep = BUTTON_HEIGHT + 8;
        int firstIndex = page * PAGE_SIZE;
        int endIndex = Math.min(firstIndex + PAGE_SIZE, GAMES.size());

        for (int index = firstIndex; index < endIndex; index++) {
            Game game = GAMES.get(index);
            int localIndex = index - firstIndex;
            int x = left + (localIndex % 2) * (BUTTON_WIDTH + 12);
            int y = top + (localIndex / 2) * rowStep;
            addDrawableChild(ButtonWidget.builder(Text.literal(game.name), button ->
                            client.setScreen(game.screenFactory.get()))
                    .dimensions(x, y, buttonWidth, BUTTON_HEIGHT)
                    .build());
        }

        int controlsY = top + rowStep * 3 + 3;
        if (page > 0) {
            addDrawableChild(ButtonWidget.builder(Text.literal("‹ Anterior"), button ->
                            client.setScreen(new GamesHubScreen(page - 1)))
                    .dimensions(width / 2 - 120, controlsY, 112, 20)
                    .build());
        }
        if (page < pageCount() - 1) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Próxima ›"), button ->
                            client.setScreen(new GamesHubScreen(page + 1)))
                    .dimensions(width / 2 + 8, controlsY, 112, 20)
                    .build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), button -> client.setScreen(null))
                .dimensions((width - 120) / 2, controlsY + 27, 120, 20)
                .build());
    }

    private static int pageCount() {
        return (GAMES.size() + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 112, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Página " + (page + 1) + "/" + pageCount() + " • " + GAMES.size() + " minijogos"),
                width / 2, height / 2 - 92, 0xFFD8E8FF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Game(String name, Supplier<Screen> screenFactory) { }
}
