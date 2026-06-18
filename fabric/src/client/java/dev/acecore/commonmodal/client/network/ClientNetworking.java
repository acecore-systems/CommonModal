package dev.acecore.commonmodal.client.network;

import com.google.gson.JsonObject;
import dev.acecore.commonmodal.protocol.CommonModalChannels;
import dev.acecore.commonmodal.protocol.FormCodec;
import dev.acecore.commonmodal.protocol.FormPayload;
import dev.acecore.commonmodal.protocol.ResponsePayload;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

public final class ClientNetworking {

    // 26.xのマッピングに準拠した基本的なByteBuf用Codec
    private static final StreamCodec<ByteBuf, String> UTF8_STRING_CODEC = StreamCodec.ofMember(
            (value, buf) -> {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                ByteBufCodecs.VAR_INT.encode(buf, bytes.length);
                buf.writeBytes(bytes);
            },
            buf -> {
                int length = ByteBufCodecs.VAR_INT.decode(buf);
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            }
    );

    /** 受信: サーバー → クライアントのフォーム表示要求ペイロード。 */
    public record FormCustomPayload(String json) implements CustomPacketPayload {
        public static final Identifier PAYLOAD_ID =
                Identifier.fromNamespaceAndPath(CommonModalChannels.NAMESPACE, CommonModalChannels.FORM_PATH);

        public static final CustomPacketPayload.Type<FormCustomPayload> TYPE =
                new CustomPacketPayload.Type<>(PAYLOAD_ID);

        // 🟢 ByteBufCodecs.STRING を使用（デフォルトでVarIntの長さプレフィックスが付きます）
        public static final StreamCodec<RegistryFriendlyByteBuf, FormCustomPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.stringUtf8(32767),
                        FormCustomPayload::json,
                        FormCustomPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 送信: クライアント → サーバーの応答ペイロード。 */
    public record ResponseCustomPayload(String json) implements CustomPacketPayload {
        public static final Identifier PAYLOAD_ID =
                Identifier.fromNamespaceAndPath(CommonModalChannels.NAMESPACE, CommonModalChannels.RESPONSE_PATH);

        public static final CustomPacketPayload.Type<ResponseCustomPayload> TYPE =
                new CustomPacketPayload.Type<>(PAYLOAD_ID);

        // 🟢 こちらも同様に修正
        public static final StreamCodec<RegistryFriendlyByteBuf, ResponseCustomPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.stringUtf8(32767),
                        ResponseCustomPayload::json,
                        ResponseCustomPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 受信: サーバー → クライアントの導入チェック要求ペイロード。 */
    public record CheckCustomPayload(String json) implements CustomPacketPayload {
        public static final Identifier PAYLOAD_ID =
                Identifier.fromNamespaceAndPath(CommonModalChannels.NAMESPACE, CommonModalChannels.CHECK_PATH);

        public static final CustomPacketPayload.Type<CheckCustomPayload> TYPE =
                new CustomPacketPayload.Type<>(PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, CheckCustomPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.stringUtf8(32767),
                        CheckCustomPayload::json,
                        CheckCustomPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 送信: クライアント → サーバーの導入チェック応答ペイロード。 */
    public record CheckResponseCustomPayload(String json) implements CustomPacketPayload {
        public static final Identifier PAYLOAD_ID =
                Identifier.fromNamespaceAndPath(CommonModalChannels.NAMESPACE, CommonModalChannels.CHECK_RESPONSE_PATH);

        public static final CustomPacketPayload.Type<CheckResponseCustomPayload> TYPE =
                new CustomPacketPayload.Type<>(PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, CheckResponseCustomPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.stringUtf8(32767),
                        CheckResponseCustomPayload::json,
                        CheckResponseCustomPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private ClientNetworking() {
    }

    /** クライアント初期化時に呼び出し、チャネル登録と受信ハンドラを設定する。 */
    /** クライアント初期化時に呼び出し、チャネル登録と受信ハンドラを設定する。 */
    public static void register(FormReceiver receiver) {
        // 公式ドキュメントに基づき、最新のメソッド名に修正
        PayloadTypeRegistry.clientboundPlay().register(FormCustomPayload.TYPE, FormCustomPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ResponseCustomPayload.TYPE, ResponseCustomPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CheckCustomPayload.TYPE, CheckCustomPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CheckResponseCustomPayload.TYPE, CheckResponseCustomPayload.CODEC);

        // 受信ハンドラ
        ClientPlayNetworking.registerGlobalReceiver(FormCustomPayload.TYPE, (payload, context) -> {
            try {
                FormPayload formPayload = FormCodec.decodeForm(payload.json());
                context.client().execute(() -> receiver.onFormReceived(formPayload));
            } catch (Exception e) {
                System.err.println("[commonModal] Failed to decode form payload: " + e.getMessage());
            }
        });

        // 導入チェック要求受信ハンドラ
        ClientPlayNetworking.registerGlobalReceiver(CheckCustomPayload.TYPE, (payload, context) -> {
            context.client().execute(ClientNetworking::sendCheckResponse);
        });
    }

    /** サーバーへ応答を送信する。 */
    public static void sendResponse(ResponsePayload payload) {
        String json = FormCodec.encodeResponse(payload);
        ClientPlayNetworking.send(new ResponseCustomPayload(json));
    }

    /** サーバーへ導入チェック応答（API バージョン 1）を送信する。 */
    public static void sendCheckResponse() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        ClientPlayNetworking.send(new CheckResponseCustomPayload(root.toString()));
    }

    /** フォーム受信時に呼び出されるコールバック。 */
    @FunctionalInterface
    public interface FormReceiver {
        void onFormReceived(FormPayload payload);
    }
}