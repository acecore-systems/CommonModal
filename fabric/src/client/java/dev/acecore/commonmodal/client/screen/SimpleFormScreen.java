package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.SimpleForm;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ボタンリスト (Simple Form) を描画する {@link Screen}。
 * <p>
 * plan.md §4.2 に基づく。画面上部にタイトル・コンテンツを表示し、その下に
 * 縦一列のボタンを配置。ボタン数が多い場合はスクロール対応する。
 * 各ボタンにはオプションで画像アイコンを左側に表示する。
 */
public final class SimpleFormScreen extends Screen {
    private final int formId;
    private final SimpleForm form;
    private final CommonModalScreens.FormCallbacks callbacks;
    private final List<ButtonState> buttonStates = new ArrayList<>();

    private static final int CONTENT_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 24;
    private static final int ICON_SIZE = 16;
    private static final int ICON_PADDING = 4;

    private int contentTop;
    private int contentBottom;
    private double scroll = 0.0;
    private int maxScroll = 0;
    private boolean dragging = false;

    public SimpleFormScreen(int formId, Form form, CommonModalScreens.FormCallbacks callbacks) {
        super(Text.literal(form.getTitle()));
        this.formId = formId;
        this.form = (SimpleForm) form;
        this.callbacks = callbacks;
    }

    @Override
    protected void init() {
        buttonStates.clear();
        List<SimpleForm.Button> buttons = form.getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            int index = i;
            SimpleForm.Button button = buttons.get(i);
            CompletableFuture<Identifier> imageFuture = dev.acecore.commonmodal.client.image.FormImageLoader.load(button.getImage());
            buttonStates.add(new ButtonState(index, button, imageFuture));
        }

        this.contentTop = 50;
        this.contentBottom = this.height - 40;
        recalculateLayout();
    }

    private void recalculateLayout() {
        int visibleHeight = contentBottom - contentTop;
        int totalHeight = Math.max(0, form.getButtons().size() * (BUTTON_HEIGHT + 4) - 4);
        maxScroll = Math.max(0, totalHeight - visibleHeight);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        recalculateLayout();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // タイトル
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // コンテンツ
        int y = contentTop - 20;
        if (form.getContent() != null && !form.getContent().isEmpty()) {
            for (String line : wrapText(form.getContent(), CONTENT_WIDTH)) {
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(line), this.width / 2, y, 0xAAAAAA);
                y += this.textRenderer.fontHeight + 2;
            }
        }

        // ボタンエリアのクリッピング表示
        int startX = (this.width - CONTENT_WIDTH) / 2;
        int buttonY = contentTop - (int) scroll;

        for (ButtonState state : buttonStates) {
            boolean hovered = mouseX >= startX && mouseX <= startX + CONTENT_WIDTH
                    && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
            int color = hovered ? 0xFF_AA_AA_AA : 0xFF_88_88_88;

            // ボタン背景
            context.fill(startX, buttonY, startX + CONTENT_WIDTH, buttonY + BUTTON_HEIGHT, 0xFF_22_22_22);
            context.fill(startX + 1, buttonY + 1, startX + CONTENT_WIDTH - 1, buttonY + BUTTON_HEIGHT - 1, color);

            // アイコン
            int textX = startX + 8;
            Identifier icon = state.imageFuture.getNow(null);
            if (icon != null) {
                renderIcon(context, icon, startX + 4, buttonY + (BUTTON_HEIGHT - ICON_SIZE) / 2);
                textX = startX + 8 + ICON_SIZE + ICON_PADDING;
            }

            // テキスト
            String text = state.button.getText();
            int textWidth = this.textRenderer.getWidth(text);
            if (textWidth > CONTENT_WIDTH - (textX - startX) - 8) {
                text = truncateText(text, CONTENT_WIDTH - (textX - startX) - 12);
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal(text), textX,
                    buttonY + (BUTTON_HEIGHT - this.textRenderer.fontHeight) / 2, 0xFFFFFF);

            buttonY += BUTTON_HEIGHT + 4;
        }

        // スクロールバー
        if (maxScroll > 0) {
            int barHeight = Math.max(16, (contentBottom - contentTop) * (contentBottom - contentTop) / (maxScroll + (contentBottom - contentTop)));
            int barY = contentTop + (int) ((contentBottom - contentTop - barHeight) * scroll / (double) maxScroll);
            int barX = (this.width + CONTENT_WIDTH) / 2 + 4;
            context.fill(barX, contentTop, barX + 6, contentBottom, 0x44_00_00_00);
            context.fill(barX, barY, barX + 6, barY + barHeight, 0xFF_CC_CC_CC);
        }
    }

    private void renderIcon(DrawContext context, Identifier texture, int x, int y) {
        AbstractTexture textureObj = MinecraftClient.getInstance().getTextureManager().getTexture(texture);
        if (textureObj == null) {
            return;
        }
        context.drawTexture(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll > 0) {
            int barX = (this.width + CONTENT_WIDTH) / 2 + 4;
            int barHeight = Math.max(16, (contentBottom - contentTop) * (contentBottom - contentTop) / (maxScroll + (contentBottom - contentTop)));
            int barY = contentTop + (int) ((contentBottom - contentTop - barHeight) * scroll / maxScroll);
            if (mouseX >= barX && mouseX <= barX + 6 && mouseY >= barY && mouseY <= barY + barHeight) {
                dragging = true;
                return true;
            }
        }

        if (button == 0 && mouseY >= contentTop && mouseY <= contentBottom) {
            int startX = (this.width - CONTENT_WIDTH) / 2;
            int buttonY = contentTop - (int) scroll;
            for (ButtonState state : buttonStates) {
                if (mouseX >= startX && mouseX <= startX + CONTENT_WIDTH
                        && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT) {
                    finish(state.index);
                    return true;
                }
                buttonY += BUTTON_HEIGHT + 4;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && maxScroll > 0) {
            int barHeight = Math.max(16, (contentBottom - contentTop) * (contentBottom - contentTop) / (maxScroll + (contentBottom - contentTop)));
            int trackHeight = contentBottom - contentTop - barHeight;
            scroll = Math.clamp((mouseY - contentTop - barHeight / 2.0) * maxScroll / (double) trackHeight, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            scroll = Math.clamp(scroll - verticalAmount * BUTTON_HEIGHT, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        finish(null);
    }

    private void finish(Integer value) {
        if (this.client == null) {
            return;
        }
        this.client.setScreen(null);
        callbacks.onResult(formId, value);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (this.textRenderer.getWidth(test) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add(text);
        }
        return lines;
    }

    private String truncateText(String text, int maxWidth) {
        String ellipsis = "...";
        int max = maxWidth - this.textRenderer.getWidth(ellipsis);
        if (max <= 0) {
            return ellipsis;
        }
        int i = text.length();
        while (i > 0 && this.textRenderer.getWidth(text.substring(0, i)) > max) {
            i--;
        }
        return text.substring(0, i) + ellipsis;
    }

    private record ButtonState(int index, SimpleForm.Button button, CompletableFuture<Identifier> imageFuture) {
    }
}
