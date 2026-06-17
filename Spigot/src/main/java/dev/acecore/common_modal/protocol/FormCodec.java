package dev.acecore.common_modal.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.acecore.common_modal.api.form.CustomForm;
import dev.acecore.common_modal.api.form.Form;
import dev.acecore.common_modal.api.form.FormImage;
import dev.acecore.common_modal.api.form.ModalForm;
import dev.acecore.common_modal.api.form.SimpleForm;
import dev.acecore.common_modal.api.form.component.DropdownComponent;
import dev.acecore.common_modal.api.form.component.FormComponent;
import dev.acecore.common_modal.api.form.component.InputComponent;
import dev.acecore.common_modal.api.form.component.LabelComponent;
import dev.acecore.common_modal.api.form.component.SliderComponent;
import dev.acecore.common_modal.api.form.component.ToggleComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * commonModal の JSON ペイロードと Java モデル間の変換を担うコーデック。
 *
 * <p>plan.md §4（通信プロトコル）に基づく。Spigot 側は Fabric 側 {@code FormCodec}
 * と対になる振る舞いを提供し、両者の JSON 表現を同一に保つ。</p>
 */
public final class FormCodec {

    private FormCodec() {
        throw new UnsupportedOperationException("constants");
    }

    /**
     * {@link Form} オブジェクトを {@code {"id": int, "form": {...}}} 形式の JSON に変換する。
     *
     * @param formId フォーム ID
     * @param form   フォームモデル
     * @return UTF-8 JSON 文字列
     */
    public static String encodeForm(int formId, Form form) {
        JsonObject root = new JsonObject();
        root.addProperty("id", formId);
        root.add("form", encodeFormBody(form));
        return root.toString();
    }

    /**
     * クライアントからの応答 {@code value} 要素を Java オブジェクトに変換する。
     *
     * @param element JSON 要素。{@code null} 要素または JsonNull の場合は {@code null}
     * @return Boolean / Integer / String / Double / List または null
     */
    public static Object decodeResponseValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
            if (element.getAsJsonPrimitive().isNumber()) {
                Number number = element.getAsNumber();
                if (number.longValue() == number.doubleValue()
                        && number.toString().matches("-?\\d+")) {
                    // 整数として扱える場合は Integer。範囲外の場合は Long。
                    long value = number.longValue();
                    if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                        return (int) value;
                    }
                    return value;
                }
                return number.doubleValue();
            }
            return element.getAsString();
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                list.add(decodeResponseValue(e));
            }
            return list;
        }
        return null;
    }

    private static JsonObject encodeFormBody(Form form) {
        return switch (form.getType()) {
            case "modal" -> encodeModalForm((ModalForm) form);
            case "simple" -> encodeSimpleForm((SimpleForm) form);
            case "custom" -> encodeCustomForm((CustomForm) form);
            default -> throw new IllegalArgumentException("Unknown form type: " + form.getType());
        };
    }

    private static JsonObject encodeModalForm(ModalForm form) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "modal");
        obj.addProperty("title", form.getTitle());
        obj.addProperty("content", form.getContent());
        obj.addProperty("button1", form.getButton1());
        obj.addProperty("button2", form.getButton2());
        return obj;
    }

    private static JsonObject encodeSimpleForm(SimpleForm form) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "simple");
        obj.addProperty("title", form.getTitle());
        obj.addProperty("content", form.getContent());

        JsonArray buttons = new JsonArray();
        for (SimpleForm.Button button : form.getButtons()) {
            JsonObject btn = new JsonObject();
            btn.addProperty("text", button.getText());
            FormImage image = button.getImage();
            if (image != null) {
                JsonObject img = new JsonObject();
                img.addProperty("type", image.getType().name().toLowerCase());
                img.addProperty("data", image.getData());
                btn.add("image", img);
            }
            buttons.add(btn);
        }
        obj.add("buttons", buttons);
        return obj;
    }

    private static JsonObject encodeCustomForm(CustomForm form) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "custom");
        obj.addProperty("title", form.getTitle());

        JsonArray components = new JsonArray();
        for (FormComponent component : form.getComponents()) {
            components.add(encodeComponent(component));
        }
        obj.add("components", components);
        return obj;
    }

    private static JsonObject encodeComponent(FormComponent component) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", component.getType());
        switch (component) {
            case LabelComponent c -> obj.addProperty("text", c.getText());
            case InputComponent c -> {
                obj.addProperty("text", c.getText());
                obj.addProperty("placeholder", c.getPlaceholder());
                obj.addProperty("default", c.getDefault());
            }
            case ToggleComponent c -> {
                obj.addProperty("text", c.getText());
                obj.addProperty("default", c.isDefault());
            }
            case SliderComponent c -> {
                obj.addProperty("text", c.getText());
                obj.addProperty("min", c.getMin());
                obj.addProperty("max", c.getMax());
                obj.addProperty("step", c.getStep());
                obj.addProperty("default", c.getDefault());
            }
            case DropdownComponent c -> {
                obj.addProperty("text", c.getText());
                JsonArray options = new JsonArray();
                for (String option : c.getOptions()) {
                    options.add(option);
                }
                obj.add("options", options);
                obj.addProperty("default", c.getDefaultIndex());
            }
            default -> throw new IllegalArgumentException("Unknown component: " + component.getClass());
        }
        return obj;
    }
}
