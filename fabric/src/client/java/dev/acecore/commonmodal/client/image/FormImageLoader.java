package dev.acecore.commonmodal.client.image;

import dev.acecore.commonmodal.api.form.SimpleForm;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SimpleForm のボタンアイコン画像を非同期で取得・キャッシュするローダ。
 * <p>
 * plan.md §4.2.A に基づく。
 * <ul>
 *   <li>{@code type: "url"}: HTTP でダウンロードし {@link NativeImageBackedTexture} へ。</li>
 *   <li>{@code type: "path"}: リソースパック内テクスチャを {@link Identifier} として解決。</li>
 * </ul>
 * エラー時はフォールバック (アイコン領域を詰める) ため {@code null} を返す設計。
 */
public final class FormImageLoader {
    private static final String NAMESPACE = "commonmodal";
    private static final ConcurrentHashMap<String, CompletableFuture<Identifier>> URL_CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private FormImageLoader() {
    }

    /**
     * 指定アイコンの描画用識別子を非同期で取得する。
     * path の場合は同期的に {@link Identifier} を返す。url の場合はダウンロード完了後に識別子が確定する。
     *
     * @param image ボタン画像定義。{@code null} の場合は {@code null} を返す。
     * @return 描画用識別子を扱う future。失敗時は {@code null} で完了する。
     */
    public static CompletableFuture<Identifier> load(SimpleForm.ButtonImage image) {
        if (image == null) {
            return CompletableFuture.completedFuture(null);
        }
        String type = image.getType();
        String data = image.getData();
        if (data == null || data.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if ("path".equalsIgnoreCase(type)) {
            try {
                Identifier id = Identifier.of(data);
                return CompletableFuture.completedFuture(id);
            } catch (Exception e) {
                return CompletableFuture.completedFuture(null);
            }
        }
        if ("url".equalsIgnoreCase(type)) {
            return URL_CACHE.computeIfAbsent(data, FormImageLoader::downloadUrl);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static CompletableFuture<Identifier> downloadUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenApply(response -> {
                        if (response.statusCode() / 100 != 2) {
                            return null;
                        }
                        try (InputStream in = response.body()) {
                            return registerTexture(url, in);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .exceptionally(e -> null);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static Identifier registerTexture(String url, InputStream in) throws IOException {
        NativeImage image = NativeImage.read(in);
        if (image == null) {
            return null;
        }
        String path = "dynamic/" + Integer.toHexString(url.hashCode());
        Identifier id = Identifier.of(NAMESPACE, path);
        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        return id;
    }
}