package dev.acecore.common_modal.api.form.component;

import java.util.Objects;

/** 表示専用テキストラベル。 */
public final class LabelComponent implements FormComponent {
    private final String text;

    public LabelComponent(String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    @Override
    public String getType() {
        return "label";
    }

    public String getText() {
        return text;
    }
}
