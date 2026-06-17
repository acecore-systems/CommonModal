package dev.acecore.commonmodal.client.screen;

import dev.acecore.commonmodal.api.form.CustomForm;
import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.component.DropdownComponent;
import dev.acecore.commonmodal.api.form.component.FormComponent;
import dev.acecore.commonmodal.api.form.component.InputComponent;
import dev.acecore.commonmodal.api.form.component.LabelComponent;
import dev.acecore.commonmodal.api.form.component.SliderComponent;
import dev.acecore.commonmodal.api.form.component.ToggleComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 複合入力フォーム (Custom Form) を描画する {@link Screen}。
 * <p>
 * plan.md §4.3 に基づく。縦に並ぶ複数の入力コンポーネントをスクロール可能な
 * パネル内に配置し、最下部に「送信」ボタンを配置する。
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
        super(Text.literal(form.getTitle()));
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
        this.addDrawableChild(ButtonWidget.builder(Text.literal("送信"), btn -> submit())
                .dimensions(submitX, submitY, CONTENT_WIDTH, SUBMIT_HEIGHT)
                .build());

        recalculateLayout();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        init();
    }

    private void recalculateLayout() {
        int total = calculateTotalHeight();
        int visible = contentBottom - contentTop;
        maxScroll = Math.max(0, total - visible);
        scroll = Math.clamp(scroll, 0.0, (double) maxScroll);
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // タイトル
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int startX = (this.width - CONTENT_WIDTH) / 2;
        int y = contentTop - (int) scroll;

        for (ComponentState<?> state : componentStates) {
            // ラベルテキスト
            String label = state.label();
            if (label != null && !label.isEmpty()) {
                context.drawTextWithShadow(this.textRenderer, Text.literal(label), startX, y, 0xFFFFFF);
                y += this.textRenderer.fontHeight + LABEL_GAP;
            }

            // 入力 UI
            state.render(context, startX, y, mouseX, mouseY);
            y += state.height() + COMPONENT_GAP;
        }

        // スクロールバー
        if (maxScroll > 0) {
            int visible = contentBottom - contentTop;
            int barHeight = Math.max(16, visible * visible / (maxScroll + visible));
            int barY = contentTop + (int) ((visible - barHeight) * scroll / maxScroll);
            int barX = startX + CONTENT_WIDTH + 6;
            context.fill(barX, contentTop, barX + SCROLLBAR_WIDTH, contentBottom, 0x44_00_00_00);
            context.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + barHeight, 0xFF_CC_CC_CC);
        }
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        for (ComponentState<?> state : componentStates) {
            state.onClearChildren();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll > 0) {
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
            if (state.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        for (ComponentState<?> state : componentStates) {
            state.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && maxScroll > 0) {
            int visible = contentBottom - contentTop;
            int barHeight = Math.max(16, visible * visible / (maxScroll + visible));
            int trackHeight = visible - barHeight;
            scroll = Math.clamp((mouseY - contentTop - barHeight / 2.0) * maxScroll / (double) trackHeight, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            scroll = Math.clamp(scroll - verticalAmount * FIELD_HEIGHT, 0.0, (double) maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ComponentState<?> state : componentStates) {
            if (state.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ComponentState<?> state : componentStates) {
            if (state.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
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
        if (this.client == null) {
            return;
        }
        this.client.setScreen(null);
        callbacks.onResult(formId, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ComponentState<?> createState(FormComponent component) {
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
        void onClearChildren() {
        }
        abstract String label();

        abstract int height();

        abstract int labelHeight();

        abstract void render(DrawContext context, int x, int y, int mouseX, int mouseY);

        abstract boolean mouseClicked(double mouseX, double mouseY, int button);

        void mouseReleased(double mouseX, double mouseY, int button) {
        }

        boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        boolean charTyped(char chr, int modifiers) {
            return false;
        }

        abstract T getValue();
    }

    private class LabelState extends ComponentState<Void> {
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
            return CustomFormScreen.this.textRenderer.fontHeight + LABEL_GAP;
        }

        @Override
        void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
        }

        @Override
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        Void getValue() {
            return null;
        }
    }

    private class InputState extends ComponentState<String> {
        private final InputComponent component;
        private TextFieldWidget field;

        InputState(InputComponent component) {
            this.component = component;
        }

        @Override
        void onClearChildren() {
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
            return component.getText().isEmpty() ? 0 : CustomFormScreen.this.textRenderer.fontHeight + LABEL_GAP;
        }

        @Override
        void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
            if (field == null) {
                field = new TextFieldWidget(CustomFormScreen.this.textRenderer, x, y, CONTENT_WIDTH, FIELD_HEIGHT,
                        Text.literal(""));
                field.setPlaceholder(Text.literal(component.getPlaceholder()));
                field.setText(component.getDefault());
                addDrawableChild(field);
            }
        }

        @Override
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            return field != null && field.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return field != null && field.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        boolean charTyped(char chr, int modifiers) {
            return field != null && field.charTyped(chr, modifiers);
        }

        @Override
        String getValue() {
            return field == null ? component.getDefault() : field.getText();
        }
    }

    private class ToggleState extends ComponentState<Boolean> {
        private final ToggleComponent component;
        private boolean value;
        private PressableWidget widget;

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
        void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
            if (widget == null) {
                widget = new PressableWidget(x, y, FIELD_HEIGHT, FIELD_HEIGHT, Text.literal("")) {
                    @Override
                    public void onPress() {
                        value = !value;
                    }

                    @Override
                    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF_33_33_33);
                        if (value) {
                            int pad = 4;
                            context.fill(getX() + pad, getY() + pad,
                                    getX() + getWidth() - pad, getY() + getHeight() - pad, 0xFF_55_FF_55);
                        }
                    }
                };
                addDrawableChild(widget);
            }
        }

        @Override
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            return widget != null && widget.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        Boolean getValue() {
            return value;
        }
    }

    private class SliderState extends ComponentState<Double> {
        private final SliderComponent component;
        private SliderWidget slider;

        SliderState(SliderComponent component) {
            this.component = component;
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
            return CustomFormScreen.this.textRenderer.fontHeight + LABEL_GAP;
        }

        @Override
        void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
            if (slider == null) {
                double range = component.getMax() - component.getMin();
                double initial = (component.getDefault() - component.getMin()) / range;
                slider = new SliderWidget(x, y, CONTENT_WIDTH, FIELD_HEIGHT, Text.literal(""), initial) {
                    @Override
                    protected void updateMessage() {
                        setMessage(Text.literal(formatValue(value)));
                    }

                    @Override
                    protected void applyValue() {
                        // value が SliderWidget 内部で 0.0〜1.0 の値として保持される
                    }
                };
                slider.updateMessage();
                addDrawableChild(slider);
            }
        }

        @Override
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            return slider != null && slider.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        boolean mouseReleased(double mouseX, double mouseY, int button) {
            return slider != null && slider.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        Double getValue() {
            if (slider == null) {
                return component.getDefault();
            }
            double range = component.getMax() - component.getMin();
            double raw = component.getMin() + slider.getValue() * range;
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
        private PressableWidget leftButton;
        private PressableWidget rightButton;

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
            return CustomFormScreen.this.textRenderer.fontHeight + LABEL_GAP;
        }

        @Override
        void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
            if (leftButton == null) {
                leftButton = new ArrowButton(x, y, FIELD_HEIGHT, FIELD_HEIGHT, false, this::previous);
                rightButton = new ArrowButton(x + CONTENT_WIDTH - FIELD_HEIGHT, y, FIELD_HEIGHT, FIELD_HEIGHT, true, this::next);
                addDrawableChild(leftButton);
                addDrawableChild(rightButton);
            }

            // 選択テキストを中央に描画
            List<String> options = component.getOptions();
            String text = options.isEmpty() ? "" : options.get(selected);
            int textWidth = CustomFormScreen.this.textRenderer.getWidth(text);
            int textX = x + (CONTENT_WIDTH - textWidth) / 2;
            int textY = y + (FIELD_HEIGHT - CustomFormScreen.this.textRenderer.fontHeight) / 2;
            context.drawTextWithShadow(CustomFormScreen.this.textRenderer, Text.literal(text), textX, textY, 0xFFFFFF);
        }

        private void previous() {
            if (selected > 0) selected--;
        }

        private void next() {
            if (selected < component.getOptions().size() - 1) selected++;
        }

        @Override
        boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (leftButton != null && leftButton.mouseClicked(mouseX, mouseY, button)) return true;
            return rightButton != null && rightButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        Integer getValue() {
            return selected;
        }
    }

    private static final class ArrowButton extends PressableWidget {
        private final boolean right;
        private final Runnable action;

        ArrowButton(int x, int y, int width, int height, boolean right, Runnable action) {
            super(x, y, width, height, Text.literal(right ? ">" : "<"));
            this.right = right;
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int bg = this.isHovered() ? 0xFF_AA_AA_AA : 0xFF_66_66_66;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFF);
        }
    }
}
