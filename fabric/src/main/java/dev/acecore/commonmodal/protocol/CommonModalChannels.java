package dev.acecore.commonmodal.protocol;

/**
 * commonModal が使用する Plugin Message Channel 識別子。
 * <p>
 * plan.md §4 に基づく。サーバー → クライアントは {@code commonmodal:form}、
 * クライアント → サーバーは {@code commonmodal:response}。
 * <p>
 * Minecraft / Fabric 非依存の純粋な定数定義。
 */
public final class CommonModalChannels {
    /** サーバー → クライアント: フォーム表示要求。 */
    public static final String FORM = "commonmodal:form";
    /** クライアント → サーバー: フォーム応答。 */
    public static final String RESPONSE = "commonmodal:response";

    private CommonModalChannels() {
        throw new UnsupportedOperationException("constants");
    }
}