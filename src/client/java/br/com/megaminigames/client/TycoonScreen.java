package br.com.megaminigames.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Central de gestão: 26 negócios com três motores de jogo diferentes.
 *
 * <ul>
 *   <li><b>RUSH</b> — fila de clientes com paciência e preparo por teclas 1–3.</li>
 *   <li><b>CYCLE</b> — comprar insumo, produzir e vender no melhor preço de mercado.</li>
 *   <li><b>RESPONSE</b> — resolver incidentes escolhendo a resposta certa a tempo.</li>
 * </ul>
 */
public final class TycoonScreen extends ArcadeScreen {
    private enum Engine { RUSH, CYCLE, RESPONSE }

    private enum Mode {
        PIZZARIA("Pizzaria Expressa", Engine.RUSH, ArcadeTheme.CORAL, "Margherita", "Calabresa", "Quatro Queijos"),
        PADARIA("Padaria da Esquina", Engine.RUSH, ArcadeTheme.GOLD, "Pão", "Croissant", "Bolo"),
        SORVETERIA("Sorveteria Gelada", Engine.RUSH, ArcadeTheme.SKY, "Baunilha", "Chocolate", "Morango"),
        MERCADO("Mercado do Bairro", Engine.RUSH, ArcadeTheme.LIME, "Frutas", "Padaria", "Limpeza"),
        CINEMA("Cinema Pipoca", Engine.RUSH, ArcadeTheme.MAGENTA, "Pipoca", "Refri", "Nachos"),
        LANCHONETE("Lanchonete Turbo", Engine.RUSH, ArcadeTheme.CORAL, "Burger", "Batata", "Milk-shake"),
        FLORICULTURA("Floricultura Bella", Engine.RUSH, ArcadeTheme.LIME, "Buquê", "Vaso", "Semente"),
        FARMACIA("Farmácia 24h", Engine.RUSH, ArcadeTheme.SKY, "Analgésico", "Curativo", "Vitamina"),
        SUSHI("Sushi Rápido", Engine.RUSH, ArcadeTheme.CORAL, "Nigiri", "Temaki", "Uramaki"),
        BRINQUEDOS("Loja de Brinquedos", Engine.RUSH, ArcadeTheme.GOLD, "Urso", "Carrinho", "Quebra-cabeça"),

        FABRICA("Fábrica de Robôs", Engine.CYCLE, ArcadeTheme.SKY, "Circuito", "Robô", ""),
        PORTO("Porto Container", Engine.CYCLE, ArcadeTheme.CORAL, "Carga", "Contêiner", ""),
        SOLAR("Fazenda Solar", Engine.CYCLE, ArcadeTheme.GOLD, "Célula", "Energia", ""),
        ESTUFA("Estufa Hidropônica", Engine.CYCLE, ArcadeTheme.LIME, "Muda", "Alface", ""),
        APIARIO("Apiário Doce Mel", Engine.CYCLE, ArcadeTheme.GOLD, "Néctar", "Mel", ""),
        MINERADORA("Mineradora Profunda", Engine.CYCLE, ArcadeTheme.CORAL, "Minério", "Barra", ""),
        FERROVIA("Ferrovia Central", Engine.CYCLE, ArcadeTheme.PURPLE, "Trilho", "Viagem", ""),
        SUCOS("Fábrica de Sucos", Engine.CYCLE, ArcadeTheme.LIME, "Fruta", "Suco", ""),
        GRAFICA("Gráfica Expressa", Engine.CYCLE, ArcadeTheme.SKY, "Papel", "Revista", ""),
        CERAMICA("Cerâmica Vila", Engine.CYCLE, ArcadeTheme.CORAL, "Argila", "Vaso", ""),

        HOSPITAL("Hospital Central", Engine.RESPONSE, ArcadeTheme.SKY, "", "", ""),
        BANCO("Banco Seguro", Engine.RESPONSE, ArcadeTheme.GOLD, "", "", ""),
        JORNAL("Redação do Jornal", Engine.RESPONSE, ArcadeTheme.MUTED, "", "", ""),
        AEROPORTO("Aeroporto Internacional", Engine.RESPONSE, ArcadeTheme.PURPLE, "", "", ""),
        HOTEL("Hotel Estrela", Engine.RESPONSE, ArcadeTheme.CORAL, "", "", ""),
        ZOOLOGICO("Zoológico Urbano", Engine.RESPONSE, ArcadeTheme.LIME, "", "", "");

        private final String title;
        private final Engine engine;
        private final int accent;
        private final String itemA;
        private final String itemB;
        private final String itemC;

        Mode(String title, Engine engine, int accent, String itemA, String itemB, String itemC) {
            this.title = title;
            this.engine = engine;
            this.accent = accent;
            this.itemA = itemA;
            this.itemB = itemB;
            this.itemC = itemC;
        }
    }

    /** Texto no formato {@code problema#opção A#opção B#opção C#índice correto}. */
    private static final String[][] INCIDENTS = {
            {
                    "Surto de gripe lota o pronto-socorro#Abrir leitos extras e chamar reforço#Mandar todos para casa#Suspender o atendimento#0",
                    "Falta um aparelho de raio-X#Emprestar da clínica parceira#Usar fita adesiva#Ignorar os exames#0",
                    "Família reclama do tempo de espera#Informar o tempo real e priorizar#Discutir na recepção#Fechar as portas#0",
                    "Estoque de luvas acabou#Acionar compra emergencial#Reutilizar luvas usadas#Atender sem proteção#0",
                    "Médico atrasou o plantão#Remanejar equipe e avisar a chefia#Cancelar o turno#Deixar a fila parada#0"
            },
            {
                    "Cliente contestou um saque#Abrir auditoria e mostrar o extrato#Apagar o histórico#Bloquear a conta sem aviso#0",
                    "Sistema caiu no horário de pico#Acionar contingência manual#Culpar a internet#Fechar sem aviso#0",
                    "Transação suspeita detectada#Bloquear preventivamente e investigar#Liberar o valor#Ignorar o alerta#0",
                    "Fila enorme na agência#Abrir mais caixas e orientar senhas#Fechar um caixa#Aumentar a tarifa#0",
                    "Funcionário novo cometeu um erro#Treinar e revisar o processo#Demitir na hora#Esconder do gerente#0"
            },
            {
                    "Fonte vazou antes da apuração#Apurar e adiar a matéria#Publicar mesmo assim#Inventar um depoimento#0",
                    "Entrevistado pediu correção#Conferir e publicar errata#Apagar tudo#Culpar o estagiário#0",
                    "Fechamento em 20 minutos#Priorizar a matéria principal#Sair mais cedo#Entregar em branco#0",
                    "Foto sem direito de uso#Substituir por imagem própria#Usar sem crédito#Perguntar depois#0",
                    "Denúncia anônima sobre a prefeitura#Checar documentos e ouvir o outro lado#Publicar sem checar#Ignorar a denúncia#0"
            },
            {
                    "Voo atrasado por neblina#Informar o painel e realocar passageiros#Embarcar assim mesmo#Cancelar sem aviso#0",
                    "Bagagem extraviada#Rastrear e abrir protocolo#Dizer que não é problema#Oferecer vale sem registro#0",
                    "Passageiro em emergência médica#Acionar equipe de resgate#Pedir para esperar#Negar o embargo#0",
                    "Pista com detrito#Fechar a pista e varrer#Ignorar o objeto#Acelerar a decolagem#0",
                    "Torre sem controlador por 10 min#Segurar pousos e chamar substituto#Autorizar pousos#Deixar por conta dos pilotos#0"
            },
            {
                    "Hóspede reclamou do ar-condicionado#Trocar de quarto e reparar#Cobrar a mais#Dizer que é normal#0",
                    "Overbooking em noite cheia#Realocar em hotel parceiro#Deixar sem quarto#Cancelar a reserva alheia#0",
                    "Café da manhã acabou às 8h#Repor rapidamente e avisar#Fechar o salão#Cobrar extra#0",
                    "Avaliação negativa publicada#Responder e resolver o problema#Apagar o comentário#Ignorar o cliente#0",
                    "Funcionário da limpeza faltou#Redistribuir quartos e ajudar#Deixar sujo#Cobrar do hóspede#0"
            },
            {
                    "Animal recusou alimento#Chamar o veterinário e revisar a dieta#Forçar a comida#Isolar sem registro#0",
                    "Visitante jogou lixo no recinto#Orientar e acionar a limpeza#Ignorar#Fechar o zoo#0",
                    "Fuga de um recinto#Seguir o protocolo e fechar a área#Abrir os portões#Esperar o animal voltar#0",
                    "Calor extremo no recinto#Ligar aspersores e sombra#Deixar como está#Tirar a água#0",
                    "Bilheteria com fila dupla#Abrir segundo guichê#Vender mais caro#Fechar a bilheteria#0"
            }
    };

    private final Mode mode;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final List<Guest> guests = new ArrayList<>();
    private int money;
    private int score;
    private int ticks;
    private int timeLeft;
    private int served;
    private int lost;
    private int combo;
    private int bestCombo;
    private int prepTicks;
    private int prepItem = -1;
    private int spawnTimer;
    private int maxGuests;
    private int price;
    private int raw;
    private int product;
    private int machineLevel;
    private int cycleTimer;
    private int marketPrice;
    private float reputation;
    private int incidentIndex;
    private int incidentRound;
    private int correctAnswers;
    private int wrongAnswers;
    private int extraTime;
    private boolean gameOver;
    private boolean won;
    private String message;
    private int flash;
    private int totalProfit;
    private int bestProfit;

    public TycoonScreen(Mode mode) {
        super(mode.title);
        this.mode = mode;
    }

    public enum Kind {
        PIZZARIA, PADARIA, SORVETERIA, MERCADO, CINEMA, LANCHONETE, FLORICULTURA, FARMACIA, SUSHI, BRINQUEDOS,
        FABRICA, PORTO, SOLAR, ESTUFA, APIARIO, MINERADORA, FERROVIA, SUCOS, GRAFICA, CERAMICA,
        HOSPITAL, BANCO, JORNAL, AEROPORTO, HOTEL, ZOOLOGICO
    }

    public static TycoonScreen of(Kind kind) {
        return new TycoonScreen(Mode.values()[kind.ordinal()]);
    }

    @Override
    protected void init() {
        addFooterButtons(height - 26, this::restart);
        restart();
    }

    private void restart() {
        money = mode.engine == Engine.CYCLE ? 140 : 0;
        score = 0;
        ticks = 0;
        served = 0;
        lost = 0;
        combo = 0;
        bestCombo = 0;
        prepTicks = 0;
        prepItem = -1;
        spawnTimer = 70;
        maxGuests = 4;
        price = 22;
        raw = 4;
        product = 0;
        machineLevel = 0;
        cycleTimer = 18;
        marketPrice = 28;
        reputation = 3.0F;
        incidentIndex = 0;
        incidentRound = 0;
        correctAnswers = 0;
        wrongAnswers = 0;
        extraTime = 2;
        gameOver = false;
        won = false;
        flash = 0;
        totalProfit = 0;
        bestProfit = 0;
        guests.clear();
        switch (mode.engine) {
            case RUSH -> {
                timeLeft = 1800;
                message = "Atenda os pedidos antes da paciência acabar.";
            }
            case CYCLE -> {
                timeLeft = 2400;
                message = "Compre insumo, produza e venda no preço alto.";
            }
            case RESPONSE -> {
                timeLeft = 280;
                message = "Escolha a resposta certa antes do tempo.";
                loadIncident();
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Lógica                                                             */
    /* ------------------------------------------------------------------ */

    private boolean isManagement() {
        return true;
    }

    @Override
    public void tick() {
        if (gameOver) {
            if (flash > 0) flash--;
            return;
        }
        ticks++;
        if (flash > 0) flash--;
        switch (mode.engine) {
            case RUSH -> tickRush();
            case CYCLE -> tickCycle();
            case RESPONSE -> tickResponse();
        }
    }

    /* --- RUSH --------------------------------------------------------- */

    private int rushCapacity() {
        return 4;
    }

    private int rushPatience() {
        return Math.max(70, 130 - served * 3);
    }

    private int rushPrep() {
        return Math.max(10, 24 - served / 4);
    }

    private void tickRush() {
        timeLeft--;
        if (spawnTimer > 0) spawnTimer--;
        if (spawnTimer <= 0 && guests.size() < rushCapacity()) {
            Guest guest = new Guest();
            guest.item = random.nextInt(3);
            guest.maxPatience = rushPatience();
            guest.patience = guest.maxPatience;
            guests.add(guest);
            spawnTimer = Math.max(30, 80 - served * 3) + random.nextInt(20);
        }
        for (Iterator<Guest> iterator = guests.iterator(); iterator.hasNext(); ) {
            Guest guest = iterator.next();
            if (--guest.patience > 0) continue;
            iterator.remove();
            lost++;
            combo = 0;
            message = "Cliente foi embora...";
            flash = 12;
        }
        if (prepTicks > 0 && --prepTicks == 0) {
            serve(prepItem);
            prepItem = -1;
        }
        if (lost >= 6) {
            finish(false, "Muita gente desistiu da fila.");
            return;
        }
        if (timeLeft <= 0) {
            finish(served >= 28, served >= 28
                    ? "Turno excelente! " + served + " clientes atendidos."
                    : "Faltaram clientes: " + served + " de 28.");
        }
    }

    private void serve(int item) {
        Guest target = null;
        for (Guest guest : guests) {
            if (guest.item == item) {
                target = guest;
                break;
            }
        }
        if (target == null) {
            combo = 0;
            score = Math.max(0, score - 4);
            message = "Ninguém pediu " + itemName(item) + ".";
            return;
        }
        guests.remove(target);
        served++;
        combo++;
        bestCombo = Math.max(bestCombo, combo);
        int earned = price + combo * 3;
        money += earned;
        totalProfit += earned;
        bestProfit = Math.max(bestProfit, totalProfit);
        score += 10 + combo * 2;
        message = itemName(item) + " servido  •  +R$ " + earned;
    }

    private void prepare(int item) {
        if (gameOver || prepTicks > 0) return;
        prepItem = item;
        prepTicks = rushPrep();
        message = "Preparando " + itemName(item) + "...";
    }

    private String itemName(int item) {
        return item == 0 ? mode.itemA : item == 1 ? mode.itemB : mode.itemC;
    }

    /* --- CYCLE -------------------------------------------------------- */

    private int cycleCost() {
        return 26 + machineLevel * 4;
    }

    private int cycleOutput() {
        return 1 + machineLevel;
    }

    private int upgradeCost() {
        return 260 + machineLevel * 220;
    }

    private void tickCycle() {
        timeLeft--;
        if (ticks % 90 == 0) {
            double wave = Math.sin(ticks / 90.0);
            marketPrice = Math.max(14, Math.min(52, 30 + (int) (wave * 12) + random.nextInt(5) - 2));
        }
        if (--cycleTimer <= 0) {
            cycleTimer = Math.max(8, 18 - machineLevel * 3);
            if (raw > 0) {
                raw--;
                product += cycleOutput();
            }
        }
        if (money < cycleCost() && raw == 0 && product == 0) {
            finish(false, "O caixa secou. Faturamento: R$ " + money + ".");
            return;
        }
        int goal = cycleGoal();
        if (money >= goal) {
            finish(true, "Meta batida! R$ " + money + " em caixa.");
            return;
        }
        if (timeLeft <= 0) {
            finish(money >= goal, "Turno encerrado com R$ " + money + ".");
        }
    }

    private int cycleGoal() {
        return 3800;
    }

    private void buyRaw() {
        if (gameOver) return;
        int cost = cycleCost();
        if (money < cost) {
            message = "Caixa insuficiente para insumo (R$ " + cost + ").";
            return;
        }
        money -= cost;
        raw += 6;
        message = "+6 " + mode.itemA + " por R$ " + cost;
    }

    private void sellProduct() {
        if (gameOver) return;
        if (product <= 0) {
            message = "Nada de " + mode.itemB + " para vender.";
            return;
        }
        int earned = product * marketPrice;
        money += earned;
        totalProfit += earned;
        bestProfit = Math.max(bestProfit, totalProfit);
        score += product * 4;
        message = "Vendeu " + product + " × " + mode.itemB + " por R$ " + earned;
        product = 0;
    }

    private void upgradeMachine() {
        if (gameOver) return;
        int cost = upgradeCost();
        if (machineLevel >= 3) {
            message = "Máquina no nível máximo.";
            return;
        }
        if (money < cost) {
            message = "Melhoria custa R$ " + cost + ".";
            return;
        }
        money -= cost;
        machineLevel++;
        message = "Máquina nível " + (machineLevel + 1) + "  •  produção x" + cycleOutput();
    }

    /* --- RESPONSE ----------------------------------------------------- */

    private String[] currentIncident() {
        String[] pool = INCIDENTS[mode.ordinal() - Mode.HOSPITAL.ordinal()];
        return pool[incidentIndex % pool.length].split("#");
    }

    private void loadIncident() {
        timeLeft = Math.max(150, 300 - incidentRound * 8);
        incidentIndex = random.nextInt(INCIDENTS[mode.ordinal() - Mode.HOSPITAL.ordinal()].length);
    }

    private void tickResponse() {
        timeLeft--;
        if (timeLeft <= 0) {
            answer(-1);
        }
    }

    private void answer(int option) {
        if (gameOver) return;
        String[] incident = currentIncident();
        int correct = Integer.parseInt(incident[4]);
        incidentRound++;
        if (option == correct) {
            correctAnswers++;
            reputation = Math.min(5.0F, reputation + 0.2F);
            money += 90;
            score += 25;
            message = "Boa decisão! +R$ 90";
            flash = 10;
        } else if (option < 0) {
            wrongAnswers++;
            reputation = Math.max(0.0F, reputation - 0.25F);
            money = Math.max(0, money - 25);
            message = "Tempo esgotado! -R$ 25";
            flash = 12;
        } else {
            wrongAnswers++;
            reputation = Math.max(0.0F, reputation - 0.3F);
            money = Math.max(0, money - 40);
            message = "Decisão ruim! -R$ 40";
            flash = 12;
        }
        if (reputation <= 0.6F) {
            finish(false, "A reputação afundou. Acertos: " + correctAnswers + ".");
            return;
        }
        if (incidentRound >= 14) {
            finish(reputation >= 3.4F, "Turno fechado com " + correctAnswers + " acertos.");
            return;
        }
        loadIncident();
    }

    private void finish(boolean victory, String text) {
        gameOver = true;
        won = victory;
        message = text;
        if (victory) score += 200;
    }

    /* ------------------------------------------------------------------ */
    /* Entrada                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82) {
            restart();
            return true;
        }
        if (gameOver) return super.keyPressed(keyCode, scanCode, modifiers);
        int slot = keyCode == 49 ? 1 : keyCode == 50 ? 2 : keyCode == 51 ? 3 : keyCode == 52 ? 4 : -1;
        switch (mode.engine) {
            case RUSH -> {
                if (slot >= 1 && slot <= 3) {
                    prepare(slot - 1);
                    return true;
                }
            }
            case CYCLE -> {
                if (slot == 1) {
                    buyRaw();
                    return true;
                }
                if (slot == 2) {
                    sellProduct();
                    return true;
                }
                if (slot == 3) {
                    upgradeMachine();
                    return true;
                }
            }
            case RESPONSE -> {
                if (slot >= 1 && slot <= 3) {
                    answer(slot - 1);
                    return true;
                }
                if (keyCode == 32 && extraTime > 0) {
                    extraTime--;
                    timeLeft += 120;
                    message = "Equipe de reforço! +6 segundos";
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ArcadeTheme.background(context, width, height);
        ArcadeTheme.header(context, textRenderer, title, width / 2, 10, mode.accent);
        ArcadeTheme.status(context, textRenderer, Text.literal(message), width / 2, 30,
                gameOver ? (won ? ArcadeTheme.SUCCESS : ArcadeTheme.DANGER) : ArcadeTheme.MUTED);
        ArcadeTheme.pill(context, textRenderer, "CAIXA R$ " + money, 12, 44, mode.accent);
        ArcadeTheme.pillRight(context, textRenderer, "PONTOS " + score, width - 12, 44, ArcadeTheme.GOLD);
        switch (mode.engine) {
            case RUSH -> renderRush(context);
            case CYCLE -> renderCycle(context);
            case RESPONSE -> renderResponse(context);
        }
        if (gameOver) {
            ArcadeTheme.banner(context, textRenderer, won ? "TURNO LUCRATIVO" : "TURNO ENCERRADO",
                    message, width / 2, height / 2 - 10, Math.min(width - 20, 320),
                    won ? ArcadeTheme.SUCCESS : ArcadeTheme.CORAL);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderRush(DrawContext context) {
        int panelLeft = 12;
        int panelRight = width - 12;
        int queueTop = 60;
        int footerY = height - 58;
        int area = Math.max(96, footerY - queueTop);
        int queueHeight = Math.max(60, Math.min(92, area * 2 / 5));
        ArcadeTheme.panelAccent(context, panelLeft, queueTop, panelRight, queueTop + queueHeight,
                mode.accent, 9);
        context.drawTextWithShadow(textRenderer, Text.literal("FILA"), panelLeft + 8, queueTop + 5,
                ArcadeTheme.MUTED);
        int cardWidth = Math.min(96, (panelRight - panelLeft - 22) / 4 - 6);
        int cardHeight = queueHeight - 22;
        for (int index = 0; index < guests.size(); index++) {
            Guest guest = guests.get(index);
            int x = panelLeft + 10 + index * (cardWidth + 6);
            int y = queueTop + 14;
            ArcadeTheme.rounded(context, x, y, x + cardWidth, y + cardHeight, 6, 0xFF16243A);
            ArcadeTheme.rounded(context, x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1, 5,
                    ArcadeTheme.alpha(mode.accent, 45));
            int center = x + cardWidth / 2;
            ArcadeTheme.disc(context, center, y + 10, 6, ArcadeTheme.alpha(mode.accent, 210));
            ArcadeTheme.disc(context, center - 2, y + 9, 1, 0xFF16243A);
            ArcadeTheme.disc(context, center + 2, y + 9, 1, 0xFF16243A);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(itemName(guest.item), cardWidth - 6)),
                    center, y + 19, ArcadeTheme.TEXT);
            int barWidth = cardWidth - 12;
            float value = guest.patience / (float) Math.max(1, guest.maxPatience);
            ArcadeTheme.progress(context, center - barWidth / 2, y + cardHeight - 7, barWidth, 4, value,
                    value > 0.5F ? ArcadeTheme.LIME : value > 0.25F ? ArcadeTheme.GOLD : ArcadeTheme.CORAL);
        }
        if (guests.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Nenhum cliente na fila"),
                    (panelLeft + panelRight) / 2, queueTop + queueHeight / 2, ArcadeTheme.DIM);
        }

        int cardsTop = queueTop + queueHeight + 10;
        int cardHeight2 = Math.max(40, Math.min(64, footerY - cardsTop - 18));
        int cardWidth2 = Math.min(120, (panelRight - panelLeft - 24) / 3);
        for (int index = 0; index < 3; index++) {
            int x = (width - (cardWidth2 * 3 + 16)) / 2 + index * (cardWidth2 + 8);
            boolean active = prepItem == index && prepTicks > 0;
            ArcadeTheme.rounded(context, x, cardsTop, x + cardWidth2, cardsTop + cardHeight2, 8,
                    active ? ArcadeTheme.alpha(mode.accent, 220) : 0xCC122033);
            ArcadeTheme.rounded(context, x + 1, cardsTop + 1, x + cardWidth2 - 1,
                    cardsTop + cardHeight2 - 1, 7, active ? 0x33000000 : 0x10FFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("[" + (index + 1) + "]"),
                    x + cardWidth2 / 2, cardsTop + 5, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(itemName(index), cardWidth2 - 8)),
                    x + cardWidth2 / 2, cardsTop + 18, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("R$ " + (price + (combo + 1) * 3)), x + cardWidth2 / 2,
                    cardsTop + 31, ArcadeTheme.LIME);
            if (active) {
                float value = 1 - prepTicks / (float) Math.max(1, rushPrep());
                ArcadeTheme.progress(context, x + 8, cardsTop + cardHeight2 - 8, cardWidth2 - 16, 4,
                        value, ArcadeTheme.LIME);
            }
        }
        ArcadeTheme.pill(context, textRenderer, "ATENDIDOS " + served + "/28", 12, footerY, ArcadeTheme.LIME);
        ArcadeTheme.pillRight(context, textRenderer,
                "COMBO x" + combo + "  •  PERDIDOS " + lost, width - 12, footerY, ArcadeTheme.CORAL);
        ArcadeTheme.progress(context, 12, footerY + 12, width - 24, 4,
                Math.max(0, timeLeft) / 1800.0F, ArcadeTheme.GOLD);
    }

    private void renderCycle(DrawContext context) {
        int panelLeft = 14;
        int panelRight = width - 14;
        int top = 60;
        int footerY = height - 58;
        int cardsHeight = Math.max(40, Math.min(56, (footerY - top) / 3));
        int panelBottom = footerY - cardsHeight - 18;
        int cardsTop = panelBottom + 10;
        ArcadeTheme.panelAccent(context, panelLeft, top, panelRight, panelBottom, mode.accent, 10);

        int spacing = Math.max(10, (panelBottom - top - 12) / 4);
        int textLeft = panelLeft + 10;
        String[] rows = {
                mode.itemA + ": " + raw,
                mode.itemB + ": " + product,
                "Mercado: R$ " + marketPrice,
                "Máquina: nível " + (machineLevel + 1)
        };
        int[] colors = {ArcadeTheme.TEXT, ArcadeTheme.TEXT, ArcadeTheme.LIME, ArcadeTheme.MUTED};
        for (int index = 0; index < 4; index++) {
            context.drawTextWithShadow(textRenderer, Text.literal(rows[index]), textLeft,
                    top + 7 + index * spacing, colors[index]);
        }

        int chartLeft = panelLeft + Math.min(150, (panelRight - panelLeft) / 2);
        int chartRight = panelRight - 10;
        int chartTop = top + 14;
        int chartBottom = panelBottom - 8;
        if (chartRight - chartLeft > 40 && chartBottom - chartTop > 20) {
            context.drawTextWithShadow(textRenderer, Text.literal("MERCADO"), chartLeft,
                    top + 4, ArcadeTheme.MUTED);
            context.fill(chartLeft, chartTop + 8, chartRight, chartBottom, 0x33000000);
            int previousX = chartLeft;
            int previousY = chartBottom - 4;
            for (int step = 1; step <= 36; step++) {
                double wave = Math.sin((ticks / 90.0) - (step / 90.0));
                int value = (int) (30 + wave * 12);
                int x = chartLeft + (chartRight - chartLeft) * step / 36;
                int y = chartBottom - 4 - (value - 14) * (chartBottom - chartTop - 14) / 38;
                ArcadeTheme.rounded(context, previousX, Math.min(previousY, y), x,
                        Math.max(previousY, y) + 1, 1, ArcadeTheme.alpha(mode.accent, 150));
                previousX = x;
                previousY = y;
            }
            int markerY = chartBottom - 4 - (marketPrice - 14) * (chartBottom - chartTop - 14) / 38;
            context.fill(chartLeft, markerY, chartRight, markerY + 1, ArcadeTheme.GOLD);
        }

        int cardWidth = Math.min(120, (panelRight - panelLeft - 24) / 3);
        String[] labels = {"COMPRAR +6", "VENDER TUDO", "MELHORAR"};
        String[] costs = {"R$ " + cycleCost(), product + " × R$ " + marketPrice, "R$ " + upgradeCost()};
        for (int index = 0; index < 3; index++) {
            int x = (width - (cardWidth * 3 + 16)) / 2 + index * (cardWidth + 8);
            ArcadeTheme.rounded(context, x, cardsTop, x + cardWidth, cardsTop + cardsHeight, 8,
                    0xCC122033);
            ArcadeTheme.rounded(context, x + 1, cardsTop + 1, x + cardWidth - 1,
                    cardsTop + cardsHeight - 1, 7, 0x10FFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("[" + (index + 1) + "]"),
                    x + cardWidth / 2, cardsTop + 4, ArcadeTheme.GOLD);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(labels[index], cardWidth - 8)),
                    x + cardWidth / 2, cardsTop + 16, ArcadeTheme.TEXT);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(costs[index], cardWidth - 8)),
                    x + cardWidth / 2, cardsTop + 29, ArcadeTheme.LIME);
        }
        ArcadeTheme.pill(context, textRenderer, "META R$ " + cycleGoal(), 12, footerY, mode.accent);
        ArcadeTheme.pillRight(context, textRenderer, "FATURADO R$ " + totalProfit, width - 12,
                footerY, ArcadeTheme.LIME);
        ArcadeTheme.progress(context, 12, footerY + 12, width - 24, 4,
                Math.max(0, timeLeft) / 2400.0F, ArcadeTheme.GOLD);
    }

    private void renderResponse(DrawContext context) {
        String[] incident = currentIncident();
        int panelLeft = 14;
        int panelRight = width - 14;
        int top = 60;
        int footerY = height - 58;
        int panelBottom = top + Math.max(66, Math.min(92, footerY - top - 84));
        ArcadeTheme.panelAccent(context, panelLeft, top, panelRight, panelBottom, mode.accent, 10);
        String text = textRenderer.trimToWidth(incident[0], panelRight - panelLeft - 20);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(text),
                width / 2, top + 10, ArcadeTheme.TEXT);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Incidente " + (incidentRound + 1) + "/14"), width / 2, top + 24,
                ArcadeTheme.MUTED);
        ArcadeTheme.progress(context, panelLeft + 12, top + 38, panelRight - panelLeft - 24, 5,
                Math.max(0, timeLeft) / 300.0F,
                timeLeft > 120 ? ArcadeTheme.LIME : timeLeft > 60 ? ArcadeTheme.GOLD : ArcadeTheme.CORAL);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Reforços: " + extraTime + "  •  Espaço adiciona tempo"), width / 2,
                top + 50, ArcadeTheme.DIM);

        int optionTop = panelBottom + 8;
        int optionHeight = Math.max(16, Math.min(26, (footerY - optionTop) / 3 - 4));
        for (int index = 0; index < 3; index++) {
            int y = optionTop + index * (optionHeight + 4);
            ArcadeTheme.rounded(context, panelLeft, y, panelRight, y + optionHeight, 7, 0xCC122033);
            ArcadeTheme.rounded(context, panelLeft + 1, y + 1, panelRight - 1, y + optionHeight - 1, 6,
                    0x10FFFFFF);
            context.drawTextWithShadow(textRenderer, Text.literal("[" + (index + 1) + "]"),
                    panelLeft + 8, y + (optionHeight - 8) / 2, ArcadeTheme.GOLD);
            context.drawTextWithShadow(textRenderer,
                    Text.literal(textRenderer.trimToWidth(incident[index + 1], panelRight - panelLeft - 44)),
                    panelLeft + 30, y + (optionHeight - 8) / 2, ArcadeTheme.TEXT);
        }
        int stars = Math.round(reputation);
        int starY = Math.min(height - 40, optionTop + 3 * (optionHeight + 4) + 6);
        for (int index = 0; index < 5; index++) {
            ArcadeTheme.star(context, 18 + index * 12, starY, 5,
                    index < stars ? ArcadeTheme.GOLD : 0xFF34485F);
        }
        ArcadeTheme.pillRight(context, textRenderer,
                "ACERTOS " + correctAnswers + "  •  ERROS " + wrongAnswers, width - 12, starY - 6,
                ArcadeTheme.SKY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class Guest {
        private int item;
        private int patience;
        private int maxPatience;
    }
}
