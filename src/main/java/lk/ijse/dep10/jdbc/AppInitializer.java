package lk.ijse.dep10.jdbc;

import javafx.application.Application;
import javafx.stage.Stage;
import lk.ijse.dep10.jdbc.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                System.out.println("Database connection is about to close");
                if (DBConnection.getInstance().getConnection() != null && !DBConnection.getInstance().getConnection().isClosed()) {
                    DBConnection.getInstance().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        generateSchemaIfNotExist();

    }
    private void generateSchemaIfNotExist() {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES");
            ArrayList<String> tableNameList = new ArrayList<>();
            while (rst.next()) {
                tableNameList.add(rst.getString(1));
            }
            System.out.println(tableNameList);
            boolean tableExist = tableNameList.containsAll(Set.of("Attendance","Picture","Student","User"));
            System.out.println(tableExist);

            if (!tableExist) {
                stm.execute(readDBScript());
            }


//            if (!rst.next()){
//                InputStream is = getClass().getResourceAsStream("/schema.sql");
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                String line;
//                StringBuilder dbScript = new StringBuilder();
//                while ((line = br.readLine()) != null){
//                    dbScript.append(line).append("\n");
//                }
//                br.close();
//                stm.execute(dbScript.toString());
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readDBScript(){

        try {InputStream is = getClass().getResourceAsStream("/schema.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder dbScript = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                dbScript.append(line).append("\n");
            }
            return dbScript.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
