package Model;

/**
 *
 * @author William Walah - 2017730054
 */
abstract class Matrix {
    protected double[][] entry;
    
    public Matrix(int row, int col){
        this.entry = new double[row][col];
    }
    
    public Matrix(double[][] e){this.entry = e;} //this is shallow copy / just moving reference
    
    public double[][] getEntry(){return this.entry;}
    public void setEntry(double[][] o){this.entry = o;}
    public double getEntryByIndex(int i, int j){return this.entry[i][j];}
    
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
    
    public void add(double[][] o){
        if(this.entry.length != o.length) throw new RuntimeException("Addition Error: row length not match");
        else if(this.entry[0].length != o[0].length) throw new RuntimeException("Addition Error: col length not match");

        for (int i = 0; i < this.entry.length; i++) {
            for (int j = 0; j < this.entry[i].length; j++) {
                this.entry[i][j] = this.entry[i][j] + o[i][j];
            }
        }
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
    
    public double[][] multiply(double[][] o){
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
    
    //  this frobenius norm is modified, at the end should return
    // square root of sumSq. But because the algorithm is using
    // the quadratic value of this norm, so the var. sumSq 
    // is not square rooted
    public double frobeniusNorm(){
        double sumSq = 0.0;
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[i].length; j++) {
                sumSq += Math.pow(entry[i][j],2);
            }
        }
        return sumSq;
    }
    
    // time-consuming process (500*500 = 250000 entry)
    public String printEntry(){
        System.out.println(entry.length+","+entry[0].length);
        String res = "";
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[i].length; j++) {
                res += this.entry[i][j]+" | ";
            }
            res += "\n";
        }
        return res;
    }
    
    public void changeEntry(int row, int col, double newEntry){
        this.entry[row][col] = newEntry;
    }
    
    public int getRowLength(){return this.entry.length;}
    public int getColLength(){return this.entry[0].length;}
}
        