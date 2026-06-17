package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.ModalForm;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * 2択ダイアログ (Modal Form) を描画する {@link Screen}。
 * <p>
 * plan.md §4.1 に基づく。中央にタイトル・説明文を表示し、下部に 2 つのボタンを
 * 横並びに配置する。いずれかのボタン押下、または Esc で画面を閉じた時点で
 * 結果をサーバーへ返却する。
 */
public final class ModalFormScreen extends Screen {
    private final int formId;
    private final ModalForm form;
    private final CommonModalScreens.FormCallbacks callbacks;

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING = 10;

    public ModalFormScreen(int formId, Form form, CommonModalScreens.FormCallbacks callbacks) {
        super(Text.literal(form.getTitle()));
        this.formId = formId;
        this.form = (ModalForm) form;
        this.callbacks = callbacks;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int bottomY = this.height / 2 + 40;

        this.addDrawableChild(ButtonWidget.builder(Text.literal(form.getButton1()), btn -> finish(true))
                .dimensions(centerX - BUTTON_WIDTH - PADDING / 2, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal(form.getButton2()), btn -> finish(false))
                .dimensions(centerX + PADDING / 2, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int textY = this.height / 2 - 40;

        // タイトル
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, textY, 0xFFFFFF);

        // 説明文（複数行に分けて描画）
        textY += 25;
        for (String line : wrapText(form.getContent(), 260)) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(line), centerX, textY, 0xAAAAAA);
            textY += this.textRenderer.fontHeight + 2;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        finish(null);
    }

    private void finish(Boolean value) {
        if (this.client == null) {
            return;
        }
        this.client.setScreen(null);
        callbacks.onResult(formId, value);
    }

    private java.util.List<String> wrapText(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
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
}
