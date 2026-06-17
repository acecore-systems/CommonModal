package dev.acecore.commonmodal.api.form.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 選択肢配列から1つを選ぶドロップダウン ({@code type: "dropdown"})。
 * <p>
 * plan.md §4.3 に基づく。{@code options} は選択肢文字列リスト、
 * {@code default} は初期選択インデックス (0 始まり)。
 * 左右矢印による切り替え (StepSlider 相当) も本コンポーネントで兼ねる
 * (詳細は {@code DECISIONS.md} 参照)。
 */
public final class DropdownComponent implements FormComponent {
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