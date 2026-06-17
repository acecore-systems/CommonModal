package dev.acecore.commonmodal.api.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ボタンリスト (Simple Form) の不変データモデル。
 * <p>
 * plan.md §4.2 / plugin-spec.md §4.3 に基づく。
 * 各ボタンには任意で画像アイコン ({@link ButtonImage}) を付与できる。
 */
public final class SimpleForm implements Form {
    private final String title;
    private final String content;
    private final List<Button> buttons;

    private SimpleForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.content = Objects.requireNonNullElse(builder.content, "");
        this.buttons = Collections.unmodifiableList(new ArrayList<>(builder.buttons));
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getType() {
        return "simple";
    }

    public String getContent() {
        return content;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Simple Form の各ボタン定義。 */
    public static final class Button {
        private final String text;
        private final ButtonImage image;

        public Button(String text, ButtonImage image) {
            this.text = Objects.requireNonNullElse(text, "");
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public ButtonImage getImage() {
            return image;
        }
    }

    /** ボタンアイコン定義 ({@code type: "url"|"path"}, {@code data: ...})。 */
    public static final class ButtonImage {
        private final String type;
        private final String data;

        public ButtonImage(String type, String data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }
    }

    public static class Builder {
        private String title = "";
        private String content = "";
        private final List<Button> buttons = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder button(String text) {
            this.buttons.add(new Button(text, null));
            return this;
        }

        public Builder button(String text, ButtonImage image) {
            this.buttons.add(new Button(text, image));
            return this;
        }

        public Builder buttons(List<Button> buttons) {
            this.buttons.clear();
            this.buttons.addAll(buttons);
            return this;
        }

        public SimpleForm build() {
            return new SimpleForm(this);
        }
    }
}