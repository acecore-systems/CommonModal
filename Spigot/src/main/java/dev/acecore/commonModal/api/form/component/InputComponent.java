package dev.acecore.commonmodal.api.form.component;

import java.util.Objects;

/** 自由入力テキストボックス ({@code type: "input"})。 */
public final class InputComponent implements FormComponent {
    private final String text;
    private final String placeholder;
    private final String defaultValue;

    public InputComponent(String text, String placeholder, String defaultValue) {
        this.text = Objects.requireNonNullElse(text, "");
        this.placeholder = Objects.requireNonNullElse(placeholder, "");
        this.defaultValue = Objects.requireNonNullElse(defaultValue, "");
    }

    @Override
    public String getType() {
        return "input";
    }

    public String getText() {
        return text;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getDefault() {
        return defaultValue;
    }
}
