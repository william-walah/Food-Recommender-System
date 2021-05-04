/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 *
 * @author asus
 */
public class FactorMatrix extends Matrix{
    private FactorType type;
    
    public FactorMatrix(int row, int col, FactorType t){
        super(row,col);
        this.type = t;
        
        //initialize entry between 1.0 - 5.0
        Random r = new Random();
        Supplier<Double> randomValue = () -> (double) (r.nextInt(5)+1);
        for (int i = 0; i < entry.length; i++) {
            entry[i] = Stream.generate(randomValue).limit(entry[i].length).mapToDouble(Double::valueOf).toArray();
        }   
    }
}
