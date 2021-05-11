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
    
    public static double[][] vectorMultiplicationToMatrix(double[] v1, double[] v2){
        double[][] res = new double[v1.length][v2.length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[i].length; j++) {
                res[i][j] = v1[i]+v2[j];
            }
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
    
    public static double[][] scalarMultiplication(double scalar, double[][] matrix) {
        double[][] res = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[i].length; j++) {
                res[i][j] = scalar * matrix[i][j];
            }
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
    
    public static double[] vectorMultiplyMatrix(double[] v, double[][] m){
        if(v.length != m.length) throw new RuntimeException("Error: mismatch vector length x matrix row length");
        else if(m.length == 1) throw new RuntimeException("Error: incorrect matrix dimension (1 x N), do you mean to use the other method? (vector, vector)");
        double[] res = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            double[] mDimens = m[i];
            double newVal = vectorMultiplication(v,mDimens);
            res[i] = newVal;
        }
        return res;
    }   
    
    public static double[][] matrixCalculation(double[][] left, double[][] right, int type) {
        if(left.length != right.length || left[0].length != right[0].length) throw new RuntimeException("Error mismatch matrix dimension");
        double[][] res = new double[left.length][left[0].length];
        for(int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[i].length; j++) {
                switch(type){
                    case 0: //-
                        res[i][j] = left[i][j] - right[i][j];
                        break;
                    default: //+
                        res[i][j] = left[i][j] + right[i][j];
                        break;
                }
            }
        }
        return res;
    }
}
