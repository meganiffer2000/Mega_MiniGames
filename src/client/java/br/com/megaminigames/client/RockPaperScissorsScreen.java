package br.com.megaminigames.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Pedra, papel e tesoura em uma série de melhor de cinco. */
public final class RockPaperScissorsScreen extends Screen {
    private static final String[] CHOICES = {"Pedra", "Papel", "Tesoura"};
    private int playerScore;
    private int computerScore;
    private int playerChoice = -1;
    private int computerChoice = -1;
    private String roundMessage = "Escolha sua jogada";
    private boolean finished;

    public RockPaperScissorsScreen() {
        super(Text.literal("Pedra, Papel e Tesoura"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(104, Math.max(72, (width - 56) / 3));
        int start = (width - buttonWidth * 3 - 16) / 2;
        int buttonY = Math.min(height - 62, Math.max(height / 2 + 62, height - 62));
        for (int choice = 0; choice < CHOICES.length; choice++) {
            final int selected = choice;
            addDrawableChild(ArcadeTheme.button(start + choice * (buttonWidth + 8), buttonY, buttonWidth, 22,
                    Text.literal(CHOICES[choice]), () -> play(selected)));
        }
        int footerWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - footerWidth - 4, height - 30, footerWidth, 20,
                Text.literal("Nova série"), this::newGame));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 30, footerWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
    }

    private void newGame() {
        playerScore = 0;
        computerScore = 0;
        playerChoice = -1;
        computerChoice = -1;
        roundMessage = "Escolha sua jogada";
        finished = false;
    }

    private void play(int choice) {
        if (finished) {
            return;
        }
        playerChoice = choice;
        computerChoice = ThreadLocalRandom.current().nextInt(3);
        int difference = (playerChoice - computerChoice + 3) % 3;
        if (difference == 1) {
            playerScore++;
            roundMessage = "Você venceu esta rodada!";
        } else if (difference == 2) {
            computerScore++;
            roundMessage = "O computador venceu esta rodada.";
        } else {
            roundMessage = "Empate nesta rodada.";
        }
        if (playerScore == 3 || computerScore == 3) {
            finished = true;
            roundMessage = playerScore == 3 ? "Você venceu a série!" : "O computador venceu a série.";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.MAGENTA);
        ArcadeTheme.status(context, textRenderer, Text.literal(roundMessage), width / 2, 32,
                finished ? (playerScore == 3 ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER) : ArcadeTheme.MUTED);

        int cardWidth = Math.min(120, (width - 70) / 2);
        int cardHeight = Math.min(118, height - 240);
        int gap = 18;
        int left = width / 2 - cardWidth - gap / 2;
        int right = width / 2 + gap / 2;
        int top = Math.max(58, (height - cardHeight - 150) / 2 + 24);
        drawCard(context, left, top, cardWidth, cardHeight, "VOCÊ", playerChoice, ArcadeTheme.SKY);
        drawCard(context, right, top, cardWidth, cardHeight, "CPU", computerChoice, ArcadeTheme.CORAL);

        int scoreY = top + cardHeight + 12;
        drawScore(context, left, scoreY, cardWidth, playerScore, ArcadeTheme.SKY, true);
        drawScore(context, right, scoreY, cardWidth, computerScore, ArcadeTheme.CORAL, false);

        int versusY = top + cardHeight / 2 - 4;
        ArcadeTheme.rounded(context, width / 2 - 12, versusY - 8, width / 2 + 12, versusY + 10, 6, 0xFF1B2B42);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("VS"), width / 2, versusY - 2,
                ArcadeTheme.GOLD);

        String lastRound = playerChoice < 0 ? "Faça sua escolha abaixo"
                : "Você: " + CHOICES[playerChoice] + "   •   CPU: " + CHOICES[computerChoice];
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(lastRound), width / 2,
                top + cardHeight + 40, ArcadeTheme.TEXT);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCard(DrawContext context, int left, int top, int cardWidth, int cardHeight, String label,
                          int choice, int accent) {
        ArcadeTheme.rounded(context, left, top, left + cardWidth, top + cardHeight, 11,
                ArcadeTheme.alpha(accent, 100));
        ArcadeTheme.rounded(context, left + 1, top + 1, left + cardWidth - 1, top + cardHeight - 1, 10, 0xFF0F2034);
        context.fillGradient(left + 2, top + 2, left + cardWidth - 2, top + 34, ArcadeTheme.alpha(accent, 30),
                0x00000000);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), left + cardWidth / 2, top + 8, accent);
        int iconSize = Math.min(cardWidth - 28, cardHeight - 52);
        int centerX = left + cardWidth / 2;
        int centerY = top + 30 + iconSize / 2;
        if (choice < 0) {
            ArcadeTheme.ring(context, centerX, centerY, iconSize / 2, 1, 0x33FFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("?"), centerX, centerY - 4,
                    ArcadeTheme.DIM);
        } else {
            drawHand(context, centerX, centerY, iconSize, choice);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(CHOICES[choice]), centerX,
                    top + cardHeight - 17, ArcadeTheme.TEXT);
        }
    }

    private void drawScore(DrawContext context, int left, int y, int cardWidth, int score, int accent,
                           boolean leftAligned) {
        for (int index = 0; index < 3; index++) {
            int x = left + 10 + index * 12;
            int color = index < score ? accent : 0xFF25374D;
            ArcadeTheme.disc(context, x, y + 4, 4, color);
            if (index < score) {
                ArcadeTheme.disc(context, x - 1, y + 3, 1, 0x66FFFFFF);
            }
        }
    }

    private void drawHand(DrawContext context, int centerX, int centerY, int size, int choice) {
        int radius = Math.max(9, size / 3);
        if (choice == 0) {
            ArcadeTheme.disc(context, centerX, centerY, radius, 0xFF8A98A8);
            ArcadeTheme.disc(context, centerX - radius / 3, centerY - radius / 3, radius / 2, 0xFFB7C4D1);
            ArcadeTheme.rounded(context, centerX - radius + 2, centerY + radius / 3, centerX + radius - 2,
                    centerY + radius / 3 + 3, 1, 0xFF5D6B7C);
        } else if (choice == 1) {
            ArcadeTheme.rounded(context, centerX - radius, centerY - radius + 3, centerX + radius,
                    centerY + radius, 4, 0xFFE8EDF4);
            ArcadeTheme.rounded(context, centerX - radius + 1, centerY - radius + 4, centerX + radius - 1,
                    centerY + radius - 1, 4, 0xFFF7FAFF);
            for (int line = 0; line < 3; line++) {
                int lineY = centerY - radius / 2 + line * Math.max(3, radius / 3);
                context.fill(centerX - radius + 5, lineY, centerX + radius - 5, lineY + 1, 0xFFB9C6D4);
            }
            context.fill(centerX - radius + 3, centerY - radius + 5, centerX + radius - 3, centerY - radius + 7,
                    0xAAFFFFFF);
        } else {
            ArcadeTheme.rounded(context, centerX - radius, centerY - radius / 2 - 2, centerX + radius,
                    centerY + radius / 2 + 2, 3, 0xFFB9C6D4);
            ArcadeTheme.rounded(context, centerX - radius + 1, centerY - radius / 2 - 1, centerX + radius - 1,
                    centerY + radius / 2 + 1, 3, 0xFFEFF4FA);
            ArcadeTheme.ring(context, centerX - radius / 2, centerY - radius / 2, Math.max(4, radius / 2), 2,
                    0xFF7A8A9C);
            ArcadeTheme.ring(context, centerX + radius / 2, centerY - radius / 2, Math.max(4, radius / 2), 2,
                    0xFF7A8A9C);
            ArcadeTheme.ring(context, centerX - radius / 2, centerY + radius / 2, Math.max(4, radius / 2), 2,
                    0xFF7A8A9C);
            ArcadeTheme.ring(context, centerX + radius / 2, centerY + radius / 2, Math.max(4, radius / 2), 2,
                    0xFF7A8A9C);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
