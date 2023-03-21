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
import java.io.IOException;
import java.io.InputStream;
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

        tblStudents.getSelectionModel().selectedItemProperty().addListener((ov,prev,current)->{
            btnDelete.setDisable(false);
            txtStudentId.setText(current.getId());
            txtStudentName.setText(current.getName());
            if (current.getPicture() != null) {
                try {
                    InputStream is = current.getPicture().getBinaryStream();
                    Image image = new Image(is);
                    imgPicture.setImage(image);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Image image = new Image("/image/empty-photo.png");
                imgPicture.setImage(image);
            }

        });

        loadStudents();
        btnNewStudent.fire();
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
        imgPicture.setImage(new Image("/image/empty-photo.png"));
        btnClear.setDisable(true);


    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Student selectedStudent = tblStudents.getSelectionModel().getSelectedItem();
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement statement1 = connection.prepareStatement("DELETE FROM Picture WHERE student_id = ?");
            statement1.setString(1, selectedStudent.getId());
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM Student WHERE id = ?");
            statement2.setString(1, selectedStudent.getId());

            statement1.executeUpdate();
            statement2.executeUpdate();

            tblStudents.getItems().remove(tblStudents.getSelectionModel().getSelectedIndex());
            btnNewStudent.fire();
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        System.out.println(tblStudents.getItems());
        String newStudentId = "Dep10/S001";
        if (tblStudents.getItems() != null) {
            String lastStudentId = (tblStudents.getItems().get(tblStudents.getItems().size() - 1).getId().substring(8));
            newStudentId = String.format("Dep10/S%03d", Integer.parseInt(lastStudentId) + 1);
        }

        txtStudentId.setText(newStudentId);
        txtStudentName.clear();
        btnClear.fire();
//        tblStudents.getSelectionModel().clearSelection();


    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) return;
        System.out.println("DataValid");

        Student newStudent = new Student(txtStudentId.getText(), txtStudentName.getText(), null);
        tblStudents.getItems().add(newStudent);
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Student(id, name) VALUES (?,?)");
            PreparedStatement preparedStatementPicture = connection.prepareStatement("INSERT INTO Picture(student_id, picture) VALUES (?,?)");

            preparedStatement.setString(1,txtStudentId.getText());
            preparedStatement.setString(2,txtStudentName.getText());
            preparedStatement.executeUpdate();

            preparedStatementPicture.setString(1,txtStudentId.getText());

            Image image = imgPicture.getImage();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", bos);
            byte[] bytes = bos.toByteArray();
            Blob picture = new SerialBlob(bytes);
            preparedStatementPicture.setBlob(2,picture);
            preparedStatementPicture.executeUpdate();
            newStudent.setPicture(picture);
            btnNewStudent.fire();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
        } catch (IOException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                e.printStackTrace();
            }
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    private boolean isDataValid() {
        return true;
    }

}
