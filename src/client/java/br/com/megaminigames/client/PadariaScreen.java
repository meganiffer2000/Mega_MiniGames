package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;

/**
 * Padaria da Esquina — a segunda loja sobre {@link ShopSimScreen}.
 *
 * <p>Pães, bolos e doces saem do forno de tijolos. As receitas continuam
 * escondidas: o jogador combina farinha, fermento e recheios na bancada até
 * descobrir cada uma das 10 delícias.</p>
 */
public final class PadariaScreen extends ShopSimScreen {

    public PadariaScreen() {
        super("Padaria da Esquina");
    }

    @Override
    protected void configure() {
        ING = new String[] {
                "Farinha", "Água", "Fermento", "Sal", "Açúcar", "Manteiga",
                "Leite", "Ovos", "Chocolate", "Canela", "Queijo", "Presunto"
        };
        ING_PRICE = new int[] {5, 2, 4, 2, 4, 7, 6, 8, 10, 6, 9, 7};
        ING_COLOR = new int[] {
                0xFFF2E8D5, 0xFF9FD8F0, 0xFFE0C08A, 0xFFFFFFFF, 0xFFF7F3E8, 0xFFFFE39A,
                0xFFF5FAFF, 0xFFF2C65C, 0xFF6B4028, 0xFFB5813F, 0xFFFFD966, 0xFFE38C8C
        };

        RECIPE_NAME = new String[] {
                "Pão Francês", "Baguete", "Croissant", "Bolo Caseiro", "Rosca Doce",
                "Sonho", "Pão de Queijo", "Pão de Forma", "Torta de Frango", "Bolo de Chocolate"
        };
        RECIPE_ITEMS = new int[][] {
                {0, 1, 2, 3},
                {0, 1, 2, 3, 3},
                {0, 5, 2, 4},
                {0, 4, 7, 6},
                {0, 4, 9, 5},
                {0, 6, 7, 4},
                {0, 7, 6, 10},
                {0, 1, 2, 4, 5},
                {0, 5, 7, 11},
                {0, 4, 7, 8, 6}
        };
        RECIPE_PRICE = new int[] {40, 48, 54, 56, 58, 60, 62, 66, 70, 84};
        RECIPE_COLOR = new int[] {
                0xFFE0A65A, 0xFFD9A15C, 0xFFE5B84B, 0xFFE8C89A, 0xFFD9A15C,
                0xFFEDC17A, 0xFFF2D08A, 0xFFE8C89A, 0xFFE0B074, 0xFF7A4A2E
        };

        EMPLOYEE_NAME = new String[] {
                "Atendente Júnior", "Balconista Ágil", "Mestre de Balcão",
                "Padeiro Aprendiz", "Padeiro Experiente", "Mestre Padeiro"
        };
        EMPLOYEE_CHEF = new boolean[] {false, false, false, true, true, true};
        EMPLOYEE_COST = new int[] {180, 420, 900, 300, 700, 1500};
        EMPLOYEE_LEVEL = new int[] {2, 4, 6, 3, 5, 8};
        EMPLOYEE_SPEED = new int[] {360, 240, 140, 300, 180, 100};

        COLLECTIBLE = new String[] {
                "Selo Dourado", "Espiga de Trigo", "Colher de Pau", "Moeda Antiga",
                "Flor de Cerejeira", "Pena de Galinha", "Botão de Camisa", "Lápis de Cor"
        };
        COLLECTIBLE_COLOR = new int[] {
                0xFFE5B84B, 0xFFD9C25A, 0xFFA2734A, 0xFFC9A227,
                0xFFF39AC0, 0xFFF2F0E4, 0xFF8FA6C4, 0xFF5AA9E6
        };
        SPECIAL_CUSTOMER = new String[] {
                "Moleiro", "Fazendeiro", "Padeiro", "Carteiro", "Clérigo", "Comerciante"
        };

        shopSign = "PADARIA";
        shopWord = "padaria";
        shopWordCap = "Padaria";
        product = "pão";
        products = "pães";
        productPrefix = "";
        productReady = "pronto";
        station = "forno";
        stationCap = "Forno";
        stationLabel = "FORNO";
        stationVerb = "assar";
        bakeAction = "ASSAR";
        bakingWord = "Assando";
        readySuffix = "saiu do forno!";
        waiterRole = "atendente";
        chefRole = "padeiro";
        hintStation = "[E] forno de tijolos";
        dailyPromo = true;
        promoLabel = "PÃO DO DIA";
        freshnessTicks = 1500;
        stalePercent = 65;
        freshWord = "quentinho";
        staleWord = "frio";

        WORLD_WIDTH = 1240;
        SHOP_END = 700;
        STREET_END = 980;
        FLOOR_Y = 176;
        STATION_A_X = 150;
        STATION_B_X = 330;
        COUNTER_X = 520;
        SHOP_X = 1120;
        DOOR_X = 660;

        WALL_TOP = 0xFFE3CDA6;
        WALL_TRIM = 0xFFA98556;
        WALL_WOOD = 0xFFC9A472;
        WALL_WOOD_LINE = 0xFFA98556;
        WALL_PANEL = 0xFF8A6440;
        FLOOR_A = 0xFFD9C49A;
        FLOOR_B = 0xFFC3A97C;
        SHOP_WALL = 0xFF7A6244;
        SHOP_WALL_TOP = 0xFF5C4A32;
        SHOP_SHELF = 0xFF8A5A3E;
        SHOP_JAR_COLORS = new int[] {0xFFD9A15C, 0xFFE5B84B, 0xFFF2E8D5, 0xFFB5813F};
        SIGN_BG = 0xFFE9C77B;
        SIGN_BORDER = 0xFF5A3A20;
        SIGN_TEXT = 0xFF4A2C13;
        ACCENT_STATION = ArcadeTheme.CORAL;
        ACCENT_BENCH = ArcadeTheme.GOLD;
    }

    /** Prateleiras de pão na parede, entre a janela e o quadro de menu. */
    @Override
    protected int decorColor(int wx, int wy, int delta) {
        if (wx < 246 || wx > 456 || wy < 58 || wy > 116) return 0;
        int shelf = (wy - 58) % 28;
        if (shelf < 3) return shade(WALL_TRIM, delta);
        if (shelf < 7 || shelf > 25) return 0;
        int local = (wx - 246) % 22;
        int column = (wx - 246) / 22;
        int kind = column % 3;
        // cada prateleira tem um tipo de pão: filão, baguete ou pão redondo
        if (kind == 1) {
            if (local < 2 || local > 19) return 0;
            int top = shelf < 15 ? shelf : 22 - shelf;
            if (top < 2) return 0;
            int shadeTop = local < 6 ? shade(0xFFE0B074, 12) : 0xFFD9A15C;
            return shade(shelf > 17 ? shade(shadeTop, -20) : shadeTop, delta);
        }
        if (kind == 2) {
            int dx = local - 10;
            int dy = shelf - 16;
            if (dx * dx * 2 + dy * dy * 3 > 92) return 0;
            return shade(dx * dx + dy * dy < 16 ? 0xFFE8C089 : 0xFFD9A15C, delta);
        }
        if (local < 2 || local > 19) return 0;
        int top = shelf < 16 ? shelf : 23 - shelf;
        if (top < 2) return 0;
        int score = (local - 10) / 6;
        if (score == 0 || score == -1) return shade(shelf > 18 ? 0xFFC98A4A : 0xFFE0B074, delta);
        return shade(0xFFD9A15C, delta);
    }

    /** Piso de tábuas corridas. */
    @Override
    protected int floorColor(int wx, int wy) {
        int band = (wy - FLOOR_Y) / 8;
        int base = band % 2 == 0 ? FLOOR_A : FLOOR_B;
        if ((wy - FLOOR_Y) % 8 == 0) base = 0xFFA98556;
        if (wx % 96 < 2) base = 0xFFA98556;
        return base;
    }

    /** Forno de tijolos com cúpula e chaminé. */
    @Override
    protected void drawStationA(DrawContext context, int x, int floorY) {
        int top = floorY - 54;
        context.fill(x - 30, top + 16, x + 30, floorY, 0xFF8A4A32);
        for (int row = 0; row < 4; row++) {
            int y = top + 20 + row * 8;
            context.fill(x - 30, y, x + 30, y + 1, 0xFF6E3A26);
            for (int column = 0; column < 6; column++) {
                int offset = row % 2 == 0 ? 0 : 5;
                context.fill(x - 28 + column * 10 + offset, y - 6, x - 27 + column * 10 + offset, y,
                        0xFF6E3A26);
            }
        }
        ArcadeTheme.disc(context, x, top + 14, 24, 0xFF9A5A3E);
        ArcadeTheme.disc(context, x, top + 14, 21, 0xFFA9694A);
        ArcadeTheme.disc(context, x, top + 14, 17, 0xFFB97A57);
        ArcadeTheme.rounded(context, x - 13, floorY - 34, x + 13, floorY - 4, 4, 0xFF2A1A12);
        context.fill(x - 4, top - 12, x + 4, top + 4, 0xFF6E4A38);
        context.fill(x - 6, top - 14, x + 6, top - 10, 0xFF8A5A44);
        drawLabel(context, x, floorY + 2, stationLabel, ACCENT_STATION);
    }

    @Override
    protected void drawStationAFront(DrawContext context, int x, int floorY) {
        int glow = (ticks / 6) % 2 == 0 ? 0xFFFF8A2B : 0xFFFFC24D;
        context.fill(x - 9, floorY - 30, x + 9, floorY - 16, glow);
        context.fill(x - 6, floorY - 26, x + 6, floorY - 18, 0xFFFFF0B8);
        ArcadeTheme.glow(context, x, floorY - 23, 16, 0x66FF9A3C);
        context.fill(x - 13, floorY - 30, x + 13, floorY - 28, 0xFF8A4A32);
        for (int index = 0; index < SAVE.baking.size() && index < ovenSlots(); index++) {
            ArcadeTheme.disc(context, x - 10 + index * 10, floorY - 23, 3, 0xFFE8C089);
        }
    }

    /** Bancada de madeira com rolo, farinha e massa. */
    @Override
    protected void drawStationB(DrawContext context, int x, int floorY) {
        int top = floorY - 34;
        context.fill(x - 32, top, x + 32, top + 5, 0xFFD9B47A);
        context.fill(x - 32, top + 5, x + 32, top + 8, 0xFFA98556);
        context.fill(x - 28, top + 8, x - 24, floorY, 0xFF8A6440);
        context.fill(x + 24, top + 8, x + 28, floorY, 0xFF8A6440);
        context.fill(x - 22, top - 3, x + 2, top, 0xFFC9A472);
        context.fill(x - 26, top - 4, x - 22, top + 1, 0xFF8A6440);
        context.fill(x + 2, top - 4, x + 6, top + 1, 0xFF8A6440);
        ArcadeTheme.disc(context, x + 16, top - 6, 7, 0xFFF2E8D5);
        ArcadeTheme.disc(context, x + 16, top - 8, 4, 0xFFFFFBF0);
        context.fill(x - 14, top - 12, x - 10, top - 2, 0xFFF7F3E8);
        drawLabel(context, x, floorY + 2, "BANCADA", ACCENT_BENCH);
    }
}
