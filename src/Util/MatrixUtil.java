package Util;

/**
 *
 * @author William Walah - 2017730054
 */
public class MatrixUtil {

    public static double vectorMultiplication(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new RuntimeException("Error: mismatch vector length, first param length="+v1.length+" | second param length="+v2.length);
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
                res[i][j] = v1[i]*v2[j];
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
    
    //vector calculation (+/-)
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
    
    //vector calculation (+/-) with scalar to be applied either:
    //   - after calculation between vector entry
    //   - only on right vector value
    public static double[]  vectorCalculation(
            double[] left,
            double[] right, 
            int type, //0 = -, 1 = +
            int scalarSide, // 0 = both (scalar multiply after entry calculated), 1 = left, 2 = right
            double scalar
    ) {
        if(left.length != right.length) throw new RuntimeException("Error mismatch vector length on simple calculation(add/sub)");
        double[] res = new double[left.length];
        switch(type){
            case 0: //-
                for (int i = 0; i < res.length; i++) {
                    if(scalarSide == 0) res[i] = scalar * (left[i] - right[i]);
                    else if(scalarSide == 1) res[i] = (scalar * left[i]) - right[i];
                    else if(scalarSide == 2) res[i] = left[i] - (scalar*right[i]);
                    else throw new RuntimeException("Unmatched parameter scalarSide (0-2), given: "+scalarSide);
                }
                break;
            default: //+
                for (int i = 0; i < res.length; i++) {
                    if(scalarSide == 0) res[i] = scalar * (left[i] + right[i]);
                    else if(scalarSide == 1) res[i] = (scalar * left[i]) + right[i];
                    else if(scalarSide == 2) res[i] = left[i] + (scalar*right[i]);
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
    
    public static void print(double[][] v){
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                System.out.printf("%f, ",v[i][j]);
            }
            System.out.println("");
        }
    }
    
    public static double[] vectorMultiplyMatrix(double[] v, double[][] m){
        if((v.length != m[0].length) && (v.length != m.length)) throw new RuntimeException("Error: mismatch vector length with 1 of the matrix's dimension");
        else if(m.length == 1) throw new RuntimeException("Error: incorrect matrix dimension (1 x N), do you mean to use the other method? (vector, vector)");
        int dimen = v.length == m.length ? m[0].length : m.length;
        int type = v.length == m.length ? 0 : 1; //0 = row dimension, 1 = col dimension
        double[] res = new double[dimen];
        for (int i = 0; i < dimen; i++) {
            double[] mDimens = type == 0 ? MatrixUtil.getColumnVector(m, i) : m[i];
            double newVal = vectorMultiplication(v,mDimens);
            res[i] = newVal;
        }
        return res;
    }   
    
    //simple matrix calculation
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
    
    //matrix calculation with scalar multiplication
    //why? karena bisa dikerjakan sekali jalan..
    public static double[][] matrixCalculation(
            double[][] left,
            double[][] right, 
            int type, //0 = -, 1 = +
            boolean scalarOnLeft, //true = left, false = right
            double scalar
    ){
        if(left.length != right.length || left[0].length != right[0].length) throw new RuntimeException("Error mismatch matrix dimension");
        double[][] res = new double[left.length][left[0].length];
        for(int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[i].length; j++) {
                switch(type){
                    case 0: //-
                        if(scalarOnLeft) res[i][j] = (scalar * left[i][j]) - right[i][j];
                        else res[i][j] = left[i][j] - (scalar * right[i][j]);
                        break;
                    default: //+
                        if(scalarOnLeft) res[i][j] = (scalar * left[i][j]) + right[i][j];
                        else res[i][j] = left[i][j] + (scalar * right[i][j]);
                        break;
                }
            }
        }
        return res;
    }
    
    public static double[][] transpose(double[][] entry){
        int row = entry[0].length;
        int col = entry.length;
        double[][] result = new double[row][col];
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                result[i][j] =  entry[j][i];
        return result;
    }
    
    public static double[] getColumnVector(double[][] matrix, int index){
        double[] res = new double[matrix.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = matrix[i][index];
        }
        return res;
    }
    
    public static double[][] multiplyWithTransposing(double[][] left, double[][] right, int transpose){
        //true (left), false(right)
        double[][] res = null;
        int r1 = left.length;
        int c1 = left[0].length;
        int r2 = right.length;
        int c2 = right[0].length;
        if(transpose<0){ //left tranpose
            if(r1 != r2) throw new RuntimeException("Error matrix calculation: matrix dimension missmatch");
            res = new double[c1][c2];   
            for (int i = 0; i < c1; i++) {
                for (int j = 0; j < c2; j++) {
                    for (int k = 0; k < r1; k++) {
                        res[i][j] += left[k][i] * right[k][j];
                    }
                }
            }
        } else if(transpose>0) { //right transpose
            if(c1 != c2) throw new RuntimeException("Error matrix calculation: matrix dimension missmatch");
            res = new double[r1][r2];   
            for (int i = 0; i < r1; i++) {
                for (int j = 0; j < r2; j++) {
                    for (int k = 0; k < c1; k++) {
                        res[i][j] += left[i][k] * right[j][k];
                    }
                }
            }
        }
        else {
            if(r1 != c2){
                throw new RuntimeException("Error matrix calculation: matrix dimension missmatch");
            }
            res = new double[c1][r2];
            for (int i = 0; i < c1; i++) {
                for (int j = 0; j < r2; j++) {
                    for (int k = 0; k < r1; k++) {
                        res[i][j] += left[k][i] * right[j][k];
                    }
                }
            }
        }
        return res;
    }
    
    public static void sumAll(double[][] o){
        double res = 0.0;
        for (int i = 0; i < o.length; i++) {
            for (int j = 0; j < o[i].length; j++) {
                if(o[i][j]>5.0) {
                    System.out.println(o[i][j]);
                    System.exit(1);
                }
                res+= o[i][j];
            }
        }
        System.out.printf("%.5f\n",res);
    }
}
