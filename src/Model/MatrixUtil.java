/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author asus
 */
public class MatrixUtil {

    public static double vectorMultiplication(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new RuntimeException("Error: mismatch vector length");
        }
        double res = 0.0;
        for (int i = 0; i < v1.length; i++) {
            res += v1[i] * v2[i];
        }
        return res;
    }

    public static double[] scalarMultiplication(double scalar, double[] vector) {
        double[] res = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            res[i] = vector[i]*scalar;
        }
        return res;
    }
    public static double[]  vectorCalculation(double[] left, double[] right, int type) {
        if(left.length != right.length) throw new RuntimeException("Error mismatch vector length on simple calculation(add/sub)");
        double[] res = new double[left.length];
        switch(type){
            case 0: //-
                for (int i = 0; i < res.length; i++) {
                    res[i] = left[i] - right[i];
                }
                break;
            default: //+
                for (int i = 0; i < res.length; i++) {
                    res[i] = left[i] + right[i];
                }
                break;
        }
        return res;
    }
    
    public static void print(double[] v){
        for (int i = 0; i < v.length; i++) {
            System.out.printf("%.2f, ",v[i]);
        }
        System.out.println("");
    }
}
