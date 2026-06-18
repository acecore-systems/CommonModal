package dev.acecore.commonmodal.protocol;

/**
 * commonModal が使用する Plugin Message Channel 識別子。
 * <p>
 * plan.md §4 に基づく。サーバー → クライアントは {@code commonmodal:form}、
 * クライアント → サーバーは {@code commonmodal:response}。
 * <p>
 * Minecraft / Fabric 非依存の純粋な定数定義。パス部文字列は
 * {@link net.minecraft.resources.ResourceLocation} と組み合わせて利用する。
 */
public final class CommonModalChannels {
    public static final String NAMESPACE = "commonmodal";

    /** サーバー → クライアント: フォーム表示要求。 */
    public static final String FORM_PATH = "form";
    public static final String FORM = NAMESPACE + ":" + FORM_PATH;

    /** クライアント → サーバー: フォーム応答。 */
    public static final String RESPONSE_PATH = "response";
    public static final String RESPONSE = NAMESPACE + ":" + RESPONSE_PATH;

    private CommonModalChannels() {
        throw new UnsupportedOperationException("constants");
    }
}
