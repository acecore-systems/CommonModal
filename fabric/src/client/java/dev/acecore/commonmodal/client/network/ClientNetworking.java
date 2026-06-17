package dev.acecore.commonmodal.client.network;

import dev.acecore.commonmodal.protocol.CommonModalChannels;
import dev.acecore.commonmodal.protocol.FormCodec;
import dev.acecore.commonmodal.protocol.FormPayload;
import dev.acecore.commonmodal.protocol.ResponsePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

/**
 * クライアント側の commonModal ネットワーク設定。
 * <p>
 * plan.md §4 / §5 に基づき、以下を行う。
 * <ul>
 *   <li>{@code commonmodal:form} チャネルの受信ハンドラ登録</li>
 *   <li>{@code commonmodal:response} チャネルの送信</li>
 * </ul>
 * Fabric Loom の splitEnvironment においてクライアント専用ソースセットに配置。
 */
public final class ClientNetworking {

    /** 受信: サーバー → クライアントのフォーム表示要求ペイロード。 */
    public record FormCustomPayload(String json) implements CustomPayload {
        public static final CustomPayload.Id<FormCustomPayload> ID =
                new CustomPayload.Id<>(Identifier.of(CommonModalChannels.FORM));
        public static final PacketCodec<RegistryByteBuf, FormCustomPayload> CODEC =
                PacketCodec.of(
                        (value, buf) -> writeUtf8(buf, value.json()),
                        buf -> new FormCustomPayload(readUtf8(buf)));

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** 送信: クライアント → サーバーの応答ペイロード。 */
    public record ResponseCustomPayload(String json) implements CustomPayload {
        public static final CustomPayload.Id<ResponseCustomPayload> ID =
                new CustomPayload.Id<>(Identifier.of(CommonModalChannels.RESPONSE));
        public static final PacketCodec<RegistryByteBuf, ResponseCustomPayload> CODEC =
                PacketCodec.of(
                        (value, buf) -> writeUtf8(buf, value.json()),
                        buf -> new ResponseCustomPayload(readUtf8(buf)));

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private ClientNetworking() {
    }

    /** クライアント初期化時に呼び出し、チャネル登録と受信ハンドラを設定する。 */
    public static void register(FormReceiver receiver) {
        // ペイロード型の登録 (受信 S2C / 送信 C2S)
        PayloadTypeRegistry.playS2C().register(FormCustomPayload.ID, FormCustomPayload.CODEC);

        // 受信ハンドラ
        ClientPlayNetworking.registerGlobalReceiver(FormCustomPayload.ID, (payload, context) -> {
            try {
                FormPayload formPayload = FormCodec.decodeForm(payload.json());
                context.client().execute(() -> receiver.onFormReceived(formPayload));
            } catch (Exception e) {
                System.err.println("[commonModal] Failed to decode form payload: " + e.getMessage());
            }
        });
    }

    /** {@code commonmodal:response} チャンネルでサーバーへ応答を送信する。 */
    public static void sendResponse(ResponsePayload payload) {
        String json = FormCodec.encodeResponse(payload);
        ClientPlayNetworking.send(new ResponseCustomPayload(json));
    }

    /** フォーム受信時に呼び出されるコールバック。 */
    @FunctionalInterface
    public interface FormReceiver {
        void onFormReceived(FormPayload payload);
    }

    // ---- エンコーディングヘルパー ----

    private static void writeUtf8(RegistryByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    private static String readUtf8(RegistryByteBuf buf) {
        int length = buf.readVarInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
