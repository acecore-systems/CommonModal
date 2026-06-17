package dev.acecore.common_modal.api.form;

import dev.acecore.common_modal.api.CommonModalAPI;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 2択ダイアログ (Modal Form)。
 *
 * <p>plugin-spec.md §4.2 / plan.md §4.1 / Cumulus API に基づく。
 * 既存の Cumulus コードからの移行を容易にするため、
 * {@code import} 文の差し替えのみでコンパイルできる構成を提供する。</p>
 *
 * <p>ビルダーメソッド ({@link #button1(String)}, {@link #button2(String)}) は
 * Cumulus の {@code ModalForm.builder()} と同名。レスポンス受け取りには
 * {@link #validResultHandler(BiConsumer)} または
 * {@link #closedResultHandler(Consumer)} を利用する。</p>
 */
public final class ModalForm implements Form {
    private final String title;
    private final String content;
    private final String button1;
    private final String button2;
    private final BiConsumer<Player, Boolean> resultHandler;
    private final Consumer<Player> closedHandler;

    private ModalForm(Builder builder) {
        this.title = Objects.requireNonNullElse(builder.title, "");
        this.content = Objects.requireNonNullElse(builder.content, "");
        this.button1 = Objects.requireNonNullElse(builder.button1, "");
        this.button2 = Objects.requireNonNullElse(builder.button2, "");
        this.resultHandler = builder.resultHandler;
        this.closedHandler = builder.closedHandler;
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

    public BiConsumer<Player, Boolean> getResultHandler() {
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
        private String content = "";
        private String button1 = "";
        private String button2 = "";
        private BiConsumer<Player, Boolean> resultHandler = (p, r) -> {};
        private Consumer<Player> closedHandler = p -> {};

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

        /**
         * いずれかのボタンが押されたとき (true=button1, false=button2) のハンドラ。
         *
         * <p>Cumulus 互換。キャンセル (ウィンドウを閉じた) 場合は呼ばれない。</p>
         */
        public Builder validResultHandler(BiConsumer<Player, Boolean> handler) {
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

        public ModalForm build() {
            return new ModalForm(this);
        }
    }
}
