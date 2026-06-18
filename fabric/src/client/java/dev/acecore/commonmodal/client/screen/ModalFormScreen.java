package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.ModalForm;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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
        super(Component.literal(form.getTitle()));
        this.formId = formId;
        this.form = (ModalForm) form;
        this.callbacks = callbacks;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int bottomY = this.height / 2 + 40;

        this.addRenderableWidget(Button.builder(Component.literal(form.getButton1()), btn -> finish(true))
                .bounds(centerX - BUTTON_WIDTH - PADDING / 2, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal(form.getButton2()), btn -> finish(false))
                .bounds(centerX + PADDING / 2, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int textY = this.height / 2 - 40;

        // タイトル
        graphics.centeredText(this.font, this.title, centerX, textY, 0xFFFFFF);

        // 説明文（複数行に分けて描画）
        textY += 25;
        for (String line : wrapText(form.getContent(), 260)) {
            graphics.centeredText(this.font, Component.literal(line), centerX, textY, 0xAAAAAA);
            textY += this.font.lineHeight + 2;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        finish(null);
    }

    private void finish(Boolean value) {
        if (this.minecraft == null) {
            return;
        }
        this.minecraft.setScreen(null);
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
            if (this.font.width(test) > maxWidth && !current.isEmpty()) {
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
