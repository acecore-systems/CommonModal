package dev.acecore.commonmodal.api.form;

import dev.acecore.commonmodal.api.CommonModalAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * ボタンリスト (Simple Form)。
 *
 * <p>plugin-spec.md §4.3 / plan.md §4.2 / Cumulus API に基づく。
 * 各ボタンには {@link FormImage} によるアイコン画像を添付可能。</p>
 */
public final class SimpleForm implements Form {
    private final String title;
    private final String content;
    private final List<Button> buttons;
    private final BiConsumer<Player, Integer> resultHandler;
    private final Consumer<Player> closedHandler;

    private SimpleForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.content = Objects.requireNonNullElse(builder.content, "");
        this.buttons = Collections.unmodifiableList(new ArrayList<>(builder.buttons));
        this.resultHandler = builder.resultHandler;
        this.closedHandler = builder.closedHandler;
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

    public BiConsumer<Player, Integer> getResultHandler() {
        return resultHandler;
    }

    public Consumer<Player> getClosedHandler() {
        return closedHandler;
    }

    @Override
    public void send(Player player) {
        CommonModalAPI.sendForm(player, this);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Simple Form のボタン定義。 */
    public static final class Button {
        private final String text;
        private final FormImage image;

        public Button(String text, FormImage image) {
            this.text = Objects.requireNonNullElse(text, "");
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public FormImage getImage() {
            return image;
        }
    }

    public static final class Builder {
        private String title = "";
        private String content = "";
        private final List<Button> buttons = new ArrayList<>();
        private BiConsumer<Player, Integer> resultHandler = (p, r) -> {};
        private Consumer<Player> closedHandler = p -> {};

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

        public Builder button(String text, FormImage image) {
            this.buttons.add(new Button(text, image));
            return this;
        }

        /** Cumulus 互換: {@link FormImage.Type#URL} 簡易指定。 */
        public Builder button(String text, FormImage.Type type, String data) {
            this.buttons.add(new Button(text, new FormImage(type, data)));
            return this;
        }

        /**
         * ボタンがクリックされたときのハンドラ (引数は 0 始まりのインデックス)。
         *
         * <p>Cumulus 互換 {@code validResultHandler} 相当。</p>
         */
        public Builder validResultHandler(BiConsumer<Player, Integer> handler) {
            this.resultHandler = handler != null ? handler : (p, r) -> {};
            return this;
        }

        /**
         * ウィンドウが閉じられた (Esc/キャンセル) 場合のハンドラ。
         */
        public Builder closedResultHandler(Consumer<Player> handler) {
            this.closedHandler = handler != null ? handler : p -> {};
            return this;
        }

        public SimpleForm build() {
            return new SimpleForm(this);
        }
    }
}
