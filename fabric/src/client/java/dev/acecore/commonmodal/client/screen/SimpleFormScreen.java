package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.SimpleForm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        super(Component.literal(form.getTitle()));
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
    public void resize(int width, int height) {
        super.resize(width, height);
        recalculateLayout();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

        int y = contentTop - 20;
        if (form.getContent() != null && !form.getContent().isEmpty()) {
            for (String line : wrapText(form.getContent(), CONTENT_WIDTH)) {
                graphics.centeredText(this.font, Component.literal(line), this.width / 2, y, 0xFF_AA_AA_AA);
                y += this.font.lineHeight + 2;
            }
        }

        int startX = (this.width - CONTENT_WIDTH) / 2;
        int buttonY = contentTop - (int) scroll;

        for (ButtonState state : buttonStates) {
            boolean hovered = mouseX >= startX && mouseX <= startX + CONTENT_WIDTH
                    && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
            int color = hovered ? 0xFF_AA_AA_AA : 0xFF_88_88_88;

            graphics.fill(startX, buttonY, startX + CONTENT_WIDTH, buttonY + BUTTON_HEIGHT, 0xFF_22_22_22);
            graphics.fill(startX + 1, buttonY + 1, startX + CONTENT_WIDTH - 1, buttonY + BUTTON_HEIGHT - 1, color);

            int textX = startX + 8;
            Identifier icon = state.imageFuture.getNow(null);
            if (icon != null) {
                renderIcon(graphics, icon, startX + 4, buttonY + (BUTTON_HEIGHT - ICON_SIZE) / 2);
                textX = startX + 8 + ICON_SIZE + ICON_PADDING;
            }

            String text = state.button.getText();
            int textWidth = this.font.width(text);
            if (textWidth > CONTENT_WIDTH - (textX - startX) - 8) {
                text = truncateText(text, CONTENT_WIDTH - (textX - startX) - 12);
            }

            graphics.text(this.font, Component.literal(text), textX,
                    buttonY + (BUTTON_HEIGHT - this.font.lineHeight) / 2, 0xFFFFFFFF, true);

            buttonY += BUTTON_HEIGHT + 4;
        }

        if (maxScroll > 0) {
            int barHeight = Math.max(16, (contentBottom - contentTop) * (contentBottom - contentTop) / (maxScroll + (contentBottom - contentTop)));
            int barY = contentTop + (int) ((contentBottom - contentTop - barHeight) * scroll / (double) maxScroll);
            int barX = (this.width + CONTENT_WIDTH) / 2 + 4;
            graphics.fill(barX, contentTop, barX + 6, contentBottom, 0x44_00_00_00);
            graphics.fill(barX, barY, barX + 6, barY + barHeight, 0xFF_CC_CC_CC);
        }
    }

    private void renderIcon(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
        AbstractTexture textureObj = Minecraft.getInstance().getTextureManager().getTexture(texture);
        if (textureObj == null) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

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

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (dragging && maxScroll > 0) {
            int barHeight = Math.max(16, (contentBottom - contentTop) * (contentBottom - contentTop) / (maxScroll + (contentBottom - contentTop)));
            int trackHeight = contentBottom - contentTop - barHeight;
            scroll = Math.clamp((mouseY - contentTop - barHeight / 2.0) * maxScroll / (double) trackHeight, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // event.verticalAmount() の代わりに、引数の scrollY をそのまま使用します
        if (scrollY != 0) {
            // Mth.clamp は Mojmap での数値クランプクラス（バニラの算術ユーティリティ）です
            this.scroll = net.minecraft.util.Mth.clamp(this.scroll - scrollY * BUTTON_HEIGHT, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        finish(null);
    }

    private void finish(Integer value) {
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

    private String truncateText(String text, int maxWidth) {
        String ellipsis = "...";
        int max = maxWidth - this.font.width(ellipsis);
        if (max <= 0) {
            return ellipsis;
        }
        int i = text.length();
        while (i > 0 && this.font.width(text.substring(0, i)) > max) {
            i--;
        }
        return text.substring(0, i) + ellipsis;
    }

    private record ButtonState(int index, SimpleForm.Button button, CompletableFuture<Identifier> imageFuture) {
    }
}
