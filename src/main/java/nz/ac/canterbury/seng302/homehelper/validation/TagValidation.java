package nz.ac.canterbury.seng302.homehelper.validation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.canterbury.seng302.homehelper.util.EnvVarUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


import org.springframework.stereotype.Service;

@Service
public class TagValidation {

    private final EnvVarUtil envVarUtil = new EnvVarUtil();

    /**
     * @param word The word being checked for profanities
     * @throws IOException
     */
    public List<String> profanityFilterCheck(String word, List<String> errors) throws IOException {

        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, word);

            Request request = new Request.Builder()
                    .url("https://api.apilayer.com/bad_words?censor_character=*")
                    .addHeader("apikey", envVarUtil.retrieveEnvironmentVariable("BAD_FILTER_API"))
                    .method("POST", body)
                    .build();
            Response response = client.newCall(request).execute();
            response.close();

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseText = objectMapper.readValue(response.body().string(), new TypeReference<Map<String, String>>() {
            });
            if (Integer.parseInt(responseText.get("bad_words_total")) > 0) {
                errors.add("This tag name does not comply with Home Helper's language standards");
            }
        }
        catch (IOException e) {
            errors.add(e.getMessage());
        }
        return errors;
    }

    /**
     * Checks the tag name the user entered is valid.
     * @param tagName The name of the tag the user enetered; can be multiple words separated by whitespace
     * @return the list of errors found in the tag name, empty if none found
     * @throws IOException
     */
    public List<String> validateTagName(String tagName) throws IOException {

        List<String> errors = new ArrayList<>();

        String[] words = tagName.split(" ");
        for (String word : words) {
            errors = profanityFilterCheck(word, errors);
            if (!errors.isEmpty()) {
                return errors;
            }
        }
        return errors;
    }


}




