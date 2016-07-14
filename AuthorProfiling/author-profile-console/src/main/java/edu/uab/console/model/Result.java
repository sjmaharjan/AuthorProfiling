package edu.uab.console.model;

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
    private double[] probs;
    private Integer[] en_probs_order = {3, 0, 4, 2, 6, 5, 7, 1, 8, 9};
    private Integer[] es_probs_order = {7, 6, 5, 3, 4, 1, 2, 0, 9, 8};

    public Result(String prediction, double[] probs) {
        this.prediction = prediction;
        this.probs = probs;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double[] getProbs() {
        return probs;
    }

    public void setProbs(double[] probs) {
        this.probs = probs;
    }

    public void rearrangeProbsEn() {
        double [] new_probs = new double[en_probs_order.length];
        //System.out.println("\n\nhellloooooo: ");
        //displayProbs();
        for(int i=0; i<en_probs_order.length; i++){
            int j = Arrays.asList(en_probs_order).indexOf(i);
            System.out.println("-------------------------" + j);
            new_probs[i] = probs[j];
        }
	//displayProbs();
        probs = new_probs;
    }

    public void rearrangeProbsEs() {
        double [] new_probs = new double[es_probs_order.length];
        for(int i=0; i<es_probs_order.length; i++){
            new_probs[i] = probs[Arrays.asList(es_probs_order).indexOf(i)];
        }
        probs = new_probs;

    }

    public void displayProbs(){
        for(int i =0; i<probs.length; i++) {
            System.out.print(i + ": " + probs[i] + ", ");
        }
        System.out.println();
    }

    public static void main(String args[]){
//        Result r = new Result("fafaa",new Double[]{-227.40303941433564,-227.86730375091057,-231.10903023089048,-231.83310004537293,-241.66711988276998,-240.53808858597085});
//        Result r = new Result("fafaa",new Double[]{-2259.3042491132433,-2259.443978495675,-2276.174516610736,-2281.24375167675,-2318.393674212602,-2316.105289654767});
//        Result r = new Result("fafaa",new Double[]{-445.4039212385178,-442.5049744745524,-444.61521775558504,-453.36707754703264,-421.95848640919314,-409.5511691798985});
//        Result r = new R/esult("fafaa",new Double[]{-29.972819556765696,-29.762629601681237,-30.689149405983102,-31.432316988422613,-32.21048485799976,-32.723388102755614});
        Result r = new Result("fafaa",new double[]{-445.4039212385178,-442.5049744745524,-444.61521775558504,-453.36707754703264,-421.95848640919314,-409.5511691798985, -945.4039212385178,-942.5049744745524,-944.61521775558504,-953.36707754703264});
        r.displayProbs();
        r.rearrangeProbsEs();
        r.displayProbs();
    }
}
