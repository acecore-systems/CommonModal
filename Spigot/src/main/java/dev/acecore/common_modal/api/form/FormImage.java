package dev.acecore.common_modal.api.form;

/**
 * SimpleForm ボタンに添付できるアイコン画像定義。
 *
 * <p>Cumulus の {@code org.geysermc.cumulus.util.FormImage} と近い構造。
 * 既存コードからの移行を容易にするため、{@link Type} 列挙型も提供する。</p>
 */
public final class FormImage {
    private final Type type;
    private final String data;

    public FormImage(Type type, String data) {
        this.type = type != null ? type : Type.URL;
        this.data = data != null ? data : "";
    }

    public Type getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    /** 画像種別。 */
    public enum Type {
        /** 外部 URL から取得する画像。 */
        URL,
        /** リソースパック内のテクスチャパス。 */
        PATH
    }
}
