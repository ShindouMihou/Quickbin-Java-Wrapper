package pw.quickbin.main.impl;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pw.quickbin.main.api.Bin;
import pw.quickbin.main.api.QuickBin;
import pw.quickbin.main.exceptions.InvalidRequestException;
import pw.quickbin.main.exceptions.RateLimitedException;
import pw.quickbin.main.factory.ThreadFactory;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

public class QuickBinFactory implements QuickBin {

    private final String token;
    private final String agent;
    private final ExecutorService service;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String BASE_URL = "https://quickbin.pw/api/v2/bot/%s/";

    public QuickBinFactory(String token, String agent, ExecutorService service) {
        if (token == null || token.isEmpty())
            throw new InvalidParameterException("The token cannot be empty or null, please request one.");

        if (agent == null || agent.isEmpty())
            throw new InvalidParameterException("The custom-user-agent cannot be empty or null, please request one.");

        this.service = (service == null ? new ThreadFactory().executorService : service);
        this.agent = agent;
        this.token = token;
    }

    /**
     * Creates a new bin with your token and user-agent.
     * @throws InvalidRequestException when the request is invalid.
     * @throws RateLimitedException when you are rate-limited (1,000 requests a day).
     * @param content the content of the bin.
     * @param language the language of the bin.
     * @return CompletableFuture<Bin>
     */
    @Override @Nullable
    public CompletableFuture<Bin> createBin(String content, String language) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder().url(String.format(BASE_URL, "create") +
                    language + '/').header("User-Agent", agent).post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("token", token).addFormDataPart("content", content).build()).build();

            // Parse response accordingly.
            try {
                    Response response = httpClient.newCall(request).execute();
                    if (!(Objects.isNull(response.body()))) {
                        if (response.code() != 200) {
                            throw (response.code() == 429 ? new RateLimitedException(true) : new InvalidRequestException(new JSONObject(response.body().string()).getString("response")));
                        } else {
                            JSONObject object = new JSONObject(response.body().string());
                            return new Bin(object.getString("bin"), object.getString("full_url"), object.getString("language"));
                        }
                    }
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
            return null;
        }, service);
    }

    /**
     * Edits a bin if it exists and is also owned by your token.
     * @throws InvalidRequestException when the request is invalid.
     * @throws RateLimitedException when you are rate-limited (1,000 requests a day).
     * @param bin the bin to edit.
     * @param content the new contents of the bin.
     * @param language the new language of the bin.
     * @return CompletableFuture<Void>
     */
    @Override
    public CompletableFuture<Void> editBin(String bin, String content, String language){
        StringBuilder s = new StringBuilder(String.format(BASE_URL, "edit")).append(bin)
                .append("/");

        if(language != null){
            s.append(language).append("/");
        }

        return sendNormalRequest(new Request.Builder().url(s.toString()).header("User-Agent", agent).post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("token", token).addFormDataPart("content", content).build()).build());
    }

    /**
     * Edits a bin if it exists and is also owned by your token.
     * @throws InvalidRequestException when the request is invalid.
     * @throws RateLimitedException when you are rate-limited (1,000 requests a day).
     * @param bin the bin to edit.
     * @param content the new contents of the bin.
     * @return CompletableFuture<Void>
     */
    @Override
    public CompletableFuture<Void> editBin(String bin, String content){
        return editBin(bin, content, null);
    }

    /**
     * Deletes a bin if it exists and is also owned by your token.
     * @throws InvalidRequestException when the request is invalid.
     * @throws RateLimitedException when you are rate-limited (1,000 requests a day).
     * @param bin the bin to delete.
     * @return CompletableFuture<Void>
     */
    @Override
    public CompletableFuture<Void> deleteBin(String bin){
        return sendNormalRequest(new Request.Builder().url(new StringBuilder(String.format(BASE_URL, "edit")).append(bin)
                .append("/").toString()).header("User-Agent", agent).post(new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("token", token).addFormDataPart("bin", bin).build()).build());
    }

    private CompletableFuture<Void> sendNormalRequest(Request request) {
        return CompletableFuture.runAsync(() -> {
            try {
                    Response response = httpClient.newCall(request).execute();
                    if (!(Objects.isNull(response.body()))) {
                        if (response.code() != 200) {
                            throw (response.code() == 429 ? new RateLimitedException(true) : new InvalidRequestException(new JSONObject(response.body().string()).getString("response")));
                        }
                        System.out.println(response.body().string());
                    }
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, service);
    }

}
