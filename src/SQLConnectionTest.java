/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author asus
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class SQLConnectionTest {
    public static void main(String[] args) {
        Connection conn = null;
 
        try {
 
            String dbURL = "jdbc:sqlserver://localhost\\SQLEXPRESS;integratedSecurity=true ";
//            String user = "sa";
//            String pass = "secret";
            conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver ve   rsion: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
                System.out.println("Schema: "+ conn.getSchema());
                Statement stmt = conn.createStatement();
                String strSelect = "select * from PurchaseDetail";
                ResultSet rset = stmt.executeQuery(strSelect);
                
                System.out.println("The records selected are:");
                int rowCount = 0;
                while(rset.next()) {   // Move the cursor to the next row
                   int id = rset.getInt("PurchaseID");
                   int orderQty = rset.getInt("OrderQty");
                   Date datePurchase = rset.getDate("DatePurchase");
                   System.out.println(id + ", " + orderQty + ", " + datePurchase.toString());
                   ++rowCount;
                }
                System.out.println("Total number of records = " + rowCount);
            }
 
        } catch (SQLException ex) {
            ex.printStackTrace();   
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
