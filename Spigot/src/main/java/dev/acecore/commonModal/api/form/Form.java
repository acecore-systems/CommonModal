package dev.acecore.commonmodal.api.form;

import org.bukkit.entity.Player;

/**
 * 各フォーム種別の基準インターフェース。
 *
 * <p>plugin-spec.md §4.1 / DECISIONS.md §4.1 に基づく。
 * Spigot 版ではサーバーからプレイヤーへ直接送信するため、{@link #send(Player)} を追加する。
 * このシグネチャにより、Cumulus の {@code form.sendPlayer(player)} に近い呼び出しを可能にする。</p>
 */
public interface Form {
    /** フォームのタイトルを返す。 */
    String getTitle();

    /** フォーム種別識別子 ({@code "modal"}, {@code "simple"}, {@code "custom"}) を返す。 */
    String getType();

    /**
     * フォームを指定したプレイヤーに送信する。
     *
     * @param player 対象プレイヤー
     */
    void send(Player player);
}
