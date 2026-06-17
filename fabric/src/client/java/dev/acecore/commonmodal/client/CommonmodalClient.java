package dev.acecore.commonmodal.client;

import dev.acecore.commonmodal.client.network.ClientNetworking;
import dev.acecore.commonmodal.client.screen.CommonModalScreens;
import dev.acecore.commonmodal.protocol.FormPayload;
import dev.acecore.commonmodal.protocol.ResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

/**
 * commonModal Fabric クライアントmod の初期化エントリポイント。
 * <p>
 * plan.md §5 に基づき、以下を行う。
 * <ul>
 *   <li>サーバーからの {@code commonmodal:form} パケット受信ハンドラ登録</li>
 *   <li>受信したフォームを {@link CommonModalScreens} に渡して表示</li>
 *   <li>画面操作結果を {@code commonmodal:response} パケットでサーバーへ返却</li>
 * </ul>
 */
public final class CommonmodalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworking.register(this::onFormReceived);
    }

    private void onFormReceived(FormPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        CommonModalScreens.open(client, payload.getId(), payload.getForm(), (formId, value) -> {
            ResponsePayload response = new ResponsePayload(formId, value);
            ClientNetworking.sendResponse(response);
        });
    }
}
