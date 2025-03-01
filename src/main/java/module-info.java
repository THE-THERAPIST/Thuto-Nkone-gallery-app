module com.example.imagegalleryapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.imagegalleryapplication to javafx.fxml;
    exports com.example.imagegalleryapplication;
}