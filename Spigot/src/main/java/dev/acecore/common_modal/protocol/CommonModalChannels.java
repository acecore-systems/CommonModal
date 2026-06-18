package dev.acecore.common_modal.protocol;

/**
 * commonModal が使用する Plugin Message Channel 識別子。
 *
 * <p>plan.md §4 / DECISIONS.md §2.2 に基づく。</p>
 */
public final class CommonModalChannels {
    /** サーバー → クライアント: フォーム表示要求。 */
    public static final String FORM = "commonmodal:form";
    /** クライアント → サーバー: フォーム応答。 */
    public static final String RESPONSE = "commonmodal:response";
    /** サーバー → クライアント: 導入チェック要求。 */
    public static final String CHECK = "commonmodal:check";
    /** クライアント → サーバー: 導入チェック応答（API バージョン）。 */
    public static final String CHECK_RESPONSE = "commonmodal:check_response";

    private CommonModalChannels() {
        throw new UnsupportedOperationException("constants");
    }
}
