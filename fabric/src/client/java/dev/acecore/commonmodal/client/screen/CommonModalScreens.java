package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.Form;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * 受信した {@link Form} に応じた {@link Screen} を生成するファクトリ。
 * <p>
 * plan.md §5 に基づき、3 種類のフォーム (modal / simple / custom) を
 * それぞれ専用の {@link Screen} 実装へ振り分ける。
 */
public final class CommonModalScreens {

    private CommonModalScreens() {
    }

    /**
     * フォーム種別に応じた画面を生成し、表示する。
     *
     * @param client    Minecraft クライアントインスタンス
     * @param formId    サーバー側で発行された一意のフォーム ID
     * @param form      表示対象のフォーム定義
     * @param callbacks 決定・キャンセル時の応答コールバック
     */
    public static void open(MinecraftClient client, int formId, Form form, FormCallbacks callbacks) {
        Screen screen = createScreen(formId, form, callbacks);
        client.setScreen(screen);
    }

    private static Screen createScreen(int formId, Form form, FormCallbacks callbacks) {
        return switch (form.getType()) {
            case "modal" -> new ModalFormScreen(formId, form, callbacks);
            case "simple" -> new SimpleFormScreen(formId, form, callbacks);
            case "custom" -> new CustomFormScreen(formId, form, callbacks);
            default -> throw new IllegalArgumentException("Unknown form type: " + form.getType());
        };
    }

    /**
     * フォーム画面からの結果を外部に通知するコールバック群。
     */
    @FunctionalInterface
    public interface FormCallbacks {
        /**
         * プレイヤーが操作を完了・キャンセルした際に呼ばれる。
         *
         * @param formId フォーム ID
         * @param value  決定時の値。キャンセル時は {@code null}
         */
        void onResult(int formId, Object value);
    }
}
