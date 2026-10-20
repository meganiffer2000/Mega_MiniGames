package br.com.megaminigames.client;

import net.minecraft.client.gui.DrawContext;

/**
 * Sorveteria Gelada — a terceira loja sobre {@link ShopSimScreen}.
 *
 * <p>Leite, creme e caldas viram 10 sorvetes diferentes. O freezer guarda os
 * potes e a bancada tem a batedeira onde o jogador testa as combinações.</p>
 */
public final class SorveteriaScreen extends ShopSimScreen {

    public SorveteriaScreen() {
        super("Sorveteria Gelada");
    }

    @Override
    protected void configure() {
        ING = new String[] {
                "Leite", "Creme", "Açúcar", "Baunilha", "Morango", "Chocolate",
                "Coco", "Limão", "Calda de Caramelo", "Granulado", "Calda de Morango", "Casquinha"
        };
        ING_PRICE = new int[] {5, 9, 4, 11, 8, 10, 7, 5, 9, 6, 8, 5};
        ING_COLOR = new int[] {
                0xFFF5FAFF, 0xFFFFF3D6, 0xFFF7F3E8, 0xFFF0E0B0, 0xFFF0708A, 0xFF8A5A3E,
                0xFFF2EAD8, 0xFFCFE36B, 0xFFD89B3F, 0xFF7A5AA8, 0xFFE0455F, 0xFFD9A15C
        };

        RECIPE_NAME = new String[] {
                "Baunilha", "Chocolate", "Morango", "Coco", "Limão",
                "Sundae de Caramelo", "Sundae de Morango", "Milkshake de Baunilha",
                "Casquinha Dupla", "Taça Especial"
        };
        RECIPE_ITEMS = new int[][] {
                {0, 1, 2, 3},
                {0, 1, 2, 5},
                {0, 1, 2, 4},
                {0, 1, 2, 6},
                {0, 1, 2, 7},
                {0, 1, 2, 8, 9},
                {0, 1, 2, 10, 9},
                {0, 0, 1, 2, 3},
                {11, 0, 1, 5, 4},
                {11, 0, 1, 5, 8, 9}
        };
        RECIPE_PRICE = new int[] {64, 66, 62, 60, 58, 78, 76, 80, 86, 104};
        RECIPE_COLOR = new int[] {
                0xFFF0E0B0, 0xFF8A5A3E, 0xFFF0708A, 0xFFF2EAD8, 0xFFCFE36B,
                0xFFD89B3F, 0xFFE0455F, 0xFFF5F0DC, 0xFFD9A15C, 0xFFB06AA8
        };

        EMPLOYEE_NAME = new String[] {
                "Atendente Júnior", "Balconista Rápido", "Gerente de Loja",
                "Sorveteiro Aprendiz", "Mestre Sorveteiro", "Lenda Gelada"
        };
        EMPLOYEE_CHEF = new boolean[] {false, false, false, true, true, true};
        EMPLOYEE_COST = new int[] {180, 420, 900, 300, 700, 1500};
        EMPLOYEE_LEVEL = new int[] {2, 4, 6, 3, 5, 8};
        EMPLOYEE_SPEED = new int[] {360, 240, 140, 300, 180, 100};

        COLLECTIBLE = new String[] {
                "Cereja de Plástico", "Colherzinha Dourada", "Casquinha Pintada", "Moeda Antiga",
                "Flor de Cerejeira", "Estrela de Açúcar", "Laço de Fita", "Bilhete de Festa"
        };
        COLLECTIBLE_COLOR = new int[] {
                0xFFE0455F, 0xFFE5B84B, 0xFFD9A15C, 0xFFC9A227,
                0xFFF39AC0, 0xFFF7E06B, 0xFF9B7BF0, 0xFF6CC9F5
        };
        SPECIAL_CUSTOMER = new String[] {
                "Criança", "Artista", "Turista", "Bibliotecária", "Carteiro", "Feirante"
        };

        shopSign = "SORVETERIA";
        shopWord = "sorveteria";
        shopWordCap = "Sorveteria";
        product = "sorvete";
        products = "sorvetes";
        productPrefix = "Sorvete de ";
        productReady = "pronto";
        station = "freezer";
        stationCap = "Freezer";
        stationLabel = "FREEZER";
        stationVerb = "congelar";
        bakeAction = "CONGELAR";
        bakingWord = "Congelando";
        readySuffix = "saiu do freezer!";
        waiterRole = "atendente";
        chefRole = "sorveteiro";
        hintStation = "[E] freezer";
        dailyPromo = true;
        promoLabel = "SABOR DO DIA";
        heatWaves = true;
        heatEvery = 1500;
        heatDuration = 600;

        WORLD_WIDTH = 1240;
        SHOP_END = 700;
        STREET_END = 980;
        FLOOR_Y = 176;
        STATION_A_X = 150;
        STATION_B_X = 330;
        COUNTER_X = 520;
        SHOP_X = 1120;
        DOOR_X = 660;

        WALL_TOP = 0xFF9FE0D8;
        WALL_TRIM = 0xFF66B8AE;
        WALL_WOOD = 0xFFF4E3E8;
        WALL_WOOD_LINE = 0xFFD9A9B6;
        WALL_PANEL = 0xFFE7C6CF;
        FLOOR_A = 0xFFF2F2F0;
        FLOOR_B = 0xFF3A3F47;
        SHOP_WALL = 0xFFC96A85;
        SHOP_WALL_TOP = 0xFF9E4C65;
        SHOP_SHELF = 0xFF8A5A3E;
        SHOP_JAR_COLORS = new int[] {0xFFF0708A, 0xFFF0E0B0, 0xFFCFE36B, 0xFF7A5AA8};
        SIGN_BG = 0xFFF6C6D8;
        SIGN_BORDER = 0xFF7A3B52;
        SIGN_TEXT = 0xFF6B2B44;
        ACCENT_STATION = ArcadeTheme.SKY;
        ACCENT_BENCH = ArcadeTheme.MAGENTA;
    }

    /** Casquinhas pintadas na parede, entre a janela e o quadro de menu. */
    @Override
    protected int decorColor(int wx, int wy, int delta) {
        if (wx < 246 || wx > 456 || wy < 54 || wy > 118) return 0;
        int column = (wx - 246) / 26;
        int local = (wx - 246) % 26;
        int center = 13;
        int top = 58 + (column % 2) * 8;
        int distance = Math.abs(local - center);
        // casquinha (triângulo de waffle)
        if (wy >= top + 18 && wy <= top + 34 && distance <= (wy - top - 18) * 6 / 16 + 2) {
            boolean lattice = (local + wy) % 6 == 0;
            return shade(lattice ? 0xFFB5813F : 0xFFD9A15C, delta);
        }
        // bola de sorvete
        int dy = wy - (top + 13);
        int radius = 8;
        if (distance * distance + dy * dy <= radius * radius) {
            int[] scoops = {0xFFF0708A, 0xFFF0E0B0, 0xFF7A5AA8, 0xFFCFE36B};
            int scoop = scoops[column % scoops.length];
            if (distance * distance + dy * dy <= 6) return shade(scoop, delta + 20);
            return shade(scoop, delta);
        }
        return 0;
    }

    /** Piso xadrez preto e branco. */
    @Override
    protected int floorColor(int wx, int wy) {
        return ((wx / 24) + ((wy - FLOOR_Y) / 24)) % 2 == 0 ? FLOOR_A : FLOOR_B;
    }

    /** Freezer de vidro com potes coloridos. */
    @Override
    protected void drawStationA(DrawContext context, int x, int floorY) {
        int top = floorY - 48;
        ArcadeTheme.rounded(context, x - 30, top + 10, x + 30, floorY, 5, 0xFFE8F4F8);
        ArcadeTheme.rounded(context, x - 27, top + 13, x + 27, floorY - 4, 4, 0xFFD2E8F0);
        context.fill(x - 24, floorY - 8, x - 16, floorY - 2, 0xFF9DB2CC);
        context.fill(x + 16, floorY - 8, x + 24, floorY - 2, 0xFF9DB2CC);
        drawLabel(context, x, floorY + 2, stationLabel, ACCENT_STATION);
    }

    @Override
    protected void drawStationAFront(DrawContext context, int x, int floorY) {
        int top = floorY - 48;
        ArcadeTheme.rounded(context, x - 30, top, x + 30, top + 16, 5, 0xFFAFD7E6);
        ArcadeTheme.rounded(context, x - 26, top + 3, x + 26, top + 13, 3, 0xFFE8FAFF);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {
                int color = ING_COLOR[(row * 4 + column + 4) % ING.length];
                ArcadeTheme.disc(context, x - 18 + column * 12, top + 7 + row * 4, 3, color);
            }
        }
        int shimmer = (ticks / 8) % 2 == 0 ? 0x66FFFFFF : 0x33FFFFFF;
        context.fill(x - 24, top + 4, x - 20, top + 12, shimmer);
        ArcadeTheme.glow(context, x, top + 8, 20, 0x335FE0FF);
        for (int index = 0; index < SAVE.baking.size() && index < ovenSlots(); index++) {
            ArcadeTheme.disc(context, x - 10 + index * 10, floorY - 22, 3, 0xFFB9F26B);
        }
    }

    /** Bancada com batedeira e tigela. */
    @Override
    protected void drawStationB(DrawContext context, int x, int floorY) {
        int top = floorY - 34;
        context.fill(x - 32, top, x + 32, top + 5, 0xFFF4E3E8);
        context.fill(x - 32, top + 5, x + 32, top + 8, 0xFFD9A9B6);
        context.fill(x - 28, top + 8, x - 24, floorY, 0xFFC98FA1);
        context.fill(x + 24, top + 8, x + 28, floorY, 0xFFC98FA1);
        ArcadeTheme.rounded(context, x - 24, top - 16, x - 8, top - 2, 3, 0xFFE8709A);
        context.fill(x - 18, top - 14, x - 14, top - 3, 0xFFB0D8E6);
        ArcadeTheme.rounded(context, x - 6, top - 12, x + 6, top - 2, 3, 0xFFF2F7FF);
        ArcadeTheme.disc(context, x, top - 6, 4, 0xFFF0708A);
        context.fill(x + 10, top - 10, x + 18, top - 2, 0xFFF7F3E8);
        context.fill(x + 12, top - 14, x + 16, top - 8, 0xFFD9A15C);
        drawLabel(context, x, floorY + 2, "BANCADA", ACCENT_BENCH);
    }
}
