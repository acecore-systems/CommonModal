package dev.acecore.commonmodal.api.form;

import java.util.Objects;

/**
 * 2択ダイアログ (Modal Form) の不変データモデル。
 * <p>
 * plan.md §4.1 / plugin-spec.md §4.2 に基づく。
 * サーバー → クライアントへ送信される {@code form} オブジェクトの Java 表現であり、
 * Minecraft 非依存の純粋な POJO とする (Gson 等で直接シリアライズ可能)。
 */
public final class ModalForm implements Form {
    private final String title;
    private final String content;
    private final String button1;
    private final String button2;

    private ModalForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.content = Objects.requireNonNullElse(builder.content, "");
        this.button1 = Objects.requireNonNullElse(builder.button1, "");
        this.button2 = Objects.requireNonNullElse(builder.button2, "");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getType() {
        return "modal";
    }

    public String getContent() {
        return content;
    }

    public String getButton1() {
        return button1;
    }

    public String getButton2() {
        return button2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title = "";
        private String content = "";
        private String button1 = "";
        private String button2 = "";

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder button1(String button1) {
            this.button1 = button1;
            return this;
        }

        public Builder button2(String button2) {
            this.button2 = button2;
            return this;
        }

        public ModalForm build() {
            return new ModalForm(this);
        }
    }
}