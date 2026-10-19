package br.com.megaminigames.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Linguagem visual compartilhada dos minigames: fundo neon, painéis de vidro,
 * pílulas de HUD, botões e ícones desenhados com {@code fill}.
 *
 * <p>Tudo aqui é desenhado a partir de retângulos preenchidos, então funciona em
 * qualquer versão 1.20.x sem depender de texturas adicionais.</p>
 */
public final class ArcadeTheme {
    /* ------------------------------------------------------------------ */
    /* Paleta                                                             */
    /* ------------------------------------------------------------------ */

    /** Cor de texto principal (branco levemente azulado). */
    public static final int TEXT = 0xFFF2F7FF;
    /** Texto secundário, usado em instruções. */
    public static final int MUTED = 0xFF9DB2CC;
    /** Texto de rodapé/dicas discretas. */
    public static final int DIM = 0xFF6A80A0;

    public static final int TEAL = 0xFF35C8B9;
    public static final int TEAL_DEEP = 0xFF1B7D78;
    public static final int LIME = 0xFFB9F26B;
    public static final int GOLD = 0xFFFFD66B;
    public static final int MAGENTA = 0xFFF0509B;
    public static final int SKY = 0xFF6CC9F5;
    public static final int PURPLE = 0xFF9B7BF0;
    public static final int CORAL = 0xFFFF8A65;

    public static final int SUCCESS = 0xFF6EE58C;
    public static final int DANGER = 0xFFFF6E6E;

    /** Fundo mais escuro usado dentro de painéis. */
    public static final int PANEL_DEEP = 0xFF0B1626;
    /** Fundo padrão de um painel de vidro. */
    public static final int PANEL = 0xF0132438;
    /** Fundo de cartões e células. */
    public static final int PANEL_LIGHT = 0xFF1B2E46;
    /** Linhas e bordas suaves. */
    public static final int BORDER = 0x33FFFFFF;
    public static final int BORDER_SOFT = 0x1AFFFFFF;

    private static final int[] ACCENTS = {TEAL, LIME, GOLD, MAGENTA, SKY, PURPLE, CORAL};

    private ArcadeTheme() {
    }

    /* ------------------------------------------------------------------ */
    /* Fundo                                                              */
    /* ------------------------------------------------------------------ */

    public static void background(DrawContext context, int width, int height) {
        background(context, width, height, (System.currentTimeMillis() % 200000L) / 1000.0F);
    }

    public static void background(DrawContext context, int width, int height, float time) {
        context.fill(0, 0, width, height, 0xFF060A13);
        context.fillGradient(0, 0, width, height, 0xFF16233F, 0xFF050810);

        // Brilhos suaves nos cantos.
        context.fillGradient(0, 0, width / 2, height / 2, 0x1F35C8B9, 0x00000000);
        context.fillGradient(width / 2, height / 2, width, height, 0x1FF0509B, 0x00000000);
        context.fillGradient(0, height / 2, width, height, 0x14000000, 0x66000000);
        context.fillGradient(0, 0, width, height / 4, 0x33060A13, 0x00060A13);

        // Grade técnica discreta.
        for (int x = 12; x < width; x += 32) {
            context.fill(x, 0, x + 1, height, 0x08FFFFFF);
        }
        for (int y = 10; y < height; y += 32) {
            context.fill(0, y, width, y + 1, 0x08FFFFFF);
        }

        // Estrelas cintilantes.
        for (int index = 0; index < 44; index++) {
            int hash = index * 73856093 ^ (index + 11) * 19349663;
            int x = Math.floorMod(hash, Math.max(1, width));
            int y = Math.floorMod(hash >> 7, Math.max(1, height));
            float twinkle = (float) (0.5 + 0.5 * Math.sin(time * 1.7F + index * 1.3F));
            int alpha = 0x14 + (int) (twinkle * 0x38);
            int size = index % 9 == 0 ? 2 : 1;
            context.fill(x, y, x + size, y + size, (alpha << 24) | 0xBFE7FF);
        }

        // Linhas horizontais lembrando um monitor CRT.
        for (int y = 0; y < height; y += 3) {
            context.fill(0, y, width, y + 1, 0x05000000);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Painéis e cabeçalho                                                */
    /* ------------------------------------------------------------------ */

    public static void panel(DrawContext context, int x1, int y1, int x2, int y2) {
        panelAccent(context, x1, y1, x2, y2, BORDER, 8);
    }

    public static void panelAccent(DrawContext context, int x1, int y1, int x2, int y2, int border, int radius) {
        rounded(context, x1 + 2, y1 + 3, x2 + 2, y2 + 3, radius, 0x66000000);
        rounded(context, x1, y1, x2, y2, radius, border);
        rounded(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), PANEL);
        context.fillGradient(x1 + 1, y1 + 1, x2 - 1, Math.min(y2 - 1, y1 + 22), 0x14FFFFFF, 0x00000000);
    }

    public static void header(DrawContext context, TextRenderer font, Text title, int centerX, int y, int accent) {
        String label = title.getString();
        int textWidth = font.getWidth(label);
        int left = centerX - textWidth / 2 - 16;
        int right = centerX + textWidth / 2 + 16;

        context.fillGradient(left - 6, y - 6, right + 6, y + 20, (accent & 0x00FFFFFF) | 0x33000000,
                0x00000000);
        rounded(context, left, y - 4, right, y + 16, 9, 0xD90B1626);
        rounded(context, left, y - 4, right, y + 15, 9, (accent & 0x00FFFFFF) | 0x66000000);
        rounded(context, left + 1, y - 3, right - 1, y + 14, 8, 0xF0101F33);
        context.fillGradient(left + 2, y - 2, right - 2, y + 6, 0x1AFFFFFF, 0x00000000);
        context.fill(left + 5, y + 3, left + 8, y + 9, accent);
        context.fill(right - 8, y + 3, right - 5, y + 9, accent);
        context.drawCenteredTextWithShadow(font, title, centerX, y, TEXT);
        context.fillGradient(centerX - textWidth / 2, y + 15, centerX + textWidth / 2, y + 17, accent,
                0x00000000);
    }

    /** Painel central usado para vitória, derrota e fim de jogo. */
    public static void banner(DrawContext context, TextRenderer font, String title, String subtitle,
                              int centerX, int centerY, int maxWidth, int accent) {
        int width = Math.min(maxWidth, Math.max(font.getWidth(title), font.getWidth(subtitle)) + 44);
        width = Math.max(width, 120);
        int height = subtitle == null || subtitle.isEmpty() ? 38 : 52;
        int left = centerX - width / 2;
        int right = centerX + width / 2;
        int top = centerY - height / 2;
        int bottom = centerY + height / 2;
        glow(context, centerX, centerY, Math.max(30, width / 2), alpha(accent, 90));
        rounded(context, left + 2, top + 3, right + 2, bottom + 3, 12, 0x66000000);
        rounded(context, left, top, right, bottom, 12, accent);
        rounded(context, left + 1, top + 1, right - 1, bottom - 1, 11, 0xF00A1422);
        context.fillGradient(left + 2, top + 2, right - 2, top + 12, alpha(accent, 70), 0x00000000);
        context.fill(left + 14, top + 1, right - 14, top + 3, accent);
        context.drawCenteredTextWithShadow(font, Text.literal(title), centerX,
                subtitle == null || subtitle.isEmpty() ? top + 15 : top + 12, TEXT);
        if (subtitle != null && !subtitle.isEmpty()) {
            context.drawCenteredTextWithShadow(font, Text.literal(subtitle), centerX, top + 28, MUTED);
        }
    }

    public static void status(DrawContext context, TextRenderer font, Text text, int centerX, int y, int color) {
        context.drawCenteredTextWithShadow(font, text, centerX, y, color);
    }

    /* ------------------------------------------------------------------ */
    /* HUD                                                                */
    /* ------------------------------------------------------------------ */

    /** Pílula de HUD alinhada à esquerda; devolve a largura usada. */
    public static int pill(DrawContext context, TextRenderer font, String label, int x, int y, int accent) {
        int width = font.getWidth(label) + 18;
        rounded(context, x, y, x + width, y + 15, 7, 0xD90B1626);
        rounded(context, x, y, x + width, y + 15, 7, (accent & 0x00FFFFFF) | 0x55000000);
        rounded(context, x + 1, y + 1, x + width - 1, y + 14, 6, 0xE8122236);
        context.fill(x + 5, y + 5, x + 8, y + 10, accent);
        context.drawTextWithShadow(font, Text.literal(label), x + 12, y + 4, TEXT);
        return width;
    }

    /** Pílula de HUD alinhada à direita; devolve a largura usada. */
    public static int pillRight(DrawContext context, TextRenderer font, String label, int right, int y, int accent) {
        int width = font.getWidth(label) + 18;
        pill(context, font, label, right - width, y, accent);
        return width;
    }

    public static void progress(DrawContext context, int x, int y, int width, int height, float value, int accent) {
        rounded(context, x, y, x + width, y + height, height / 2, 0xCC0A1422);
        int filled = Math.round(width * Math.max(0.0F, Math.min(1.0F, value)));
        if (filled > 0) {
            rounded(context, x, y, x + Math.max(height, filled), y + height, height / 2, accent);
            context.fill(x + 1, y + 1, x + Math.max(1, filled - 1), y + Math.max(1, height / 2),
                    (accent & 0x00FFFFFF) | 0x44000000);
        }
        rounded(context, x, y, x + width, y + height, height / 2, BORDER_SOFT);
    }

    /** Barra dupla usada como enfeite de rodapé. */
    public static void divider(DrawContext context, int x1, int x2, int y) {
        context.fillGradient(x1, y, x2, y + 1, 0x2235C8B9, 0x00F0509B);
        context.fill(x1, y + 2, x2, y + 3, BORDER_SOFT);
    }

    /* ------------------------------------------------------------------ */
    /* Botões                                                             */
    /* ------------------------------------------------------------------ */

    /** Cria um botão já estilizado com o tema. */
    public static ButtonWidget button(int x, int y, int width, int height, Text label, Runnable action) {
        return new ArcadeButton(x, y, width, height, label, action);
    }

    /** Botão principal, com destaque na cor de acento do jogo. */
    public static ButtonWidget primaryButton(int x, int y, int width, int height, Text label, int accent,
                                             Runnable action) {
        ArcadeButton button = new ArcadeButton(x, y, width, height, label, action);
        button.accent = accent;
        button.primary = true;
        return button;
    }

    /**
     * Botão desenhado manualmente: retângulo arredondado, brilho superior e
     * variação de cor no hover. Estende {@link ButtonWidget} apenas para herdar
     * o comportamento de clique e navegação.
     */
    private static final class ArcadeButton extends ButtonWidget {
        private int accent = TEAL;
        private boolean primary;

        private ArcadeButton(int x, int y, int width, int height, Text label, Runnable action) {
            super(x, y, width, height, label, button -> action.run(), supplier -> Text.literal(label.getString()));
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.active && isMouseOver(mouseX, mouseY);
            int top = hovered ? mix(PANEL_LIGHT, accent, 0.28F) : PANEL_LIGHT;
            int bottom = hovered ? darken(accent, 70) : 0xFF122134;
            if (this.primary) {
                top = hovered ? lighten(accent, 30) : accent;
                bottom = hovered ? darken(accent, 35) : darken(accent, 65);
            }
            if (!this.active) {
                top = 0xFF17202D;
                bottom = 0xFF101722;
            }
            rounded(context, getX(), getY() + 2, getX() + getWidth(), getY() + getHeight() + 2, 6, 0x55000000);
            rounded(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 6,
                    this.active ? (hovered ? (this.primary ? lighten(accent, 60) : accent) : BORDER) : 0x22FFFFFF);
            rounded(context, getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 5,
                    this.active ? bottom : 0xFF1A2430);
            context.fillGradient(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 2, top,
                    this.active ? bottom : 0xFF1A2430);
            context.fill(getX() + 3, getY() + 2, getX() + getWidth() - 3, getY() + 3,
                    this.active ? 0x33FFFFFF : 0x11FFFFFF);
            if (hovered) {
                context.fill(getX() + 3, getY() + getHeight() - 3, getX() + getWidth() - 3,
                        getY() + getHeight() - 2, 0x88FFFFFF);
            }
            int textColor = !this.active ? 0xFF6C7E93 : this.primary ? 0xFF0A1A1C : TEXT;
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, textColor);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Formas                                                             */
    /* ------------------------------------------------------------------ */

    public static void rounded(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        int r = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        if (r <= 0) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }
        context.fill(x1, y1 + r, x2, y2 - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            context.fill(x1 + inset, y1 + row, x2 - inset, y1 + row + 1, color);
            context.fill(x1 + inset, y2 - row - 1, x2 - inset, y2 - row, color);
        }
    }

    public static void disc(DrawContext context, int centerX, int centerY, int radius, int color) {
        if (radius <= 0) {
            context.fill(centerX, centerY, centerX + 1, centerY + 1, color);
            return;
        }
        for (int row = -radius; row <= radius; row++) {
            int half = (int) Math.sqrt(Math.max(0, radius * radius - row * row));
            context.fill(centerX - half, centerY + row, centerX + half, centerY + row + 1, color);
        }
    }

    public static void ring(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
        if (radius <= 0) {
            return;
        }
        for (int row = -radius; row <= radius; row++) {
            int outer = (int) Math.sqrt(Math.max(0, radius * radius - row * row));
            int innerRadius = Math.max(0, radius - Math.max(1, thickness));
            int inner = (int) Math.sqrt(Math.max(0, innerRadius * innerRadius - row * row));
            if (outer - inner > 0) {
                context.fill(centerX - outer, centerY + row, centerX - inner, centerY + row + 1, color);
                context.fill(centerX + inner + 1, centerY + row, centerX + outer + 1, centerY + row + 1, color);
            }
        }
    }

    /** Halo suave; camadas concêntricas de baixa opacidade. */
    public static void glow(DrawContext context, int centerX, int centerY, int radius, int color) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        int layers = 4;
        for (int layer = layers; layer >= 1; layer--) {
            int r = Math.max(1, radius * layer / layers);
            int alpha = Math.max(2, baseAlpha / (layer * 2));
            disc(context, centerX, centerY, r, (alpha << 24) | rgb);
        }
    }

    public static void drawX(DrawContext context, int centerX, int centerY, int size, int thickness, int color) {
        for (int step = -size; step <= size; step++) {
            for (int offset = 0; offset < Math.max(1, thickness); offset++) {
                context.fill(centerX + step + offset, centerY + step, centerX + step + offset + 1,
                        centerY + step + 1, color);
                context.fill(centerX + step + offset, centerY - step, centerX + step + offset + 1,
                        centerY - step + 1, color);
            }
        }
    }

    public static void drawO(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
        ring(context, centerX, centerY, radius, thickness, color);
    }

    public static void drawCheck(DrawContext context, int x, int y, int color) {
        for (int step = 0; step < 4; step++) {
            context.fill(x + step, y + 2 + step / 2, x + step + 1, y + 4 + step / 2, color);
        }
        for (int step = 0; step < 6; step++) {
            context.fill(x + 3 + step, y + 4 - step / 2, x + 4 + step, y + 5 - step / 2, color);
        }
    }

    /** Estrela de cinco pontas simplificada, desenhada com linhas. */
    public static void star(DrawContext context, int centerX, int centerY, int size, int color) {
        for (int step = -size; step <= size; step++) {
            int vertical = (int) (Math.abs(step) * 0.55F);
            context.fill(centerX + step, centerY - vertical, centerX + step + 1, centerY + size / 2, color);
        }
        context.fill(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 4, color);
        context.fill(centerX - size / 3, centerY - size, centerX + size / 3, centerY - size + 2, color);
    }

    public static void heart(DrawContext context, int centerX, int centerY, int size, int color) {
        int radius = Math.max(2, size / 2);
        disc(context, centerX - radius / 2, centerY - radius / 3, radius / 2, color);
        disc(context, centerX + radius / 2, centerY - radius / 3, radius / 2, color);
        for (int row = 0; row < radius; row++) {
            int half = (int) ((radius - row) * 0.9);
            context.fill(centerX - half, centerY + row, centerX + half, centerY + row + 1, color);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Cores                                                              */
    /* ------------------------------------------------------------------ */

    public static int accentFor(Text text) {
        return accentFor(text == null ? "" : text.getString());
    }

    public static int accentFor(String name) {
        if (name == null || name.isEmpty()) {
            return TEAL;
        }
        return ACCENTS[Math.floorMod(name.hashCode(), ACCENTS.length)];
    }

    public static int mix(int from, int to, float amount) {
        float a = Math.max(0.0F, Math.min(1.0F, amount));
        int r = (int) (((from >> 16) & 0xFF) * (1 - a) + ((to >> 16) & 0xFF) * a);
        int g = (int) (((from >> 8) & 0xFF) * (1 - a) + ((to >> 8) & 0xFF) * a);
        int b = (int) ((from & 0xFF) * (1 - a) + (to & 0xFF) * a);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int color, int amount) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int darken(int color, int amount) {
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int alpha(int color, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
