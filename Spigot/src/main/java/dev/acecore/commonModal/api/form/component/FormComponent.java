package dev.acecore.commonmodal.api.form.component;

/**
 * Custom Form に配置可能な入力/表示コンポーネントの基準インターフェース。
 *
 * <p>plan.md §4.3 に基づく。Fabric 側と同一のパッケージ・シグネチャを持ち、
 * いずれ common モジュールに集約可能。</p>
 */
public interface FormComponent {
    /** コンポーネント種別識別子 ({@code "label"}, {@code "toggle"}, {@code "slider"}, {@code "dropdown"}, {@code "input"})。 */
    String getType();
}
