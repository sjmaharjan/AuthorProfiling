package edu.uab.console.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by suraj on 4/17/14.
 */
public class SplitData {


    public static void splitAt(String inputFolder, int testPercent, String outputFolder, List<String> labels) {

        Map<String, Integer> countFiles = new HashMap<String, Integer>();
        Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
        for (String l : labels) {
            countFiles.put(l, 0);
            fileMap.put(l, new ArrayList<String>());
        }


        File folder = new File(inputFolder);
        File training = new File(outputFolder, "training");
        File test = new File(outputFolder, "test");
        if (!training.exists()) {
            if (training.mkdirs()) {
                System.out.println("Directory  training is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        if (!test.exists()) {
            if (test.mkdirs()) {
                System.out.println("Directory  test is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }


        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                if (listOfFiles[i].getName().equals(".DS_Store"))
                    continue;
                System.out.println(" file " + listOfFiles[i].getName());
                String label = Helper.getLabel(listOfFiles[i].getName());
                countFiles.put(label, countFiles.get(label) + 1);
                List fileList = fileMap.get(label);
                fileList.add(listOfFiles[i].getName());
                fileMap.put(label, fileList);


            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }

        }

        System.out.println("File distribution :" + countFiles.toString());
        for (Map.Entry<String, List<String>> entry : fileMap.entrySet()) {

            int copyFile = countFiles.get(entry.getKey()) * testPercent / 100;
            System.out.println("Number of file to be in test folder" + copyFile);
            List<String> files = entry.getValue();
            for (int i = 0; i < copyFile; i++) {
                //random number
                //get file and copy to destination
                System.out.println("List size "+files.size());
                int id = getRandomNumber(0, files.size());
                System.out.println("Random number  "+id);
                String fileName = files.get(id);
                files.remove(id);
                try {
                    System.out.println("Copying file " + fileName + "test folder");
                    Files.copy(new File(folder, fileName).toPath(), new File(test, fileName).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Couldn't copy file");
                }


            }

            for (String file : files) {
                try {
                    System.out.println("Copying file " + file + "training folder");
                    Files.copy(new File(folder, file).toPath(), new File(training, file).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Couldn't copy file");
                }
            }


        }

    }


    public static int getRandomNumber(int minimum, int maximum) {
        Random rn = new Random();
        return rn.nextInt(maximum);
    }


    public static void main(String[] args) {
        String inputFolder = args[0];
        String outputFolder = args[1];
        int testPercent = Integer.parseInt(args[2]);
        // age_group="18-24|25-34|35-49|50-64|65-xx"
        String[] l = {
                "18-24_male", "18-24_female", "25-34_male", "25-34_female", "35-49_male", "35-49_female", "50-64_male", "50-64_female", "65-xx_male", "65-xx_female"
        };


        List<String> labels = new ArrayList<String>(Arrays.asList(l));
        SplitData.splitAt(inputFolder, testPercent, outputFolder, labels);
    }
}
