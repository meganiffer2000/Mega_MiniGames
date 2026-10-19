package br.com.megaminigames.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Tema visual compartilhado do Mega MiniGames.
 *
 * <p>Uma única fonte de verdade para cores, fundos animados, painéis, títulos e botões.
 * Assim todos os jogos falam a mesma linguagem visual (neon arcade sobre azul-marinho),
 * inspirada no ícone oficial do mod.</p>
 */
public final class ArcadeTheme {
    public static final int INK = 0xFF060A14;
    public static final int INK_SOFT = 0xFF0A1120;
    public static final int PANEL = 0xF0122033;
    public static final int PANEL_DEEP = 0xF00B1524;
    public static final int PANEL_LIGHT = 0xF0182C45;
    public static final int BORDER = 0x3DFFFFFF;
    public static final int BORDER_SOFT = 0x1FFFFFFF;

    public static final int TEAL = 0xFF35C8B9;
    public static final int TEAL_DEEP = 0xFF1B7D78;
    public static final int LIME = 0xFFB9F26B;
    public static final int GOLD = 0xFFFFD66B;
    public static final int MAGENTA = 0xFFF0509B;
    public static final int SKY = 0xFF6CC9F5;
    public static final int PURPLE = 0xFF9B7BF0;
    public static final int CORAL = 0xFFFF8A65;

    public static final int TEXT = 0xFFF4F9FF;
    public static final int MUTED = 0xFFA2B6CF;
    public static final int DIM = 0xFF7086A0;
    public static final int SUCCESS = 0xFF6FE58E;
    public static final int DANGER = 0xFFFF7373;

    public static final int[] ACCENTS = {TEAL, LIME, GOLD, MAGENTA, SKY, PURPLE, CORAL};

    private ArcadeTheme() {
    }

    /* ------------------------------------------------------------------ */
    /* Fundo                                                              */
    /* ------------------------------------------------------------------ */

    public static void background(DrawContext context, int width, int height) {
        background(context, width, height, (System.currentTimeMillis() % 120000L) / 1000.0F);
    }

    public static void background(DrawContext context, int width, int height, float time) {
        context.fillGradient(0, 0, width, height, 0xFF132542, 0xFF060A13);
        context.fillGradient(0, 0, width, Math.max(1, height * 2 / 3), 0x222FB9AE, 0x00000000);
        context.fillGradient(0, height / 4, width, height, 0x1AF0509B, 0x00000000);

        // Grade tecnológica suave.
        for (int x = 0; x < width; x += 32) {
            context.fill(x, 0, x + 1, height, 0x08A9C7E6);
        }
        for (int y = 0; y < height; y += 32) {
            context.fill(0, y, width, y + 1, 0x08A9C7E6);
        }

        // Estrelas com cintilação.
        for (int i = 0; i < 52; i++) {
            int hash = i * 374761393 + 668265263;
            hash = (hash ^ (hash >> 13)) * 1274126177;
            int x = Math.floorMod(hash, Math.max(1, width));
            int y = Math.floorMod(hash >> 7, Math.max(1, height));
            int wave = (int) ((Math.sin(time * 1.7 + i * 0.9) + 1.0) * 0.5 * 3.0);
            int alpha = 0x1E + wave * 0x14;
            int size = i % 11 == 0 ? 2 : 1;
            context.fill(x, y, x + size, y + size, (alpha << 24) | 0xFFFFFF);
        }

        // Vinheta e linhas de varredura.
        context.fillGradient(0, 0, width, Math.max(1, height / 6), 0x66040A14, 0x00040A14);
        context.fillGradient(0, height - Math.max(1, height / 5), width, height, 0x00040A14, 0x99040A14);
        for (int y = 0; y < height; y += 4) {
            context.fill(0, y, width, y + 1, 0x07000000);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Painéis, cabeçalhos e HUD                                          */
    /* ------------------------------------------------------------------ */

    public static void panel(DrawContext context, int x1, int y1, int x2, int y2) {
        panelAccent(context, x1, y1, x2, y2, BORDER, 10);
    }

    public static void panelAccent(DrawContext context, int x1, int y1, int x2, int y2, int accent, int radius) {
        rounded(context, x1 + 2, y1 + 3, x2 + 2, y2 + 3, radius, 0x55000000);
        rounded(context, x1, y1, x2, y2, radius, accent);
        rounded(context, x1 + 1, y1 + 1, x2 - 1, y2 - 1, Math.max(0, radius - 1), PANEL);
        rounded(context, x1 + 1, y1 + 1, x2 - 1, y1 + Math.max(2, (y2 - y1) / 2), Math.max(0, radius - 1), 0x0DFFFFFF);
        context.fill(x1 + radius, y1 + 1, x2 - radius, y1 + 2, 0x30FFFFFF);
    }

    public static void header(DrawContext context, TextRenderer font, Text title, int centerX, int y, int accent) {
        int textWidth = font.getWidth(title);
        int x1 = centerX - textWidth / 2 - 18;
        int x2 = centerX + textWidth / 2 + 18;
        rounded(context, x1 - 2, y - 2, x2 + 2, y + 17, 11, (accent & 0x00FFFFFF) | 0x29000000);
        rounded(context, x1, y - 4, x2, y + 15, 9, (accent & 0x00FFFFFF) | 0x66000000);
        rounded(context, x1 + 1, y - 3, x2 - 1, y + 14, 8, 0xF00C1828);
        rounded(context, x1 + 2, y - 2, x2 - 2, y + 6, 6, 0x14FFFFFF);
        rounded(context, x1 + 7, y + 1, x1 + 11, y + 10, 2, accent);
        rounded(context, x2 - 11, y + 1, x2 - 7, y + 10, 2, accent);
        context.drawCenteredTextWithShadow(font, title, centerX, y, TEXT);
        context.fillGradient(x1 + 16, y + 14, x2 - 16, y + 16, accent, 0x00000000);
    }

    public static void status(DrawContext context, TextRenderer font, Text text, int centerX, int y, int color) {
        int width = font.getWidth(text) + 16;
        rounded(context, centerX - width / 2, y - 2, centerX + width / 2, y + 11, 6, 0x8C081120);
        context.drawCenteredTextWithShadow(font, text, centerX, y, color);
    }

    /** Pílula de HUD alinhada à esquerda; devolve a largura usada. */
    public static int pill(DrawContext context, TextRenderer font, String label, int x, int y, int accent) {
        int width = font.getWidth(label) + 18;
        rounded(context, x, y, x + width, y + 15, 7, 0xD90B1626);
        rounded(context, x + 1, y + 1, x + width - 1, y + 14, 6, 0x12FFFFFF);
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
        rounded(context, x, y, x + width, y + height, height / 2, 0xFF101C2C);
        int filled = Math.round(width * Math.max(0.0F, Math.min(1.0F, value)));
        if (filled > 0) {
            rounded(context, x, y, x + Math.max(height, filled), y + height, height / 2, accent);
            rounded(context, x + 1, y + 1, x + Math.max(height, filled) - 1, y + height / 2, Math.max(1, height / 2 - 1), 0x33FFFFFF);
        }
        rounded(context, x, y, x + width, y + height, height / 2, BORDER);
    }

    /* ------------------------------------------------------------------ */
    /* Botões                                                             */
    /* ------------------------------------------------------------------ */

    public static ButtonWidget button(int x, int y, int width, int height, Text label, Runnable action) {
        return new ArcadeButton(x, y, width, height, label, action);
    }

    private static final class ArcadeButton extends ButtonWidget {
        private ArcadeButton(int x, int y, int width, int height, Text message, Runnable action) {
            super(x, y, width, height, message, button -> action.run(), supplier -> message);
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.active && mouseX >= this.x && mouseX < this.x + this.width
                    && mouseY >= this.y && mouseY < this.y + this.height;
            int accent = accentFor(getMessage());
            int base = !this.active ? 0x66101A2A : hovered ? mix(TEAL_DEEP, accent, 0.45F) : 0xE0162B41;
            int border = !this.active ? 0x22FFFFFF : hovered ? accent : 0x44FFFFFF;
            rounded(context, this.x, this.y + 3, this.x + this.width, this.y + this.height + 3, 6, 0x55000000);
            rounded(context, this.x, this.y, this.x + this.width, this.y + this.height, 6, border);
            rounded(context, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 5, base);
            rounded(context, this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height / 2 + 2, 4,
                    this.active ? 0x1FFFFFFF : 0x0AFFFFFF);
            context.fill(this.x + 8, this.y + this.height - 2, this.x + this.width - 8, this.y + this.height - 1,
                    this.active ? (hovered ? accent : (accent & 0x00FFFFFF) | 0x99000000) : 0x22556680);
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            context.drawCenteredTextWithShadow(font, getMessage(), this.x + this.width / 2, this.y + this.height / 2 - 4,
                    !this.active ? DIM : hovered ? 0xFFFFFFFF : 0xFFDCECF8);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Formas e ícones                                                    */
    /* ------------------------------------------------------------------ */

    public static void rounded(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
        if (x2 <= x1 || y2 <= y1) {
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        if (r == 0) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }
        context.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            double offset = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(Math.max(0.0, (double) r * r - offset * offset)));
            context.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            context.fill(x1 + inset, y2 - 1 - i, x2 - inset, y2 - i, color);
        }
    }

    public static void disc(DrawContext context, int centerX, int centerY, int radius, int color) {
        int r = Math.max(0, radius);
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
            context.fill(centerX - half, centerY + dy, centerX + half + 1, centerY + dy + 1, color);
        }
    }

    public static void ring(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int half = (int) Math.sqrt((double) radius * radius - (double) dy * dy);
            if (Math.abs(dy) >= radius - thickness) {
                context.fill(centerX - half, centerY + dy, centerX + half + 1, centerY + dy + 1, color);
            } else {
                context.fill(centerX - half, centerY + dy, centerX - half + thickness, centerY + dy + 1, color);
                context.fill(centerX + half - thickness + 1, centerY + dy, centerX + half + 1, centerY + dy + 1, color);
            }
        }
    }

    public static void glow(DrawContext context, int centerX, int centerY, int radius, int color) {
        int rgb = color & 0x00FFFFFF;
        for (int i = 4; i >= 1; i--) {
            disc(context, centerX, centerY, Math.max(1, radius * i / 4), (0x10 << 24) | rgb);
        }
    }

    public static void drawX(DrawContext context, int centerX, int centerY, int size, int thickness, int color) {
        for (int i = -size; i <= size; i++) {
            context.fill(centerX + i, centerY + i, centerX + i + thickness, centerY + i + thickness, color);
            context.fill(centerX + i, centerY - i, centerX + i + thickness, centerY - i + thickness, color);
        }
    }

    public static void drawO(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
        ring(context, centerX, centerY, radius, thickness, color);
    }

    public static void drawCheck(DrawContext context, int x, int y, int color) {
        for (int i = 0; i < 4; i++) {
            context.fill(x + i, y + 3 + i, x + i + 1, y + 4 + i, color);
            context.fill(x + 7 - i, y + i, x + 8 - i, y + 1 + i, color);
        }
    }

    public static void star(DrawContext context, int centerX, int centerY, int size, int color) {
        for (int i = 0; i < size; i++) {
            int half = Math.max(0, size / 2 - i / 2);
            context.fill(centerX - half, centerY - size + i, centerX + half + 1, centerY - size + i + 1, color);
            context.fill(centerX - half, centerY + size - i, centerX + half + 1, centerY + size - i + 1, color);
        }
        context.fill(centerX - 1, centerY - size, centerX + 2, centerY + size + 1, color);
        context.fill(centerX - size, centerY - 1, centerX + size + 1, centerY + 2, color);
    }

    public static void heart(DrawContext context, int centerX, int centerY, int size, int color) {
        disc(context, centerX - size / 2, centerY - size / 4, Math.max(1, size / 2), color);
        disc(context, centerX + size / 2, centerY - size / 4, Math.max(1, size / 2), color);
        for (int i = 0; i <= size; i++) {
            int half = size - i;
            context.fill(centerX - half, centerY - size / 4 + i, centerX + half + 1, centerY - size / 4 + i + 1, color);
        }
    }

    public static void divider(DrawContext context, int x1, int x2, int y) {
        context.fillGradient(x1, y, x2, y + 1, 0x00000000, 0x66FFFFFF);
        context.fillGradient(x1, y, x2, y + 1, 0x44FFFFFF, 0x00000000);
    }

    /* ------------------------------------------------------------------ */
    /* Cores utilitárias                                                  */
    /* ------------------------------------------------------------------ */

    public static int accentFor(Text text) {
        return accentFor(text.asTruncatedString(64));
    }

    public static int accentFor(String name) {
        int hash = name == null ? 0 : name.hashCode();
        return ACCENTS[Math.floorMod(hash, ACCENTS.length)];
    }

    public static int mix(int from, int to, float amount) {
        float a = Math.max(0.0F, Math.min(1.0F, amount));
        int r = (int) (((from >> 16) & 255) * (1 - a) + ((to >> 16) & 255) * a);
        int g = (int) (((from >> 8) & 255) * (1 - a) + ((to >> 8) & 255) * a);
        int b = (int) ((from & 255) * (1 - a) + (to & 255) * a);
        int alpha = (from >>> 24) == 0 ? (to >>> 24) : (from >>> 24);
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int color, int amount) {
        int r = Math.min(255, ((color >> 16) & 255) + amount);
        int g = Math.min(255, ((color >> 8) & 255) + amount);
        int b = Math.min(255, (color & 255) + amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int darken(int color, int amount) {
        int r = Math.max(0, ((color >> 16) & 255) - amount);
        int g = Math.max(0, ((color >> 8) & 255) - amount);
        int b = Math.max(0, (color & 255) - amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int alpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }
}
