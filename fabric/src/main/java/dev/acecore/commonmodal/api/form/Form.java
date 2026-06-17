package dev.acecore.commonmodal.api.form;

/**
 * 各フォーム種別の基準インターフェース。
 * <p>
 * Spigot 側の {@code dev.acecore.commonmodal.api.form.Form} と同じシグネチャを持ち、
 * 将来的に Fabric サーバー側 (dedicated) でも同一の Form 定義を扱えるようにするための土台。
 * このインターフェース自体は Minecraft / Fabric のクラスに依存しない純粋な定義とする。
 */
public interface Form {
    /** フォームのタイトルを返す。 */
    String getTitle();

    /** フォーム種別識別子 ({@code "modal"}, {@code "simple"}, {@code "custom"}) を返す。 */
    String getType();
}