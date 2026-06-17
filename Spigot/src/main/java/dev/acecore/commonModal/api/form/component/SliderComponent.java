package dev.acecore.commonmodal.api.form.component;

import java.util.Objects;

/** 数値範囲を {@code step} 単位で動かすスライダー ({@code type: "slider"})。 */
public final class SliderComponent implements FormComponent {
    private final String text;
    private final double min;
    private final double max;
    private final double step;
    private final double defaultValue;

    public SliderComponent(String text, double min, double max, double step, double defaultValue) {
        this.text = Objects.requireNonNullElse(text, "");
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 1 : step;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getType() {
        return "slider";
    }

    public String getText() {
        return text;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public double getDefault() {
        return defaultValue;
    }
}
