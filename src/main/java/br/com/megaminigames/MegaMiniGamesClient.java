package br.com.megaminigames;

import br.com.megaminigames.client.GamesHubScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Inicialização de código que existe somente no cliente. */
public final class MegaMiniGamesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MegaMiniGames.OPEN_GAMES_HUB_PACKET,
                (client, handler, buffer, responseSender) -> client.execute(
                        () -> client.setScreen(new GamesHubScreen())));
    }
}
