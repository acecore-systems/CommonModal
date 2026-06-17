package dev.acecore.commonmodal.protocol;

/**
 * {@code commonmodal:response} チャネルのペイロードモデル。
 * <p>
 * plan.md §4.B に基づき、{@code id} と {@code value} を保持する。
 * {@code value} はフォーム種別に依存し、キャンセル時は {@code null}。
 * Minecraft 非依存の純粋なデータモデル。
 */
public final class ResponsePayload {
    private final int id;
    private final Object value;

    public ResponsePayload(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }
}