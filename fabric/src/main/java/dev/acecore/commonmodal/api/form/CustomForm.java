package dev.acecore.commonmodal.api.form;

import dev.acecore.commonmodal.api.form.component.DropdownComponent;
import dev.acecore.commonmodal.api.form.component.FormComponent;
import dev.acecore.commonmodal.api.form.component.InputComponent;
import dev.acecore.commonmodal.api.form.component.LabelComponent;
import dev.acecore.commonmodal.api.form.component.SliderComponent;
import dev.acecore.commonmodal.api.form.component.ToggleComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 複合入力フォーム (Custom Form) の不変データモデル。
 * <p>
 * plan.md §4.3 / plugin-spec.md §4 に基づく。
 * 縦に並ぶ複数の {@link FormComponent} を内包する。
 */
public final class CustomForm implements Form {
    private final String title;
    private final List<FormComponent> components;

    private CustomForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.components = Collections.unmodifiableList(new ArrayList<>(builder.components));
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getType() {
        return "custom";
    }

    public List<FormComponent> getComponents() {
        return components;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title = "";
        private final List<FormComponent> components = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder component(FormComponent component) {
            this.components.add(component);
            return this;
        }

        public Builder label(String text) {
            return component(new LabelComponent(text));
        }

        public Builder input(String text, String placeholder, String defaultValue) {
            return component(new InputComponent(text, placeholder, defaultValue));
        }

        public Builder toggle(String text, boolean defaultValue) {
            return component(new ToggleComponent(text, defaultValue));
        }

        public Builder slider(String text, double min, double max, double step, double defaultValue) {
            return component(new SliderComponent(text, min, max, step, defaultValue));
        }

        public Builder dropdown(String text, List<String> options, int defaultIndex) {
            return component(new DropdownComponent(text, options, defaultIndex));
        }

        public Builder components(List<FormComponent> components) {
            this.components.clear();
            this.components.addAll(components);
            return this;
        }

        public CustomForm build() {
            return new CustomForm(this);
        }
    }
}