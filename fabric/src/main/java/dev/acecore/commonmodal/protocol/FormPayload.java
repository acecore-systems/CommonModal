package dev.acecore.commonmodal.protocol;

import dev.acecore.commonmodal.api.form.Form;

/**
 * {@code commonmodal:form} チャネルのペイロードモデル。
 * <p>
 * plan.md §4.A に基づき、{@code id} と {@code form} を保持する。
 * Minecraft 非依存の純粋なデータモデル。
 */
public final class FormPayload {
    private final int id;
    private final Form form;

    public FormPayload(int id, Form form) {
        this.id = id;
        this.form = form;
    }

    public int getId() {
        return id;
    }

    public Form getForm() {
        return form;
    }
}