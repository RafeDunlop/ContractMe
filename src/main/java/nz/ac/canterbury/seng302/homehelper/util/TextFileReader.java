package nz.ac.canterbury.seng302.homehelper.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileReader {

    /**
     * @param fileName name of file to be read
     * @return list
     */
    public static List[] readTextFileReturnStringList(String fileName) {
        List[] textFileReturn = new List[2];

        List<String> errors = new ArrayList<>();
        List<String> output = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
            textFileReturn[0] = output;
        } catch (IOException e) {
            errors.add(e.getMessage());
            textFileReturn[1] = errors;

        }
        return textFileReturn;
    }


}
