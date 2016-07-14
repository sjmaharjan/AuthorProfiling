package edu.uab.web.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by prasha on 1/5/14.
 */
public class Result {
    //    private double tens_male;
//    private double tens_female;
//    private double twentys_male;
//    private double twentys_female;
//    private double thirties_male;
//    private double thirties_female;
    private String prediction;
    private Double[] probs;

    public Result(String prediction, Double[] probs) {
        this.prediction = prediction;
        this.probs = probs;
        updateProbs();
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public Double[] getProbs() {
        return probs;
    }

    public void setProbs(Double[] probs) {
        this.probs = probs;
    }

//    max_x = max(data)
//    min_x = min(data)
//    updated_data = []
//    sum = 0
//            for x in data:
//            #print x, math.exp(x)
//    #print x, x-max_x, math.exp(x-max_x)
//    x -= min_x
//    #print x
//    x = math.exp(x)
//    print x
//    sum += x
//    updated_data.append(x)
//            #print x, math.exp(x)
//
//    print "\n", sum, "\n"
//
//            #max_x_updated = max(data)
//    for x in updated_data:
//    print x, x/sum

    public void updateProbs() {
        double total = 0;
        List<Double> temp = Arrays.asList(probs);
        double min = Collections.min(temp);

        for (int i = 0; i < probs.length; i++) {
            double x = probs[i];
            x -= min;
            x = Math.exp(x);
            probs[i] = x;
            total += probs[i];
            System.out.print(probs[i] + ", ");
        }
        System.out.println(total); 
        for (int i = 0; i < probs.length; i++) {
            probs[i] = probs[i] / total;
            System.out.print(probs[i] +", ");
        }


    }

    public static void main(String args[]){
//        Result r = new Result("fafaa",new Double[]{-227.40303941433564,-227.86730375091057,-231.10903023089048,-231.83310004537293,-241.66711988276998,-240.53808858597085});
        Result r = new Result("fafaa",new Double[]{-2259.3042491132433,-2259.443978495675,-2276.174516610736,-2281.24375167675,-2318.393674212602,-2316.105289654767});
//        Result r = new Result("fafaa",new Double[]{-445.4039212385178,-442.5049744745524,-444.61521775558504,-453.36707754703264,-421.95848640919314,-409.5511691798985});
//        Result r = new R/esult("fafaa",new Double[]{-29.972819556765696,-29.762629601681237,-30.689149405983102,-31.432316988422613,-32.21048485799976,-32.723388102755614});
    }
}