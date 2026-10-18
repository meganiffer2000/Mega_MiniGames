package br.com.megaminigames;

import br.com.megaminigames.client.GamesHubScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/** Inicialização de código que existe somente no cliente. */
public final class MegaMiniGamesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Comando local para que o jogador possa abrir o hub sem depender da
        // disponibilidade de comandos no servidor ou de permissões de operador.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("mmg")
                        .then(ClientCommandManager.literal("games_hub").executes(context -> {
                            MinecraftClient.getInstance().setScreen(new GamesHubScreen());
                            return 1;
                        }))));

        // Mantido como canal de compatibilidade para a rota de comando do servidor.
        ClientPlayNetworking.registerGlobalReceiver(MegaMiniGames.OPEN_GAMES_HUB_PACKET,
                (client, handler, buffer, responseSender) -> client.execute(
                        () -> client.setScreen(new GamesHubScreen())));
    }
}
