package br.com.megaminigames;

import br.com.megaminigames.client.GamesHubScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Inicialização de código que existe somente no cliente. */
public final class MegaMiniGamesClient implements ClientModInitializer {
    private static boolean openHubOnNextTick;

    @Override
    public void onInitializeClient() {
        // Agenda a abertura para depois que o chat fechar. Sem esse adiamento, o
        // ChatScreen pode limpar a tela que o comando acabou de abrir.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("mmg")
                        .then(ClientCommandManager.literal("games_hub").executes(context -> {
                            openHubOnNextTick = true;
                            return 1;
                        }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openHubOnNextTick) {
                openHubOnNextTick = false;
                client.setScreen(new GamesHubScreen());
            }
        });

        // Mantido como canal de compatibilidade para a rota de comando do servidor.
        ClientPlayNetworking.registerGlobalReceiver(MegaMiniGames.OPEN_GAMES_HUB_PACKET,
                (client, handler, buffer, responseSender) -> client.execute(
                        () -> client.setScreen(new GamesHubScreen())));
    }
}
