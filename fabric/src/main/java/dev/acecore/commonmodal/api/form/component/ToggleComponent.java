package dev.acecore.commonmodal.api.form.component;

import java.util.Objects;

/**
 * ON/OFF を切り替えるトグルスイッチ ({@code type: "toggle"})。
 * <p>
 * plan.md §4.3 に基づく。{@code default} は初期状態。
 */
public final class ToggleComponent implements FormComponent {
    private final String text;
    private final boolean defaultValue;

    public ToggleComponent(String text, boolean defaultValue) {
        this.text = Objects.requireNonNullElse(text, "");
        this.defaultValue = defaultValue;
    }

    @Override
    public String getType() {
        return "toggle";
    }

    public String getText() {
        return text;
    }

    public boolean isDefault() {
        return defaultValue;
    }
}