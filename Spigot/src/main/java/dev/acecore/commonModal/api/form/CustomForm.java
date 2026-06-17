package dev.acecore.commonmodal.api.form;

import dev.acecore.commonmodal.api.CommonModalAPI;
import dev.acecore.commonmodal.api.form.component.DropdownComponent;
import dev.acecore.commonmodal.api.form.component.FormComponent;
import dev.acecore.commonmodal.api.form.component.InputComponent;
import dev.acecore.commonmodal.api.form.component.LabelComponent;
import dev.acecore.commonmodal.api.form.component.SliderComponent;
import dev.acecore.commonmodal.api.form.component.ToggleComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 複合入力フォーム (Custom Form)。
 *
 * <p>plugin-spec.md §4.4 / plan.md §4.3 / Cumulus API に基づく。
 * {@link FormComponent} を縦に並べた入力フォームを提供する。</p>
 */
public final class CustomForm implements Form {
    private final String title;
    private final List<FormComponent> components;
    private final BiConsumer<Player, List<Object>> resultHandler;
    private final Consumer<Player> closedHandler;

    private CustomForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.components = Collections.unmodifiableList(new ArrayList<>(builder.components));
        this.resultHandler = builder.resultHandler;
        this.closedHandler = builder.closedHandler;
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

    public BiConsumer<Player, List<Object>> getResultHandler() {
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

    public static final class Builder {
        private String title = "";
        private final List<FormComponent> components = new ArrayList<>();
        private BiConsumer<Player, List<Object>> resultHandler = (p, r) -> {};
        private Consumer<Player> closedHandler = p -> {};

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder component(FormComponent component) {
            this.components.add(Objects.requireNonNull(component));
            return this;
        }

        public Builder label(String text) {
            return component(new LabelComponent(text));
        }

        public Builder input(String text, String placeholder, String defaultValue) {
            return component(new InputComponent(text, placeholder, defaultValue));
        }

        /** Cumulus 互換: placeholder のみ指定。 */
        public Builder input(String text, String placeholder) {
            return input(text, placeholder, "");
        }

        /** Cumulus 互換: テキストのみ指定。 */
        public Builder input(String text) {
            return input(text, "", "");
        }

        public Builder toggle(String text, boolean defaultValue) {
            return component(new ToggleComponent(text, defaultValue));
        }

        /** Cumulus 互換: デフォルト OFF。 */
        public Builder toggle(String text) {
            return toggle(text, false);
        }

        public Builder slider(String text, double min, double max, double step, double defaultValue) {
            return component(new SliderComponent(text, min, max, step, defaultValue));
        }

        /** Cumulus 互換: step=1, default=min 指定。 */
        public Builder slider(String text, double min, double max) {
            return slider(text, min, max, 1, min);
        }

        /** Cumulus 互換: step=1 指定。 */
        public Builder slider(String text, double min, double max, double defaultValue) {
            return slider(text, min, max, 1, defaultValue);
        }

        public Builder dropdown(String text, List<String> options, int defaultIndex) {
            return component(new DropdownComponent(text, options, defaultIndex));
        }

        /** Cumulus 互換: 可変長で選択肢を渡す。 */
        public Builder dropdown(String text, String... options) {
            List<String> list = new ArrayList<>();
            Collections.addAll(list, options);
            return dropdown(text, list, 0);
        }

        /**
         * 送信ボタンが押され、各コンポーネントの値のリストが返されたときのハンドラ。
         *
         * <p>Cumulus 互換 {@code validResultHandler} 相当。</p>
         */
        public Builder validResultHandler(BiConsumer<Player, List<Object>> handler) {
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

        public CustomForm build() {
            return new CustomForm(this);
        }
    }
}
