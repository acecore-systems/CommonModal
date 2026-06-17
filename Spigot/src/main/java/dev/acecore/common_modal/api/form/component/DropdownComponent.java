package dev.acecore.common_modal.api.form.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 選択肢配列から 1 つを選ぶドロップダウン ({@code type: "dropdown"})。
 *
 * <p>plan.md §4.3 / DECISIONS.md §3.3.1 に基づく。Fabric 側では左右矢印 (StepSlider 的表現)
 * として描画されるが、サーバー側モデルは Cumulus の {@code dropdown} と同じ。</p>
 */
public final class DropdownComponent implements dev.acecore.common_modal.api.form.component.FormComponent {
    private final String text;
    private final List<String> options;
    private final int defaultIndex;

    public DropdownComponent(String text, List<String> options, int defaultIndex) {
        this.text = Objects.requireNonNullElse(text, "");
        this.options = Collections.unmodifiableList(new ArrayList<>(
                options == null ? Collections.emptyList() : options));
        this.defaultIndex = Math.max(0, Math.min(defaultIndex, Math.max(0, this.options.size() - 1)));
    }

    @Override
    public String getType() {
        return "dropdown";
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getDefaultIndex() {
        return defaultIndex;
    }
}
