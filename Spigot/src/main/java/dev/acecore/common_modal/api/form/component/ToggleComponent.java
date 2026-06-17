package dev.acecore.common_modal.api.form.component;

import java.util.Objects;

/** ON/OFF を切り替えるトグルスイッチ ({@code type: "toggle"})。 */
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
