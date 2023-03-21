package lk.ijse.dep10.jdbc.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import lk.ijse.dep10.jdbc.db.DBConnection;
import lk.ijse.dep10.jdbc.model.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.sql.*;

public class StudentViewController {
    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<Student> tblStudents;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtStudentId;

    @FXML
    private TextField txtStudentName;
    public void initialize(){
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("picture"));

        loadStudents();
    }

    private void loadStudents() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm1 = connection.createStatement();
            ResultSet rst = stm1.executeQuery("SELECT * FROM Student");
            PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM Picture WHERE student_id = ?");
            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                Blob picture = null;

                stm2.setString(1,id);
                ResultSet rstPicture = stm2.executeQuery();
                if (rstPicture.next()) {
                    picture = rstPicture.getBlob("picture");
                }
                Student student = new Student(id, name, picture);
                tblStudents.getItems().add(student);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Student Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg","*.jpeg","*.gif","*.dmp"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());

        if (file != null) {
            try {
                Image image = new Image(String.valueOf(file.toURI().toURL()));
                imgPicture.setImage(image);
                btnClear.setDisable(false);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        Image image = new Image("/image/empty-photo.png");
        imgPicture.setImage(image);
        btnClear.setDisable(true);


    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {

    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        String lastStudentId = (tblStudents.getItems().get(tblStudents.getItems().size()-1).getId().substring(8));
        String newStudentId = String.format("Dep10/S%03d",Integer.parseInt(lastStudentId)+1);

        txtStudentId.setText(newStudentId);
        txtStudentName.clear();
        btnClear.fire();

    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {



    }

    private boolean isDataValid() {
        return true;
    }

}
