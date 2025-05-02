package nz.ac.canterbury.seng302.homehelper.validation;

import java.io.*;

import nz.ac.canterbury.seng302.homehelper.util.EnvVarUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.springframework.stereotype.Service;

@Service
public class ProfanitySpikeValidation {

    private final EnvVarUtil envVarUtil = new EnvVarUtil();

    public void validateTagName(String tagName) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, tagName);

        Request request = new Request.Builder()
                .url("https://api.apilayer.com/bad_words?censor_character=censor_character")
                .addHeader("apikey", envVarUtil.retrieveEnvironmentVariable("BAD_FILTER_API"))
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
    System.out.println(response.body().string());


    }




}
