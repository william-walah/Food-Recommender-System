/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author asus
 */
abstract class Matrix {
    private double[][] entry;
    
    public double[][] getEntry(){return this.entry;}
    
    // basic method
    public double[][] transpose(){
        int row = this.entry[0].length;
        int col = this.entry.length;
        double[][] result = new double[row][col];
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                result[i][j] =  this.entry[j][i];
        return result;
    }
    
    public double[][] add(double[][] o){
        if(this.entry.length != o.length) throw new RuntimeException("Addition Error: row length not match");
        else if(this.entry[0].length != o[0].length) throw new RuntimeException("Addition Error: col length not match");
        
        double[][] result = new double[this.entry.length][this.entry[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = this.entry[i][j] + o[i][j];
            }
        }
        
        return result;
    }
    
    public double[][] subtract(double[][] o){
        if(this.entry.length != o.length) throw new RuntimeException("Subtract Error: row length not match");
        else if(this.entry[0].length != o[0].length) throw new RuntimeException("Subtract Error: col length not match");
        
        double[][] result = new double[this.entry.length][this.entry[0].length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                result[i][j] = this.entry[i][j] - o[i][j];
            }
        }
        
        return result;
    }
    
    public double[][] mulitply(double[][] o){
        int m1 = this.entry.length;
        int n1 = this.entry[0].length;
        int m2 = o.length;
        int n2 = o[0].length;
        if(n1 != m2) throw new RuntimeException("Multiply Error: unmatch matrix dimension");
        
        double[][] result = new double[m1][n2];
        for (int i = 0; i < m1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n1; k++)
                    result[i][j] += this.entry[i][k] * o[k][j];
            }
        }
        
        return result;
    }
    
    public void printEntry(){
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[i].length; j++) {
                System.out.print(this.entry[i][j]+" | ");
            }
            System.out.println("");
        }
    }
}
    