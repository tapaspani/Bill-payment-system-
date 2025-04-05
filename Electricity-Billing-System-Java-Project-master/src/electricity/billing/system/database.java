package electricity.billing.system;

import java.sql.*;
import java.sql.DriverManager;


public class database {
    Connection connection;
    Statement statement;
    database(){
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/BIll_system1","root","Tapas@63");
            statement = connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


