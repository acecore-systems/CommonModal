package dev.acecore.commonmodal.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.acecore.commonmodal.api.form.CustomForm;
import dev.acecore.commonmodal.api.form.Form;
import dev.acecore.commonmodal.api.form.ModalForm;
import dev.acecore.commonmodal.api.form.SimpleForm;
import dev.acecore.commonmodal.api.form.component.DropdownComponent;
import dev.acecore.commonmodal.api.form.component.FormComponent;
import dev.acecore.commonmodal.api.form.component.InputComponent;
import dev.acecore.commonmodal.api.form.component.LabelComponent;
import dev.acecore.commonmodal.api.form.component.SliderComponent;
import dev.acecore.commonmodal.api.form.component.ToggleComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * commonModal の JSON ペイロードと Java モデル間の変換を担うコーデック。
 * <p>
 * plan.md §4（通信プロトコル）に基づき、以下を行う。
 * <ul>
 *   <li>サーバー → クライアント: {@code {"id": int, "form": {...}}} の JSON を
 *       {@link FormPayload} にデコード。</li>
 *   <li>クライアント → サーバー: {@link ResponsePayload} を
 *       {@code {"id": int, "value": ...}} の JSON にエンコード。</li>
 * </ul>
 * 本クラスは Minecraft / Fabric 非依存であり、今後 Spigot 側プラグインでも
 * そのまま再利用できるよう、{@link Form} / {@link FormComponent} の Builder を
 * 経由して不変モデルを再構成する。
 */
public final class FormCodec {

    private FormCodec() {
        throw new UnsupportedOperationException("constants");
    }

    /**
     * {@code commonmodal:form} チャネルの JSON を {@link FormPayload} に変換する。
     *
     * @param json UTF-8 JSON 文字列
     * @return デコード結果
     * @throws IllegalArgumentException JSON 構造が不正、または未対応のフォーム/コンポーネント
     *                                  種別が指定された場合
     */
    public static FormPayload decodeForm(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        int id = getInt(root, "id", 0);
        JsonObject formJson = root.getAsJsonObject("form");
        if (formJson == null) {
            throw new IllegalArgumentException("Missing 'form' object in payload");
        }
        Form form = decodeFormBody(formJson);
        return new FormPayload(id, form);
    }

    /**
     * {@link ResponsePayload} を {@code commonmodal:response} チャネル用 JSON に変換する。
     *
     * @param payload 応答ペイロード
     * @return UTF-8 JSON 文字列
     */
    public static String encodeResponse(ResponsePayload payload) {
        JsonObject root = new JsonObject();
        root.addProperty("id", payload.getId());
        Object value = payload.getValue();
        if (value == null) {
            root.add("value", JsonNull.INSTANCE);
        } else {
            root.add("value", JsonParser.parseString(toJson(value)));
        }
        return root.toString();
    }

    private static Form decodeFormBody(JsonObject obj) {
        String type = getString(obj, "type", "");
        return switch (type) {
            case "modal" -> decodeModalForm(obj);
            case "simple" -> decodeSimpleForm(obj);
            case "custom" -> decodeCustomForm(obj);
            default -> throw new IllegalArgumentException("Unknown form type: " + type);
        };
    }

    private static ModalForm decodeModalForm(JsonObject obj) {
        return ModalForm.builder()
                .title(getString(obj, "title", ""))
                .content(getString(obj, "content", ""))
                .button1(getString(obj, "button1", ""))
                .button2(getString(obj, "button2", ""))
                .build();
    }

    private static SimpleForm decodeSimpleForm(JsonObject obj) {
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(getString(obj, "title", ""))
                .content(getString(obj, "content", ""));

        JsonArray buttons = obj.getAsJsonArray("buttons");
        if (buttons != null) {
            for (JsonElement e : buttons) {
                JsonObject btn = e.getAsJsonObject();
                String text = getString(btn, "text", "");
                SimpleForm.ButtonImage image = null;
                if (btn.has("image") && !btn.get("image").isJsonNull()) {
                    JsonObject img = btn.getAsJsonObject("image");
                    image = new SimpleForm.ButtonImage(
                            getString(img, "type", ""),
                            getString(img, "data", ""));
                }
                builder.button(text, image);
            }
        }
        return builder.build();
    }

    private static CustomForm decodeCustomForm(JsonObject obj) {
        CustomForm.Builder builder = CustomForm.builder()
                .title(getString(obj, "title", ""));

        JsonArray components = obj.getAsJsonArray("components");
        if (components != null) {
            for (JsonElement e : components) {
                builder.component(decodeComponent(e.getAsJsonObject()));
            }
        }
        return builder.build();
    }

    private static FormComponent decodeComponent(JsonObject obj) {
        String type = getString(obj, "type", "");
        return switch (type) {
            case "label" -> new LabelComponent(getString(obj, "text", ""));
            case "input" -> new InputComponent(
                    getString(obj, "text", ""),
                    getString(obj, "placeholder", ""),
                    getString(obj, "default", ""));
            case "toggle" -> new ToggleComponent(
                    getString(obj, "text", ""),
                    getBoolean(obj, "default", false));
            case "slider" -> new SliderComponent(
                    getString(obj, "text", ""),
                    getDouble(obj, "min", 0.0),
                    getDouble(obj, "max", 100.0),
                    getDouble(obj, "step", 1.0),
                    getDouble(obj, "default", 0.0));
            case "dropdown" -> {
                List<String> options = new ArrayList<>();
                JsonArray arr = obj.getAsJsonArray("options");
                if (arr != null) {
                    for (JsonElement e : arr) {
                        options.add(e.getAsString());
                    }
                }
                yield new DropdownComponent(
                        getString(obj, "text", ""),
                        options,
                        getInt(obj, "default", 0));
            }
            default -> throw new IllegalArgumentException("Unknown component type: " + type);
        };
    }

    // ---------- JSON ヘルパー ----------

    private static String getString(JsonObject obj, String key, String defaultValue) {
        JsonElement e = obj.get(key);
        if (e == null || e.isJsonNull()) {
            return defaultValue;
        }
        return e.getAsString();
    }

    private static int getInt(JsonObject obj, String key, int defaultValue) {
        JsonElement e = obj.get(key);
        if (e == null || e.isJsonNull()) {
            return defaultValue;
        }
        return e.getAsInt();
    }

    private static double getDouble(JsonObject obj, String key, double defaultValue) {
        JsonElement e = obj.get(key);
        if (e == null || e.isJsonNull()) {
            return defaultValue;
        }
        return e.getAsDouble();
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        JsonElement e = obj.get(key);
        if (e == null || e.isJsonNull()) {
            return defaultValue;
        }
        return e.getAsBoolean();
    }

    private static String toJson(Object value) {
        if (value instanceof String s) {
            return '"' + escapeJson(s) + '"';
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        // 未知の型は文字列化して JSON 文字列として返す
        return '"' + escapeJson(String.valueOf(value)) + '"';
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
