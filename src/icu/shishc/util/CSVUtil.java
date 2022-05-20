package icu.shishc.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class CSVUtil {

    public static void main(String[] args) throws FileNotFoundException {
        String csvFile = "UserBehavior.csv";
        String csvSplit = ",";
        ArrayList<String> output = new ArrayList<>();
        String line;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
            Integer ite = 100;
            while (ite > 0 && (line = bufferedReader.readLine()) != null) {
                ite --;
                String[] arr = line.split(csvSplit);
                if (arr[3].equals("pv")) {
                    output.add(arr[1]);
                }
            }
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

}
