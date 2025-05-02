package nz.ac.canterbury.seng302.homehelper.validation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import nz.ac.canterbury.seng302.homehelper.util.EnvVarUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.springframework.stereotype.Service;

@Service
public class TagValidation {

    private final EnvVarUtil envVarUtil = new EnvVarUtil();


    /**
     * @param word The word being checked for profanities
     * @throws IOException
     */
    public boolean profanityFilterCheck(String word) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, word);

        Request request = new Request.Builder()
                .url("https://api.apilayer.com/bad_words?censor_character=censor_character")
                .addHeader("apikey", envVarUtil.retrieveEnvironmentVariable("BAD_FILTER_API"))
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());

        return true;

    }

    public List<String> validateTagName(String tagName) throws IOException {

        List<String> errors = new ArrayList<>();

        String[] words = tagName.split(" ");
        for (String word : words) {
            Boolean cleanWord = profanityFilterCheck(word);
            if (!cleanWord) {
                errors.add("This tag name does not comply with Home Helper's language standards");
                return errors;
            }
        }
        return errors;
    }
}


