package dev.acecore.commonmodal.api.form.component;

/**
 * Custom Form に配置可能な入力/表示コンポーネントの基準インターフェース。
 * <p>
 * plan.md §4.3 に基づく。各実装は {@link #getType()} で識別子を返す。
 * Minecraft 非依存の純粋なデータ定義とする。
 */
public interface FormComponent {
    /** コンポーネント種別識別子 ({@code "label"}, {@code "toggle"}, {@code "slider"}, {@code "dropdown"}, {@code "input"})。 */
    String getType();
}