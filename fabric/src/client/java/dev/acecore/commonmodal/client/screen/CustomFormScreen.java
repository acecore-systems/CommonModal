package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.CustomForm;
import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.component.DropdownComponent;
import dev.acecore.commonmodal.api.form.component.FormComponent;
import dev.acecore.commonmodal.api.form.component.InputComponent;
import dev.acecore.commonmodal.api.form.component.LabelComponent;
import dev.acecore.commonmodal.api.form.component.SliderComponent;
import dev.acecore.commonmodal.api.form.component.ToggleComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.InputWithModifiers; // 追加
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * 複合入力フォーム (Custom Form) を描画する {@link Screen}。
 */
public final class CustomFormScreen extends Screen {
    private final int formId;
    private final CustomForm form;
    private final CommonModalScreens.FormCallbacks callbacks;
    private final List<ComponentState<?>> componentStates = new ArrayList<>();

    private static final int CONTENT_WIDTH = 260;
    private static final int COMPONENT_GAP = 8;
    private static final int FIELD_HEIGHT = 20;
    private static final int LABEL_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SUBMIT_HEIGHT = 20;

    private int contentTop;
    private int contentBottom;
    private double scroll = 0.0;
    private int maxScroll = 0;
    private boolean dragging = false;

    public CustomFormScreen(int formId, Form form, CommonModalScreens.FormCallbacks callbacks) {
        super(Component.literal(form.getTitle()));
        this.formId = formId;
        this.form = (CustomForm) form;
        this.callbacks = callbacks;
    }

    @Override
    protected void init() {
        contentTop = 40;
        contentBottom = this.height - 40;

        componentStates.clear();
        for (FormComponent component : form.getComponents()) {
            componentStates.add(createState(component));
        }

        int submitX = (this.width - CONTENT_WIDTH) / 2;
        int submitY = this.height - 32;
        this.addRenderableWidget(Button.builder(Component.literal("送信"), btn -> submit())
                .bounds(submitX, submitY, CONTENT_WIDTH, SUBMIT_HEIGHT)
                .build());

        recalculateLayout();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        init();
    }

    private void recalculateLayout() {
        int total = calculateTotalHeight();
        int visible = contentBottom - contentTop;
        maxScroll = Math.max(0, total - visible);
        scroll = Mth.clamp(scroll, 0.0, (double) maxScroll);
    }

    private int calculateTotalHeight() {
        int y = 0;
        for (ComponentState<?> state : componentStates) {
            y += state.labelHeight();
            y += state.height();
            y += COMPONENT_GAP;
        }
        return Math.max(y - COMPONENT_GAP, 0);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int startX = (this.width - CONTENT_WIDTH) / 2;
        int y = contentTop - (int) scroll;

        for (ComponentState<?> state : componentStates) {
            String label = state.label();
            if (label != null && !label.isEmpty()) {
                graphics.text(this.font, Component.literal(label), startX, y, 0xFFFFFF, true);
                y += this.font.lineHeight + LABEL_GAP;
            }

            state.render(graphics, startX, y, mouseX, mouseY, delta);
            y += state.height() + COMPONENT_GAP;
        }

        if (maxScroll > 0) {
            int visible = contentBottom - contentTop;
            int barHeight = Math.max(16, visible * visible / (maxScroll + visible));
            int barY = contentTop + (int) ((visible - barHeight) * scroll / maxScroll);
            int barX = startX + CONTENT_WIDTH + 6;
            graphics.fill(barX, contentTop, barX + SCROLLBAR_WIDTH, contentBottom, 0x44_00_00_00);
            graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF_CC_CC_CC);
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        for (ComponentState<?> state : componentStates) {
            state.onClearChildren();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && maxScroll > 0) {
            int startX = (this.width - CONTENT_WIDTH) / 2;
            int visible = contentBottom - contentTop;
            int barHeight = Math.max(16, visible * visible / (maxScroll + visible));
            int barY = contentTop + (int) ((visible - barHeight) * scroll / maxScroll);
            int barX = startX + CONTENT_WIDTH + 6;
            if (mouseX >= barX && mouseX <= barX + SCROLLBAR_WIDTH && mouseY >= barY && mouseY <= barY + barHeight) {
                dragging = true;
                return true;
            }
        }

        for (ComponentState<?> state : componentStates) {
            if (state.mouseClicked(event, doubleClick)) { // 引数を修正
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        for (ComponentState<?> state : componentStates) {
            state.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            scroll = Mth.clamp(scroll - verticalAmount * FIELD_HEIGHT, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        for (ComponentState<?> state : componentStates) {
            if (state.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (ComponentState<?> state : componentStates) {
            if (state.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        finish(null);
    }

    private void submit() {
        List<Object> values = new ArrayList<>();
        for (ComponentState<?> state : componentStates) {
            values.add(state.getValue());
        }
        finish(values);
    }

    private void finish(Object value) {
        if (this.minecraft == null) {
            return;
        }
        this.minecraft.setScreen(null);
        callbacks.onResult(formId, value);
    }

    ComponentState<?> createState(FormComponent component) {
        if (component instanceof LabelComponent c) {
            return new LabelState(c);
        }
        if (component instanceof InputComponent c) {
            return new InputState(c);
        }
        if (component instanceof ToggleComponent c) {
            return new ToggleState(c);
        }
        if (component instanceof SliderComponent c) {
            return new SliderState(c);
        }
        if (component instanceof DropdownComponent c) {
            return new DropdownState(c);
        }
        throw new IllegalArgumentException("Unknown component: " + component.getClass());
    }

    // ====== Component State Abstraction ======

    private abstract class ComponentState<T> {
        void onClearChildren() {}

        abstract String label();

        abstract int height();

        abstract int labelHeight();

        abstract void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick);

        abstract boolean mouseClicked(MouseButtonEvent event, boolean doubleClick); // 引数を変更

        void mouseReleased(MouseButtonEvent event) {}

        boolean keyPressed(KeyEvent event) {
            return false;
        }

        boolean charTyped(CharacterEvent event) {
            return false;
        }

        abstract T getValue();
    }

    private class LabelState extends ComponentState<String> {
        private final LabelComponent component;

        LabelState(LabelComponent component) {
            this.component = component;
        }

        @Override
        String label() {
            return component.getText();
        }

        @Override
        int height() {
            return 0;
        }

        @Override
        int labelHeight() {
            return CustomFormScreen.this.font.lineHeight + LABEL_GAP;
        }

        @Override
        void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick) {}

        @Override
        boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return false;
        }

        @Override
        String getValue() {
            return null;
        }
    }

    private class InputState extends ComponentState<String> {
        private final InputComponent component;
        private EditBox field;

        InputState(InputComponent component) {
            this.component = component;
        }

        @Override
        void onClearChildren() {
            if (field != null) {
                removeWidget(field);
            }
            field = null;
        }

        @Override
        String label() {
            return component.getText();
        }

        @Override
        int height() {
            return FIELD_HEIGHT;
        }

        @Override
        int labelHeight() {
            return component.getText().isEmpty() ? 0 : CustomFormScreen.this.font.lineHeight + LABEL_GAP;
        }

        @Override
        void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
            if (field == null) {
                field = new EditBox(CustomFormScreen.this.font, x, y, CONTENT_WIDTH, FIELD_HEIGHT, Component.literal(""));
                field.setHint(Component.literal(component.getPlaceholder()));
                field.setValue(component.getDefault());
                addRenderableWidget(field);
            } else {
                field.setX(x);
                field.setY(y);
            }
        }

        @Override
        boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (field != null) {
                if (field.isMouseOver(event.x(), event.y())) {
                    field.setFocused(true);
                    return field.mouseClicked(event, doubleClick); // 第2引数を追加
                } else {
                    field.setFocused(false);
                }
            }
            return false;
        }

        @Override
        boolean keyPressed(KeyEvent event) {
            return field != null && field.isFocused() && field.keyPressed(event);
        }

        @Override
        boolean charTyped(CharacterEvent event) {
            return field != null && field.isFocused() && field.charTyped(event);
        }

        @Override
        String getValue() {
            return field == null ? component.getDefault() : field.getValue();
        }
    }

    private class ToggleState extends ComponentState<Boolean> {
        private final ToggleComponent component;
        private boolean value;
        private AbstractButton widget;

        ToggleState(ToggleComponent component) {
            this.component = component;
            this.value = component.isDefault();
        }

        @Override
        void onClearChildren() {
            widget = null;
        }

        @Override
        String label() {
            return component.getText();
        }

        @Override
        int height() {
            return FIELD_HEIGHT;
        }

        @Override
        int labelHeight() {
            return 0;
        }

        @Override
        void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
            if (widget == null) {
                widget = new AbstractButton(x, y, FIELD_HEIGHT, FIELD_HEIGHT, Component.literal("")) {
                    @Override
                    public void onPress(InputWithModifiers modifiers) {
                        value = !value;
                    }

                    // ★ 追加: ナレーション更新メソッドの実装
                    @Override
                    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
                        // 必要に応じてナレーションを追加できますが、空でもコンパイルは通ります
                        this.defaultButtonNarrationText(narrationElementOutput);
                    }

                    @Override
                    protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
                        extractor.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF_33_33_33);
                        if (value) {
                            int pad = 4;
                            extractor.fill(getX() + pad, getY() + pad,
                                    getX() + getWidth() - pad, getY() + getHeight() - pad, 0xFF_55_FF_55);
                        }
                    }
                };
                addRenderableWidget(widget);
            }
        }

        @Override
        boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return widget != null && widget.mouseClicked(event, doubleClick); // 引数を修正
        }

        @Override
        Boolean getValue() {
            return value;
        }
    }

    private class SliderState extends ComponentState<Double> {
        private final SliderComponent component;
        private AbstractSliderButton slider;
        private double internalValue; // protectedの代わりに入力値を安全に保持する用

        SliderState(SliderComponent component) {
            this.component = component;
            this.internalValue = (component.getDefault() - component.getMin()) / (component.getMax() - component.getMin());
        }

        @Override
        void onClearChildren() {
            slider = null;
        }

        @Override
        String label() {
            return component.getText();
        }

        @Override
        int height() {
            return FIELD_HEIGHT;
        }

        @Override
        int labelHeight() {
            return CustomFormScreen.this.font.lineHeight + LABEL_GAP;
        }

        @Override
        void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
            if (slider == null) {
                slider = new AbstractSliderButton(x, y, CONTENT_WIDTH, FIELD_HEIGHT, Component.literal(""), internalValue) {
                    @Override
                    protected void updateMessage() {
                        // 自身の内部のvalue変数を渡しメッセージを更新
                        setMessage(Component.literal(formatValue(this.value)));
                    }

                    @Override
                    protected void applyValue() {
                        // スライダーが動いたとき、protectedの値を安全に退避
                        SliderState.this.internalValue = this.value;
                    }
                };
                addRenderableWidget(slider);
            }
        }

        @Override
        boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return slider != null && slider.mouseClicked(event, doubleClick); // 引数を修正
        }

        @Override
        void mouseReleased(MouseButtonEvent event) {
            if (slider != null) {
                slider.mouseReleased(event);
            }
        }

        @Override
        Double getValue() {
            double range = component.getMax() - component.getMin();
            // 直接 slider.value を見ず、内部に同期しておいた値を計算に使用
            double raw = component.getMin() + internalValue * range;
            double step = component.getStep();
            if (step > 0) {
                raw = Math.round((raw - component.getMin()) / step) * step + component.getMin();
            }
            return raw;
        }

        private String formatValue(double value) {
            double v = component.getMin() + value * (component.getMax() - component.getMin());
            if (v == (long) v) {
                return String.valueOf((long) v);
            }
            return String.valueOf(v);
        }
    }

    private class DropdownState extends ComponentState<Integer> {
        private final DropdownComponent component;
        private int selected;
        private AbstractButton leftButton;
        private AbstractButton rightButton;

        DropdownState(DropdownComponent component) {
            this.component = component;
            this.selected = component.getDefaultIndex();
        }

        @Override
        void onClearChildren() {
            leftButton = null;
            rightButton = null;
        }

        @Override
        String label() {
            return component.getText();
        }

        @Override
        int height() {
            return FIELD_HEIGHT;
        }

        @Override
        int labelHeight() {
            return CustomFormScreen.this.font.lineHeight + LABEL_GAP;
        }

        @Override
        void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
            if (leftButton == null) {
                leftButton = new ArrowButton(x, y, FIELD_HEIGHT, FIELD_HEIGHT, false, this::previous);
                rightButton = new ArrowButton(x + CONTENT_WIDTH - FIELD_HEIGHT, y, FIELD_HEIGHT, FIELD_HEIGHT, true, this::next);
                addRenderableWidget(leftButton);
                addRenderableWidget(rightButton);
            }

            List<String> options = component.getOptions();
            String text = options.isEmpty() ? "" : options.get(selected);
            int textWidth = CustomFormScreen.this.font.width(text);
            int textX = x + (CONTENT_WIDTH - textWidth) / 2;
            int textY = y + (FIELD_HEIGHT - CustomFormScreen.this.font.lineHeight) / 2;
            graphics.text(CustomFormScreen.this.font, Component.literal(text), textX, textY, 0xFFFFFF, true);
        }

        private void previous() {
            if (selected > 0) selected--;
        }

        private void next() {
            if (selected < component.getOptions().size() - 1) selected++;
        }

        @Override
        boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (leftButton != null && leftButton.mouseClicked(event, doubleClick)) return true;
            return rightButton != null && rightButton.mouseClicked(event, doubleClick);
        }

        @Override
        Integer getValue() {
            return selected;
        }
    }

    private static final class ArrowButton extends AbstractButton {
        private final boolean right;
        private final Runnable action;

        ArrowButton(int x, int y, int width, int height, boolean right, Runnable action) {
            super(x, y, width, height, Component.literal(right ? ">" : "<"));
            this.right = right;
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers modifiers) {
            action.run();
        }

        // ★ 追加: ナレーション更新メソッドの実装
        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
            int bg = this.isHovered() ? 0xFF_AA_AA_AA : 0xFF_66_66_66;
            extractor.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            extractor.centeredText(Minecraft.getInstance().font, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFF);
        }
    }
}