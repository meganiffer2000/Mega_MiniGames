package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Pescaria de reflexo: lance a linha e recolha quando o peixe morder. */
public final class FishingScreen extends Screen {
    private static final int READY = 0;
    private static final int WAITING = 1;
    private static final int BITE = 2;
    private static final int RESULT = 3;
    private final List<Particle> particles = new ArrayList<>();
    private int state = READY;
    private int waitTicks;
    private int biteTicks;
    private int biteTotal = 30;
    private int fishCaught;
    private int ticks;
    private String message = "Lance a linha e espere a mordida";

    public FishingScreen() {
        super(Text.literal("Pescaria"));
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(112, Math.max(78, (width - 46) / 2));
        addDrawableChild(ArcadeTheme.button(width / 2 - buttonWidth - 4, height - 30, buttonWidth, 20,
                Text.literal("Lançar linha"), this::cast));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 30, buttonWidth, 20,
                Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
    }

    private void cast() {
        if (state == WAITING || state == BITE) {
            return;
        }
        state = WAITING;
        waitTicks = ThreadLocalRandom.current().nextInt(50, 121);
        message = "Linha na água... aguarde a mordida";
    }

    @Override
    public void tick() {
        ticks++;
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.16;
            particle.life--;
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
        if (state == WAITING && --waitTicks <= 0) {
            state = BITE;
            biteTicks = 30;
            biteTotal = 30;
            message = "MORDEU! Clique ou pressione ESPAÇO agora!";
            splashAt(width * 0.56, height * 0.5);
        } else if (state == BITE && --biteTicks <= 0) {
            state = RESULT;
            message = "O peixe escapou! Lance novamente.";
        }
    }

    private void splashAt(double x, double y) {
        for (int index = 0; index < 12; index++) {
            double angle = Math.random() * Math.PI - Math.PI;
            double speed = 0.8 + Math.random() * 1.8;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    12 + (int) (Math.random() * 10),
                    index % 2 == 0 ? 0xFFFFFFFF : ArcadeTheme.SKY));
        }
    }

    private void reelIn() {
        if (state == BITE) {
            fishCaught++;
            state = RESULT;
            message = "Você pescou um peixe! Total: " + fishCaught;
            splashAt(width * 0.56, height * 0.5);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        int horizon = Math.max(96, (int) (height * 0.42));
        int waterBottom = height - 48;
        context.fillGradient(0, 0, width, horizon, 0xFF1E4F77, 0xFF6FB6D9);
        context.fillGradient(0, horizon, width, waterBottom, 0xFF1E6389, 0xFF0C3550);

        ArcadeTheme.glow(context, width - 60, 42, 26, 0x44FFE3A0);
        ArcadeTheme.disc(context, width - 60, 42, 15, 0xFFFFEDB8);
        for (int index = 0; index < 4; index++) {
            int x = (width / 5) * (index + 1) - 40;
            int y = 28 + (index * 27) % Math.max(24, horizon / 3);
            ArcadeTheme.rounded(context, x, y, x + 46, y + 12, 6, 0x55FFFFFF);
            ArcadeTheme.rounded(context, x + 10, y - 6, x + 34, y + 11, 7, 0x44FFFFFF);
        }
        for (int index = 0; index < 5; index++) {
            int x = index * (width / 4) - 30;
            ArcadeTheme.rounded(context, x, horizon - 26, x + 110, horizon + 4, 14, 0xFF2C6B57);
        }
        context.fillGradient(0, horizon, width, horizon + 10, 0x88B8F0FF, 0x00000000);
        context.fill(0, horizon, width, horizon + 1, 0x99D7F6FF);

        for (int index = 0; index < 7; index++) {
            int y = horizon + 14 + index * Math.max(12, (waterBottom - horizon) / 8);
            int offset = (int) (Math.sin((ticks + index * 14) * 0.04) * 10);
            context.fill(30 + offset + index * 9, y, width - 30 + offset - index * 7, y + 1, 0x33B8F0FF);
        }

        context.fill(0, waterBottom, width, height, 0xFF0A2437);
        context.fill(0, waterBottom, width, waterBottom + 2, 0xFF2E7A9B);
        int boatX = width / 2 - 46;
        ArcadeTheme.rounded(context, boatX, waterBottom - 12, boatX + 92, waterBottom + 4, 8, 0xFF7B4B2A);
        ArcadeTheme.rounded(context, boatX + 2, waterBottom - 10, boatX + 90, waterBottom - 6, 4, 0xFFA9713F);
        context.fill(boatX + 20, waterBottom - 6, boatX + 72, waterBottom - 4, 0xFF5A3520);

        int bobberX = (int) (width * 0.56);
        int bobberY = horizon + 22 + (state == BITE ? (ticks % 4 < 2 ? 2 : -1) : (int) (Math.sin(ticks * 0.08) * 2));
        int rodTipX = width / 2 + 78;
        int rodTipY = waterBottom - 6;
        drawLine(context, rodTipX, rodTipY, bobberX, bobberY, 0xFFD9E9F2);
        ArcadeTheme.disc(context, bobberX, bobberY, 4, 0xFFE33B3B);
        ArcadeTheme.disc(context, bobberX, bobberY - 1, 2, 0xFFFFDADA);
        ArcadeTheme.ring(context, bobberX, bobberY, 8, 1, 0x66FFFFFF);

        if (state == BITE) {
            ArcadeTheme.glow(context, bobberX, bobberY, 22, 0x66FFD66B);
            int shimmer = -10 + (int) (Math.sin(ticks * 0.5) * 4);
            ArcadeTheme.rounded(context, bobberX + shimmer, bobberY + 14, bobberX + shimmer + 28,
                    bobberY + 22, 4, 0xFFFFD66B);
            ArcadeTheme.rounded(context, bobberX + shimmer + 22, bobberY + 12, bobberX + shimmer + 36,
                    bobberY + 18, 3, 0xFFF0A93C);
        } else if (state == WAITING) {
            int shadow = (int) (Math.sin(ticks * 0.05) * 8);
            context.fill(bobberX - 16 + shadow, bobberY + 20, bobberX + 8 + shadow, bobberY + 27, 0x55204A66);
        }
        for (Particle particle : particles) {
            ArcadeTheme.disc(context, (int) particle.x, (int) particle.y, 2,
                    ArcadeTheme.alpha(particle.color, Math.min(230, particle.life * 16)));
        }

        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, ArcadeTheme.SKY);
        ArcadeTheme.pill(context, textRenderer, "PEIXES  " + fishCaught, 12, 12, ArcadeTheme.GOLD);
        ArcadeTheme.pillRight(context, textRenderer, stateName(), width - 12, 12,
                state == BITE ? ArcadeTheme.GOLD : ArcadeTheme.TEAL);

        int panelWidth = Math.min(width - 24, 300);
        int panelTop = waterBottom - 76;
        ArcadeTheme.rounded(context, width / 2 - panelWidth / 2, panelTop, width / 2 + panelWidth / 2,
                panelTop + 38, 10, state == BITE ? 0xF03B2E12 : 0xE80A1B2A);
        if (state == BITE) {
            ArcadeTheme.rounded(context, width / 2 - panelWidth / 2, panelTop, width / 2 + panelWidth / 2,
                    panelTop + 38, 10, 0x66FFD66B);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("MORDEU!"),
                    width / 2, panelTop + 8, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("CLIQUE / ESPAÇO para recolher"),
                    width / 2, panelTop + 22, ArcadeTheme.TEXT);
            ArcadeTheme.progress(context, width / 2 - panelWidth / 2 + 12, panelTop + 34,
                    panelWidth - 24, 3, biteTicks / (float) Math.max(1, biteTotal), ArcadeTheme.GOLD);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(message),
                    width / 2, panelTop + 10, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(state == READY ? "ENTER ou o botão Lançar" : "Aguarde a linha afundar..."),
                    width / 2, panelTop + 23, ArcadeTheme.MUTED);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private String stateName() {
        return switch (state) {
            case READY -> "PRONTO";
            case WAITING -> "AGUARDANDO";
            case BITE -> "FISGOU!";
            default -> "RESULTADO";
        };
    }

    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int step = 0; step <= steps; step += 2) {
            int x = x1 + (x2 - x1) * step / Math.max(1, steps);
            int y = y1 + (y2 - y1) * step / Math.max(1, steps)
                    + (int) (Math.sin(step * 0.4) * 2 * (1 - step / (float) Math.max(1, steps)));
            context.fill(x, y, x + 1, y + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && state == BITE) {
            reelIn();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (state == BITE) {
                reelIn();
            } else {
                cast();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER && (state == READY || state == RESULT)) {
            cast();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Particle {
        private double x;
        private double y;
        private final double vx;
        private double vy;
        private int life;
        private final int color;

        private Particle(double x, double y, double vx, double vy, int life, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.color = color;
        }
    }
}
