package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Vinte jogos curtos adicionais, cada qual com regras e controles próprios. */
public final class ExtraArcadeScreen extends Screen {
    public enum Game {
        SLIDING_15("Jogo dos 15"), GOMOKU("Gomoku"), ANAGRAM("Anagrama"), HIGH_LOW("Maior ou Menor"),
        DICE_DUEL("Duelo de Dados"), PENALTIES("Pênaltis"), AIM_TRAINER("Caçador de Alvos"),
        FROGGER("Sapo na Estrada"), MINI_GOLF("Mini Golfe"), NUMBER_MEMORY("Memória Numérica"),
        COLOR_MATCH("Combinação de Cores"), STAR_CATCH("Pegue as Estrelas"), STOP_BAR("Pare a Barra"),
        TREASURE_HUNT("Caça ao Tesouro"), LANE_RACER("Corrida de Faixas"), BASKET_SHOT("Arremesso ao Cesto"),
        JUMP_HURDLE("Salto de Obstáculos"), ARROW_SEQUENCE("Sequência de Setas"),
        TRUE_FALSE("Verdadeiro ou Falso"), CARD_WAR("Guerra de Cartas");

        private final String title;
        Game(String title) { this.title = title; }
    }

    private static final String[] WORDS = {"PRAIA", "NUVEM", "LIVRO", "MUNDO", "PLANO", "FESTA", "FOLHA", "TIGRE", "PEDRA", "CASTELO"};
    private static final String[] COLORS = {"VERMELHO", "AZUL", "VERDE", "AMARELO"};
    private static final int[] COLOR_VALUES = {0xFFE74C4C, 0xFF4386E8, 0xFF4EBD70, 0xFFFFD64D};
    private static final String[] ARROWS = {"↑", "→", "↓", "←"};

    private final Game game;
    private int[][] grid = new int[11][11];
    private boolean[][] revealed = new boolean[6][6];
    private final List<Token> tokens = new ArrayList<>();
    private final List<Integer> sequence = new ArrayList<>();
    private int areaLeft, areaTop, areaWidth, areaHeight;
    private int score, round, phase, timeLeft, ticks;
    private int playerX, playerY, targetX, targetY, selectedX, selectedY;
    private int card, nextCard, playerRoll, cpuRoll, keeper;
    private int strikes, hitCount, moveCount, moveDirection, power;
    private int correctAnswer, answerA, answerB, answerC;
    private int sequenceIndex, sequenceTimer, lane, jumpDistance, golfRemaining;
    private int barPosition, barDirection, misses;
    private boolean gameOver, won, initialized, truthValue, colorMatches, jumped;
    private String answer = "", secret = "", scrambled = "", input = "", message = "";

    public ExtraArcadeScreen(Game game) {
        super(Text.literal(game.title));
        this.game = game;
    }

    @Override
    protected void init() {
        areaLeft = 18;
        areaTop = 52;
        areaWidth = Math.max(100, width - 36);
        areaHeight = Math.max(80, height - 100);
        addDrawableChild(ArcadeTheme.button(width / 2 - 116, height - 25, 112, 20, Text.literal("Reiniciar (R)"), () -> resetGame()));
        addDrawableChild(ArcadeTheme.button(width / 2 + 4, height - 25, 112, 20, Text.literal("Voltar ao menu"), () -> client.setScreen(new GamesHubScreen())));
        if (!initialized) {
            resetGame();
            initialized = true;
        }
    }

    private void resetGame() {
        for (int[] row : grid) java.util.Arrays.fill(row, 0);
        for (boolean[] row : revealed) java.util.Arrays.fill(row, false);
        tokens.clear();
        sequence.clear();
        score = round = phase = ticks = strikes = hitCount = moveCount = misses = 0;
        timeLeft = 0;
        gameOver = won = jumped = false;
        input = answer = message = "";
        switch (game) {
            case SLIDING_15 -> setupSlidingPuzzle();
            case GOMOKU -> message = "Clique para colocar X. Faça cinco em linha antes da IA.";
            case ANAGRAM -> { round = 1; setupAnagram(); }
            case HIGH_LOW -> { card = random(1, 13); message = "A próxima carta será maior ou menor?"; }
            case DICE_DUEL -> message = "Clique na área para rolar os dados. Primeiro a 3 vitórias.";
            case PENALTIES -> message = "Escolha um dos três alvos para chutar.";
            case AIM_TRAINER -> { timeLeft = 600; relocateTarget(); message = "Clique no alvo! Você tem 30 segundos."; }
            case FROGGER -> setupFrogger();
            case MINI_GOLF -> { playerX = 1; playerY = 7; targetX = 9; targetY = 1; power = 3; moveDirection = 0; golfRemaining = 0; message = "Setas ajustam direção/potência; Espaço bate."; }
            case NUMBER_MEMORY -> { sequence.add(random(0, 9)); showNumberSequence(); }
            case COLOR_MATCH -> { timeLeft = 600; nextColorMatch(); }
            case STAR_CATCH -> { timeLeft = 600; playerX = 5; message = "Mova a cesta com ←/→ e pegue as estrelas."; }
            case STOP_BAR -> { barPosition = 0; barDirection = 1; round = 0; message = "Pare a barra na faixa verde com Espaço."; }
            case TREASURE_HUNT -> setupTreasure();
            case LANE_RACER -> { timeLeft = 900; lane = 1; message = "Troque de faixa com ←/→ e desvie dos blocos."; }
            case BASKET_SHOT -> { barPosition = 0; barDirection = 1; round = 0; message = "Acerte a força (zona verde) com Espaço."; }
            case JUMP_HURDLE -> { jumpDistance = 34; message = "Pressione Espaço perto do obstáculo."; }
            case ARROW_SEQUENCE -> { sequence.add(random(0, 3)); showArrowSequence(); }
            case TRUE_FALSE -> { round = 0; score = 0; makeTrueFalse(); }
            case CARD_WAR -> { round = score = 0; message = "Clique para virar uma carta; maior valor vence."; }
        }
    }

    private int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void setupSlidingPuzzle() {
        int value = 1;
        for (int row = 0; row < 4; row++) for (int col = 0; col < 4; col++) grid[row][col] = value++ % 16;
        for (int i = 0; i < 120; i++) {
            int blank = blankIndex();
            int row = blank / 4;
            int col = blank % 4;
            int direction = random(0, 3);
            int nr = row + (direction == 0 ? -1 : direction == 1 ? 1 : 0);
            int nc = col + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
            if (nr >= 0 && nr < 4 && nc >= 0 && nc < 4) {
                grid[row][col] = grid[nr][nc];
                grid[nr][nc] = 0;
            }
        }
        message = "Clique em uma peça ao lado do espaço vazio para ordenar 1–15.";
    }

    private int blankIndex() {
        for (int i = 0; i < 16; i++) if (grid[i / 4][i % 4] == 0) return i;
        return 15;
    }

    private void movePuzzle(int row, int col) {
        int blank = blankIndex();
        int br = blank / 4, bc = blank % 4;
        if (Math.abs(br - row) + Math.abs(bc - col) == 1) {
            grid[br][bc] = grid[row][col];
            grid[row][col] = 0;
            moveCount++;
            won = isPuzzleSolved();
            gameOver = won;
            if (won) message = "Quebra-cabeça resolvido em " + moveCount + " movimentos!";
        }
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < 15; i++) if (grid[i / 4][i % 4] != i + 1) return false;
        return grid[3][3] == 0;
    }

    private void setupAnagram() {
        secret = WORDS[random(0, WORDS.length - 1)];
        char[] letters = secret.toCharArray();
        do {
            for (int i = letters.length - 1; i > 0; i--) {
                int j = random(0, i);
                char c = letters[i]; letters[i] = letters[j]; letters[j] = c;
            }
            scrambled = new String(letters);
        } while (scrambled.equals(secret));
        message = "Reorganize as letras e pressione Enter.";
    }

    private void relocateTarget() {
        targetX = areaLeft + random(24, Math.max(25, areaWidth - 24));
        targetY = areaTop + random(24, Math.max(25, areaHeight - 24));
    }

    private void setupFrogger() {
        playerX = 5; playerY = 10;
        for (int row = 1; row < 9; row++) {
            for (int col = random(0, 3); col < 11; col += random(2, 4)) grid[row][col] = 1;
        }
        message = "Atravesse as pistas e chegue ao topo sem tocar nos carros.";
    }

    private void setupTreasure() {
        targetX = random(0, 5); targetY = random(0, 5);
        round = 12;
        message = "Encontre o tesouro em até 12 tentativas. Clique nas casas.";
    }

    private void showNumberSequence() {
        phase = 0;
        sequenceTimer = 70;
        input = "";
        message = "Memorize: " + sequenceText();
    }

    private String sequenceText() {
        StringBuilder text = new StringBuilder();
        for (int n : sequence) text.append(n);
        return text.toString();
    }

    private void showArrowSequence() {
        phase = 0;
        sequenceIndex = 0;
        sequenceTimer = 70;
        message = "Memorize: " + arrowText();
    }

    private String arrowText() {
        StringBuilder text = new StringBuilder();
        for (int n : sequence) text.append(ARROWS[n]).append(' ');
        return text.toString();
    }

    private void makeTrueFalse() {
        int a = random(2, 20), b = random(2, 20);
        boolean actual = random(0, 1) == 1;
        int result = actual ? a + b : a + b + random(1, 5) * (random(0, 1) == 0 ? 1 : -1);
        answerA = a; answerB = b; correctAnswer = result;
        truthValue = result == a + b;
        message = "Questão " + (round + 1) + "/10";
    }

    private void nextTrueFalse(boolean yes) {
        if (gameOver) return;
        if (yes == truthValue) score++;
        round++;
        if (round >= 10) {
            gameOver = true;
            message = "Fim: " + score + "/10 corretas. Pressione R.";
        } else makeTrueFalse();
    }

    private void resolveColor(boolean yes) {
        if (gameOver) return;
        if (yes == colorMatches) score++;
        else if (++strikes >= 3) gameOver = true;
        if (!gameOver) nextColorMatch();
        else message = "Fim! Pontos: " + score;
    }

    private int wordColor, inkColor;
    private void nextColorMatch() {
        wordColor = random(0, 3); inkColor = random(0, 3);
        if (random(0, 1) == 1) inkColor = wordColor;
        else if (inkColor == wordColor) inkColor = (wordColor + 1) % 4;
        colorMatches = wordColor == inkColor;
        message = "O nome combina com a cor da tinta? Clique SIM ou NÃO.";
    }

    private void shootPenalty(int direction) {
        if (gameOver) return;
        keeper = random(0, 2);
        if (direction != keeper) score++;
        round++;
        if (round >= 5) {
            gameOver = true;
            message = "Placar: " + score + "/5 gols. Pressione R para repetir.";
        } else message = direction == keeper ? "Defendido! Próxima cobrança." : "Gol! Próxima cobrança.";
    }

    private void drawDice() {
        playerRoll = random(1, 6) + random(1, 6);
        cpuRoll = random(1, 6) + random(1, 6);
        if (playerRoll > cpuRoll) score++;
        else if (cpuRoll > playerRoll) misses++;
        round++;
        message = "Você " + playerRoll + " × " + cpuRoll + " computador — " + (playerRoll == cpuRoll ? "empate" : playerRoll > cpuRoll ? "ponto seu" : "ponto da IA");
        if (score >= 3 || misses >= 3 || round >= 5) gameOver = true;
    }

    private void playHighLow(boolean higher) {
        if (gameOver) return;
        nextCard = random(1, 13);
        boolean correct = higher ? nextCard >= card : nextCard <= card;
        if (nextCard == card) correct = true;
        if (correct) score++;
        round++;
        message = "Carta: " + cardName(nextCard) + (correct ? " — acertou!" : " — errou!");
        card = nextCard;
        if (round >= 10) {
            gameOver = true;
            message = "Fim: " + score + "/10 acertos. Pressione R.";
        }
    }

    private String cardName(int rank) {
        return switch (rank) { case 1 -> "Ás"; case 11 -> "Valete"; case 12 -> "Dama"; case 13 -> "Rei"; default -> Integer.toString(rank); };
    }

    @Override
    public void tick() {
        ticks++;
        switch (game) {
            case AIM_TRAINER, COLOR_MATCH, STAR_CATCH -> {
                if (timeLeft > 0 && --timeLeft == 0) {
                    gameOver = true;
                    if (game == Game.AIM_TRAINER) message = "Tempo! Alvos acertados: " + score;
                    else if (game == Game.COLOR_MATCH) message = "Tempo! Pontos: " + score + " • Erros: " + strikes;
                    else message = "Tempo! Estrelas coletadas: " + score;
                }
                if (game == Game.STAR_CATCH && !gameOver) tickStars();
            }
            case FROGGER -> { if (ticks % 8 == 0) moveCars(); }
            case MINI_GOLF -> tickGolf();
            case NUMBER_MEMORY -> {
                if (phase == 0 && sequenceTimer > 0 && --sequenceTimer == 0) {
                    phase = 1; message = "Repita a sequência com as teclas numéricas.";
                }
            }
            case STOP_BAR, BASKET_SHOT -> {
                barPosition += barDirection * (game == Game.STOP_BAR ? 3 : 2);
                if (barPosition >= 100) { barPosition = 100; barDirection = -1; }
                if (barPosition <= 0) { barPosition = 0; barDirection = 1; }
            }
            case LANE_RACER -> { if (timeLeft > 0 && --timeLeft == 0) { gameOver = true; message = "Corrida concluída! Pontuação: " + score; } else if (!gameOver && ticks % 12 == 0) tickRacer(); }
            case JUMP_HURDLE -> tickHurdle();
            case ARROW_SEQUENCE -> {
                if (phase == 0 && sequenceTimer > 0 && --sequenceTimer == 0) {
                    phase = 1; sequenceIndex = 0; message = "Repita as setas com o teclado.";
                }
            }
            default -> { }
        }
    }

    private void tickStars() {
        if (ticks % 14 == 0) tokens.add(new Token(random(0, 10), 0, false));
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            token.y += 0.24;
            if (token.y >= 9) {
                if (token.x == playerX) score++;
                else if (++misses >= 10) { gameOver = true; message = "Muitas estrelas perdidas! Pontos: " + score; }
                tokens.remove(i);
            }
        }
    }

    private void moveCars() {
        for (int row = 1; row < 9; row++) {
            int[] old = grid[row].clone();
            int shift = row % 2 == 0 ? 1 : -1;
            for (int col = 0; col < 11; col++) grid[row][(col + shift + 11) % 11] = old[col];
        }
        if (playerY > 0 && playerY < 9 && grid[playerY][playerX] == 1) {
            gameOver = true;
            message = "O sapo foi atingido! Pontuação: " + score;
        }
    }

    private void moveFrog(int dx, int dy) {
        if (gameOver) return;
        int x = Math.max(0, Math.min(10, playerX + dx));
        int y = Math.max(0, Math.min(10, playerY + dy));
        playerX = x;
        playerY = y;
        if (y == 0) {
            score++;
            playerX = 5;
            playerY = 10;
            message = "Chegou ao outro lado! Mais uma travessia.";
        } else if (y > 0 && y < 9 && grid[y][x] == 1) {
            gameOver = true;
            message = "O sapo foi atingido! Pontuação: " + score;
        }
    }

    private void tickGolf() {
        if (phase != 2 || ticks % 3 != 0) return;
        int[][] dirs = {{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1}};
        playerX += dirs[moveDirection][0];
        playerY += dirs[moveDirection][1];
        golfRemaining--;
        if (playerX < 0 || playerX > 10 || playerY < 0 || playerY > 7) {
            playerX -= dirs[moveDirection][0]; playerY -= dirs[moveDirection][1]; phase = 0;
            message = "A bola saiu do campo. Ajuste a tacada e tente de novo.";
        } else if (Math.abs(playerX - targetX) <= 1 && Math.abs(playerY - targetY) <= 1) {
            gameOver = true; won = true; message = "Bola no buraco em " + score + " tacadas!";
        } else if (golfRemaining <= 0) {
            phase = 0;
            message = "Tacada concluída. Ajuste a direção e tente de novo.";
        }
    }

    private void tickRacer() {
        if (random(0, 2) == 0) grid[0][random(0, 2)] = 1;
        for (int row = 9; row >= 0; row--) {
            for (int col = 0; col < 3; col++) {
                if (grid[row][col] == 1) {
                    if (row == 9 && col == lane) { gameOver = true; message = "Batida! Distância: " + score; }
                    else if (row == 9) { grid[row][col] = 0; score++; }
                    else { grid[row + 1][col] = 1; grid[row][col] = 0; }
                }
            }
        }
    }

    private void tickHurdle() {
        if (gameOver) return;
        jumpDistance--;
        if (jumpDistance <= 5 && !jumped) {
            gameOver = true;
            message = "Você bateu no obstáculo! Pontos: " + score;
        } else if (jumpDistance <= 0) {
            score++;
            jumpDistance = 34;
            jumped = false;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
ArcadeTheme.background(context, width, height);
        int accent = ArcadeTheme.accentFor(title);
        context.fillGradient(0, 0, width, 64, ArcadeTheme.alpha(accent, 30), 0x00000000);
        ArcadeTheme.rounded(context, 8, 3, width - 8, height - 25, 14, ArcadeTheme.alpha(accent, 110));
        ArcadeTheme.rounded(context, 9, 4, width - 9, height - 26, 13, 0xFF07101C);
        ArcadeTheme.rounded(context, 11, 6, width - 11, height - 28, 11, 0xFF0E1B2C);
        ArcadeTheme.rounded(context, 12, 7, width - 12, height - 29, 10, 0xFF122134);
        context.fillGradient(15, 44, width - 15, 116, ArcadeTheme.alpha(accent, 16), 0x00000000);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 12, accent);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 31,
                gameOver ? ArcadeTheme.GOLD : ArcadeTheme.TEXT);
        switch (game) {
            case SLIDING_15 -> renderSliding(context);
            case GOMOKU -> renderGomoku(context);
            case ANAGRAM -> renderAnagram(context);
            case HIGH_LOW -> renderHighLow(context);
            case DICE_DUEL -> renderDice(context);
            case PENALTIES -> renderPenalties(context);
            case AIM_TRAINER -> renderAim(context);
            case FROGGER -> renderFrogger(context);
            case MINI_GOLF -> renderGolf(context);
            case NUMBER_MEMORY -> renderNumberMemory(context);
            case COLOR_MATCH -> renderColorMatch(context);
            case STAR_CATCH -> renderStars(context);
            case STOP_BAR -> renderStopBar(context);
            case TREASURE_HUNT -> renderTreasure(context);
            case LANE_RACER -> renderRacer(context);
            case BASKET_SHOT -> renderBasket(context);
            case JUMP_HURDLE -> renderHurdle(context);
            case ARROW_SEQUENCE -> renderArrowSequence(context);
            case TRUE_FALSE -> renderTrueFalse(context);
            case CARD_WAR -> renderCardWar(context);
        }
        if (gameOver) ArcadeTheme.status(context, textRenderer, Text.literal("R para jogar novamente"),
                width / 2, height - 45, ArcadeTheme.GOLD);
        if (width >= 460) {
            ArcadeTheme.pill(context, textRenderer, "PONTOS  " + score, 16, height - 22, accent);
            ArcadeTheme.pillRight(context, textRenderer, "RODADA  " + round, width - 16, height - 22,
                    ArcadeTheme.GOLD);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSliding(DrawContext c) {
        int size = Math.min(62, Math.min((width - 50) / 4, (height - 115) / 4));
        int x0 = (width - size * 4) / 2, y0 = 56;
        for (int r=0;r<4;r++) for(int col=0;col<4;col++) {
            int x=x0+col*size,y=y0+r*size,v=grid[r][col];
            c.fill(x+2,y+2,x+size-2,y+size-2,v==0?0xFF303C48:0xFFCEB878);
            if(v>0)c.drawCenteredTextWithShadow(textRenderer,Text.literal(""+v),x+size/2,y+size/2-4,0xFF20252B);
        }
        drawCentered(c,"Movimentos: "+moveCount,width/2,y0+4*size+5,0xFFFFFFFF);
    }
    private void renderGomoku(DrawContext c) {
        int size=Math.min(34,Math.min((width-50)/9,(height-115)/9)); int x0=(width-9*size)/2,y0=53;
        for(int r=0;r<9;r++)for(int col=0;col<9;col++){
            int x=x0+col*size,y=y0+r*size;c.fill(x,y,x+size,y+size,0xFFB99A62);
            c.fill(x+size/2,y+size/2,x+size/2+2,y+size/2+2,0xFF4A3928);
            if(grid[r][col]!=0)disc(c,x+size/2,y+size/2,size/3,grid[r][col]==1?0xFF262626:0xFFE9E3D4);
        }
    }
    private void renderAnagram(DrawContext c) {
        drawCentered(c,"Letras embaralhadas",width/2,height/2-50,0xFFE6E6E6);
        drawCentered(c,scrambled,width/2,height/2-20,0xFFFFD65A);
        c.fill(width/2-130,height/2+5,width/2+130,height/2+48,0xFF273A49);
        drawCentered(c,input+"_",width/2,height/2+20,0xFFFFFFFF);
        drawCentered(c,"Rodada "+round+" • Acertos: "+score+" • Enter confirma",width/2,height/2+65,0xFFDCE8F2);
    }
    private void renderHighLow(DrawContext c) {
        drawCentered(c,"Carta atual",width/2,height/2-65,0xFFDEE8EF);
        c.fill(width/2-48,height/2-45,width/2+48,height/2+65,0xFFF1EEE3);
        drawCentered(c,cardName(card),width/2,height/2+2,0xFF18212A);
        choiceBox(c,width/2-130,height/2+88,110,34,"MENOR",0xFF4589D1);
        choiceBox(c,width/2+20,height/2+88,110,34,"MAIOR",0xFF48A565);
        drawCentered(c,"Acertos: "+score+"/10",width/2,height/2+140,0xFFFFFFFF);
    }
    private void renderDice(DrawContext c) {
        drawCentered(c,"Você",width/2-85,height/2-35,0xFFFFFFFF);
        drawCentered(c,"Computador",width/2+85,height/2-35,0xFFFFFFFF);
        c.fill(width/2-125,height/2-18,width/2-45,height/2+62,0xFFF0E8D8);
        c.fill(width/2+45,height/2-18,width/2+125,height/2+62,0xFFF0E8D8);
        drawCentered(c,round==0?"?":""+playerRoll,width/2-85,height/2+14,0xFF222222);
        drawCentered(c,round==0?"?":""+cpuRoll,width/2+85,height/2+14,0xFF222222);
        choiceBox(c,width/2-115,height/2+88,230,32,"ROLAR DADOS",0xFF4589D1);
        drawCentered(c,"Vitórias: "+score+" • Derrotas: "+misses,width/2,height/2+135,0xFFFFFFFF);
    }
    private void renderPenalties(DrawContext c) {
        for(int i=0;i<3;i++){int x=width/2-150+i*100;c.fill(x,height/2-15,x+88,height/2+65,0xFF397B4A);drawCentered(c,new String[]{"ESQUERDA","CENTRO","DIREITA"}[i],x+44,height/2+18,0xFFFFFFFF);}
        drawCentered(c,"Gols: "+score+" / Cobranças: "+round,width/2,height/2+100,0xFFFFFFFF);
    }
    private void renderAim(DrawContext c) {
        c.fill(targetX-12,targetY-12,targetX+12,targetY+12,0xFFE94E4E);
        c.fill(targetX-3,targetY-18,targetX+3,targetY+18,0xFFFFFFFF);
        c.fill(targetX-18,targetY-3,targetX+18,targetY+3,0xFFFFFFFF);
        drawCentered(c,"Alvos: "+score+" • Tempo: "+timeLeft/20+"s",width/2,areaTop+areaHeight-10,0xFFFFFFFF);
    }
    private void renderFrogger(DrawContext c) {
        int size=Math.min(32,Math.min((width-45)/11,(height-115)/11));int x0=(width-11*size)/2,y0=53;
        for(int r=0;r<11;r++)for(int col=0;col<11;col++){
            int x=x0+col*size,y=y0+r*size;int color=r==0||r==10?0xFF458B4F:r%2==0?0xFF454B50:0xFF3D4248;
            c.fill(x+1,y+1,x+size-1,y+size-1,color);
            if(r>0&&r<9&&grid[r][col]==1)c.fill(x+3,y+size/4,x+size-3,y+size*3/4,0xFFE5544E);
            if(r==playerY&&col==playerX)c.fill(x+4,y+4,x+size-4,y+size-4,0xFF55D7A5);
        }
        drawCentered(c,"Salvos: "+score,width/2,y0+11*size+5,0xFFFFFFFF);
    }
    private void renderGolf(DrawContext c) {
        int size=Math.min(34,Math.min((width-50)/11,(height-115)/8));int x0=(width-11*size)/2,y0=57;
        for(int r=0;r<8;r++)for(int col=0;col<11;col++)c.fill(x0+col*size+1,y0+r*size+1,x0+(col+1)*size-1,y0+(r+1)*size-1,0xFF4C945D);
        disc(c,x0+targetX*size+size/2,y0+targetY*size+size/2,size/4,0xFF1C252B);
        disc(c,x0+playerX*size+size/2,y0+playerY*size+size/2,size/4,0xFFFFFFFF);
        drawCentered(c,"Tacadas: "+score+" • Direção: "+ARROWS[moveDirection]+" • Força: "+power,width/2,y0+8*size+4,0xFFFFFFFF);
    }
    private void renderNumberMemory(DrawContext c) {
        drawCentered(c,"Rodada "+(score+1),width/2,height/2-65,0xFFFFFFFF);
        drawCentered(c,phase==0?sequenceText():input.isEmpty()?"_":input,width/2,height/2-5,0xFFFFD65A);
        drawCentered(c,message,width/2,height/2+45,0xFFE4EBF1);
    }
    private void renderColorMatch(DrawContext c) {
        drawCentered(c,COLORS[wordColor],width/2,height/2-32,COLOR_VALUES[inkColor]);
        choiceBox(c,width/2-135,height/2+28,120,36,"SIM",0xFF49A364);
        choiceBox(c,width/2+15,height/2+28,120,36,"NÃO",0xFFD04D4D);
        drawCentered(c,"Pontos "+score+" • Erros "+strikes+" • "+timeLeft/20+"s",width/2,height/2+90,0xFFFFFFFF);
    }
    private void renderStars(DrawContext c) {
        int cell=Math.min(38,Math.min((width-40)/11,(height-115)/10));int x0=(width-11*cell)/2,y0=54;
        for(Token t:tokens){int x=x0+(int)t.x*cell+cell/2,y=y0+(int)t.y*cell+cell/2;disc(c,x,y,Math.max(4,cell/4),0xFFFFDF50);}
        c.fill(x0+playerX*cell,y0+9*cell,x0+(playerX+1)*cell,y0+9*cell+cell/2,0xFF5AB9E8);
        drawCentered(c,"Estrelas: "+score+" • Perdidas: "+misses+"/10 • "+timeLeft/20+"s",width/2,y0+10*cell+4,0xFFFFFFFF);
    }
    private void renderStopBar(DrawContext c) {
        int x0=width/2-150,y=height/2-8;c.fill(x0,y,x0+300,y+16,0xFF46515D);
        c.fill(x0+135,y-5,x0+165,y+21,0xFF57B66C);
        c.fill(x0+barPosition*3-3,y-8,x0+barPosition*3+3,y+24,0xFFFFE05A);
        drawCentered(c,"Acertos: "+score+"/5 • Espaço para parar",width/2,height/2+52,0xFFFFFFFF);
    }
    private void renderTreasure(DrawContext c) {
        int size=Math.min(48,Math.min((width-40)/6,(height-115)/6));int x0=(width-6*size)/2,y0=55;
        for(int r=0;r<6;r++)for(int col=0;col<6;col++){
            int x=x0+col*size,y=y0+r*size;int color=0xFF577B93;
            if(revealed[r][col]) color=(r==targetY&&col==targetX)?0xFFFFD34E:0xFF354854;
            c.fill(x+2,y+2,x+size-2,y+size-2,color);
            if(revealed[r][col]&&r==targetY&&col==targetX)drawCentered(c,"★",x+size/2,y+size/2-4,0xFF202020);
        }
        drawCentered(c,"Tentativas restantes: "+round,width/2,y0+6*size+5,0xFFFFFFFF);
    }
    private void renderRacer(DrawContext c) {
        int size=Math.min(42,Math.min((width-40)/3,(height-115)/10));int x0=width/2-size*3/2,y0=53;
        for(int r=0;r<10;r++)for(int col=0;col<3;col++){
            int x=x0+col*size,y=y0+r*size;c.fill(x+1,y+1,x+size-1,y+size-1,0xFF353C44);
            if(grid[r][col]==1)c.fill(x+size/4,y+4,x+size*3/4,y+size-4,0xFFE34C4C);
            if(r==9&&col==lane)c.fill(x+size/4,y+4,x+size*3/4,y+size-4,0xFF59C7FA);
        }
        drawCentered(c,"Distância: "+score+" • "+timeLeft/20+"s",width/2,y0+10*size+4,0xFFFFFFFF);
    }
    private void renderBasket(DrawContext c) {
        c.fill(width/2+70,height/2-45,width/2+76,height/2+50,0xFFE5E5E5);
        c.fill(width/2+76,height/2+45,width/2+120,height/2+51,0xFFE5E5E5);
        int x0=width/2-150,y=height/2+70;c.fill(x0,y,x0+300,y+14,0xFF414D58);
        c.fill(x0+barPosition*3,y-4,x0+barPosition*3+8,y+18,0xFFFFD34F);
        c.fill(x0+180,y-4,x0+240,y+18,0xFF59B96B);
        drawCentered(c,"Cestas: "+score+" • Espaço arremessa",width/2,height/2+110,0xFFFFFFFF);
    }
    private void renderHurdle(DrawContext c) {
        int centerX=width/2;int ground=height/2+54;c.fill(centerX-180,ground,centerX+180,ground+4,0xFFBD9A61);
        c.fill(centerX-120,ground-42,centerX-92,ground,0xFF4DB3F0);
        int obstacleX=centerX-120+jumpDistance*5;c.fill(obstacleX,ground-28,obstacleX+20,ground,0xFFDE5147);
        drawCentered(c,"Saltos: "+score+" • Obstáculo a "+jumpDistance,width/2,height/2-55,0xFFFFFFFF);
        if(jumped&&!gameOver)drawCentered(c,"PULANDO!",centerX-105,ground-50,0xFFFFDD66);
    }
    private void renderArrowSequence(DrawContext c) {
        drawCentered(c,"Rodada "+sequence.size(),width/2,height/2-55,0xFFFFFFFF);
        drawCentered(c,phase==0?arrowText():"Repita com as setas",width/2,height/2,0xFFFFD65A);
    }
    private void renderTrueFalse(DrawContext c) {
        drawCentered(c,answerA+" + "+answerB+" = "+correctAnswer,width/2,height/2-35,0xFFFFD65A);
        choiceBox(c,width/2-132,height/2+25,118,38,"VERDADEIRO",0xFF4CA267);
        choiceBox(c,width/2+14,height/2+25,118,38,"FALSO",0xFFD64D4D);
        drawCentered(c,"Pontos: "+score+" • Questão "+(round+1)+"/10",width/2,height/2+85,0xFFFFFFFF);
    }
    private void renderCardWar(DrawContext c) {
        drawCentered(c,"Você  "+score+"  ×  "+misses+"  Computador",width/2,height/2-60,0xFFFFFFFF);
        c.fill(width/2-95,height/2-30,width/2-15,height/2+75,0xFFF2EEE3);
        c.fill(width/2+15,height/2-30,width/2+95,height/2+75,0xFFF2EEE3);
        drawCentered(c,round==0?"?":""+playerRoll,width/2-55,height/2+12,0xFF20252B);
        drawCentered(c,round==0?"?":""+cpuRoll,width/2+55,height/2+12,0xFF20252B);
        choiceBox(c,width/2-110,height/2+100,220,34,"VIRAR CARTAS",0xFF4986BF);
    }

    private void drawCentered(DrawContext c,String text,int x,int y,int color){c.drawCenteredTextWithShadow(textRenderer,Text.literal(text),x,y,color);}
    private void choiceBox(DrawContext c,int x,int y,int w,int h,String label,int color){c.fill(x,y,x+w,y+h,color);drawCentered(c,label,x+w/2,y+h/2-4,0xFFFFFFFF);}
    private void disc(DrawContext c,int x,int y,int radius,int color){for(int dy=-radius;dy<=radius;dy++){int dx=(int)Math.sqrt(radius*radius-dy*dy);c.fill(x-dx,y+dy,x+dx+1,y+dy+1,color);}}

    @Override
    public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(super.mouseClicked(mouseX,mouseY,button))return true;
        if(button!=0||gameOver)return false;
        switch(game){
            case SLIDING_15 -> {int size=Math.min(62,Math.min((width-50)/4,(height-115)/4));int x0=(width-size*4)/2,y0=56;int col=(int)((mouseX-x0)/size),row=(int)((mouseY-y0)/size);if(mouseX>=x0&&mouseY>=y0&&col<4&&row<4&&col>=0&&row>=0)movePuzzle(row,col);}
            case GOMOKU -> playGomoku(mouseX,mouseY);
            case HIGH_LOW -> {if(mouseY>height/2+70)playHighLow(mouseX>width/2);}
            case DICE_DUEL -> {if(mouseY>height/2-40&&mouseY<height/2+140){drawDice();}}
            case PENALTIES -> {if(mouseY>height/2-25&&mouseY<height/2+75){int d=(int)((mouseX-(width/2-150))/100);if(d>=0&&d<3)shootPenalty(d);}}
            case AIM_TRAINER -> {if(Math.abs(mouseX-targetX)<=18&&Math.abs(mouseY-targetY)<=18){score++;relocateTarget();}}
            case COLOR_MATCH -> {if(mouseY>height/2+20&&mouseY<height/2+80)resolveColor(mouseX<width/2);}
            case TREASURE_HUNT -> playTreasure(mouseX,mouseY);
            case TRUE_FALSE -> {if(mouseY>height/2+15&&mouseY<height/2+75)nextTrueFalse(mouseX<width/2);}
            case CARD_WAR -> {if(mouseY>height/2-35&&mouseY<height/2+145)drawCardWar();}
            default -> { }
        }
        return true;
    }

    private void playGomoku(double mx,double my){int size=Math.min(34,Math.min((width-50)/9,(height-115)/9));int x0=(width-9*size)/2,y0=53;int c=(int)((mx-x0)/size),r=(int)((my-y0)/size);if(mx<x0||my<y0||c<0||c>=9||r<0||r>=9||grid[r][c]!=0)return;grid[r][c]=1;if(wonAt(r,c,1)){gameOver=true;won=true;message="Cinco em linha! Você venceu.";return;}int[] move=chooseGomokuMove();if(move[0]<0){gameOver=true;message="Tabuleiro cheio: empate!";return;}grid[move[0]][move[1]]=2;if(wonAt(move[0],move[1],2)){gameOver=true;message="A IA completou cinco.";}}
    private int[] chooseGomokuMove(){int[] best={-1,-1};int bestScore=Integer.MIN_VALUE;for(int r=0;r<9;r++)for(int c=0;c<9;c++)if(grid[r][c]==0){int near=0;for(int dr=-1;dr<=1;dr++)for(int dc=-1;dc<=1;dc++){int rr=r+dr,cc=c+dc;if(rr>=0&&rr<9&&cc>=0&&cc<9&&grid[rr][cc]!=0)near++;}int value=near*10-Math.abs(r-4)-Math.abs(c-4);if(value>bestScore){bestScore=value;best=new int[]{r,c};}}return best;}
    private boolean wonAt(int r,int c,int p){int[][]dirs={{1,0},{0,1},{1,1},{1,-1}};for(int[]d:dirs){int n=1;for(int k=1;k<5;k++){int y=r+d[0]*k,x=c+d[1]*k;if(y<0||y>=9||x<0||x>=9||grid[y][x]!=p)break;n++;}for(int k=1;k<5;k++){int y=r-d[0]*k,x=c-d[1]*k;if(y<0||y>=9||x<0||x>=9||grid[y][x]!=p)break;n++;}if(n>=5)return true;}return false;}
    private void playTreasure(double mx,double my){int size=Math.min(48,Math.min((width-40)/6,(height-115)/6));int x0=(width-6*size)/2,y0=55;int c=(int)((mx-x0)/size),r=(int)((my-y0)/size);if(mx<x0||my<y0||c<0||c>=6||r<0||r>=6||revealed[r][c])return;revealed[r][c]=true;round--;if(r==targetY&&c==targetX){won=gameOver=true;score=1;message="Tesouro encontrado!";}else if(round<=0){gameOver=true;message="As tentativas acabaram. O tesouro está marcado.";}else{int d=Math.abs(r-targetY)+Math.abs(c-targetX);message=d<=1?"Muito quente! Tentativas: "+round:d<=3?"Quente. Tentativas: "+round:"Frio. Tentativas: "+round;}}
    private void drawCardWar(){playerRoll=random(1,13);cpuRoll=random(1,13);round++;if(playerRoll>cpuRoll)score++;else if(cpuRoll>playerRoll)misses++;message="Sua carta "+cardName(playerRoll)+" × "+cardName(cpuRoll)+" da IA";if(score>=5||misses>=5){gameOver=true;won=score>misses;message=won?"Você venceu a guerra de cartas!":"A IA venceu a guerra de cartas.";}}

    @Override
    public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(keyCode==GLFW.GLFW_KEY_R){resetGame();return true;}
        if(gameOver)return super.keyPressed(keyCode,scanCode,modifiers);
        switch(game){
            case ANAGRAM -> {if(keyCode==GLFW.GLFW_KEY_ENTER){if(input.equals(secret)){score++;round++;if(round>5){won=gameOver=true;message="Você venceu! 5 anagramas resolvidos.";}else{setupAnagram();message="Correto! Próxima palavra: "+scrambled;}}else{message="Não é essa palavra. Tente novamente.";}input="";return true;}if(keyCode==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty()){input=input.substring(0,input.length()-1);return true;}if(keyCode>=GLFW.GLFW_KEY_A&&keyCode<=GLFW.GLFW_KEY_Z&&input.length()<secret.length()){input+=(char)('A'+keyCode-GLFW.GLFW_KEY_A);return true;}}
            case FROGGER -> {if(keyCode==GLFW.GLFW_KEY_LEFT)moveFrog(-1,0);else if(keyCode==GLFW.GLFW_KEY_RIGHT)moveFrog(1,0);else if(keyCode==GLFW.GLFW_KEY_UP)moveFrog(0,-1);else if(keyCode==GLFW.GLFW_KEY_DOWN)moveFrog(0,1);else return super.keyPressed(keyCode,scanCode,modifiers);return true;}
            case MINI_GOLF -> {if(keyCode==GLFW.GLFW_KEY_LEFT){moveDirection=(moveDirection+7)%8;return true;}if(keyCode==GLFW.GLFW_KEY_RIGHT){moveDirection=(moveDirection+1)%8;return true;}if(keyCode==GLFW.GLFW_KEY_UP){power=Math.min(6,power+1);return true;}if(keyCode==GLFW.GLFW_KEY_DOWN){power=Math.max(1,power-1);return true;}if(keyCode==GLFW.GLFW_KEY_SPACE&&phase!=2){phase=2;score++;golfRemaining=power*2;ticks=0;return true;}}
            case NUMBER_MEMORY -> {if(phase==1&&keyCode>=GLFW.GLFW_KEY_0&&keyCode<=GLFW.GLFW_KEY_9&&input.length()<sequence.size()){input+=(char)('0'+keyCode-GLFW.GLFW_KEY_0);return true;}if(keyCode==GLFW.GLFW_KEY_BACKSPACE&&!input.isEmpty()){input=input.substring(0,input.length()-1);return true;}if(keyCode==GLFW.GLFW_KEY_ENTER&&phase==1){if(input.equals(sequenceText())){score++;sequence.add(random(0,9));showNumberSequence();}else{gameOver=true;message="Errou! Você chegou à rodada "+(score+1)+".";}return true;}}
            case STAR_CATCH -> {if(keyCode==GLFW.GLFW_KEY_LEFT)playerX=Math.max(0,playerX-1);if(keyCode==GLFW.GLFW_KEY_RIGHT)playerX=Math.min(10,playerX+1);return true;}
            case STOP_BAR -> {if(keyCode==GLFW.GLFW_KEY_SPACE){boolean hit=barPosition>=45&&barPosition<=55;if(hit)score++;round++;if(round>=5){gameOver=true;won=score>=3;message="Precisão: "+score+"/5";}else message=hit?"Perfeito! Próxima rodada.":"Fora da faixa! Tente novamente.";return true;}}
            case LANE_RACER -> {if(keyCode==GLFW.GLFW_KEY_LEFT)lane=Math.max(0,lane-1);if(keyCode==GLFW.GLFW_KEY_RIGHT)lane=Math.min(2,lane+1);return true;}
            case BASKET_SHOT -> {if(keyCode==GLFW.GLFW_KEY_SPACE){boolean hit=barPosition>=60&&barPosition<=80;if(hit)score++;round++;if(round>=10){gameOver=true;message="Cestas: "+score+"/10";}else message=hit?"Cesta! Próximo arremesso.":"Errou! Ajuste o tempo.";return true;}}
            case JUMP_HURDLE -> {if(keyCode==GLFW.GLFW_KEY_SPACE&&jumpDistance<=10&&jumpDistance>=5){jumped=true;return true;}}
            case ARROW_SEQUENCE -> {if(phase==1){int v=keyCode==GLFW.GLFW_KEY_UP?0:keyCode==GLFW.GLFW_KEY_RIGHT?1:keyCode==GLFW.GLFW_KEY_DOWN?2:keyCode==GLFW.GLFW_KEY_LEFT?3:-1;if(v>=0){if(v!=sequence.get(sequenceIndex)){gameOver=true;message="Sequência errada! Rodada alcançada: "+score;}else if(++sequenceIndex==sequence.size()){score++;sequence.add(random(0,3));showArrowSequence();}return true;}}}
            default -> { }
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override
    public boolean shouldPause(){return false;}
    private static final class Token{private final int x;private double y;private Token(int x,double y,boolean ignored){this.x=x;this.y=y;}}
}
