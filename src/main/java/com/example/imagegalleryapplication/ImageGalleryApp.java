package com.example.imagegalleryapplication;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;

public class ImageGalleryApp extends Application {
    private static final String IMAGE_DIRECTORY_PATH = "src/main/resources/Images";
    private static final Duration FADE_DURATION = Duration.millis(500);
    private Map<String, List<String>> categorizedImages;
    private GridPane thumbnailGrid;
    private BorderPane root;
    private ScrollPane mainScrollPane; // Field to hold the thumbnail scrollpane

    // For favorite feature: store the paths that are marked as favorite.
    private Set<String> favoriteImages = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        categorizedImages = loadCategorizedImages();
        root = new BorderPane();

        Label galleryTitle = new Label("LIMKOKWING GALLERY");
        galleryTitle.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-font-family: 'Trebuchet MS'; -fx-underline: true;");
        galleryTitle.setEffect(new DropShadow(10, Color.DARKGRAY));

        VBox topBar = new VBox(galleryTitle);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);");
        root.setTop(topBar);

        VBox categoryMenu = new VBox(10);
        categoryMenu.setPadding(new Insets(15));
        categoryMenu.setStyle("-fx-background-color: linear-gradient(to bottom, #3a3a3a, #333333); -fx-padding: 20; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 10;");
        categoryMenu.setAlignment(Pos.TOP_LEFT);

        // Smooth transition between categories added on category button action, plus a loading overlay.
        for (String category : categorizedImages.keySet()) {
            Button categoryButton = new Button(category);
            categoryButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS'; -fx-border-radius: 5; -fx-cursor: hand;");
            categoryButton.setMaxWidth(Double.MAX_VALUE);

            categoryButton.setOnMouseEntered(e -> {
                categoryButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS'; -fx-border-radius: 5; -fx-cursor: hand;");
                categoryButton.setEffect(new DropShadow(10, Color.WHITE));
            });
            categoryButton.setOnMouseExited(e -> {
                categoryButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS'; -fx-border-radius: 5; -fx-cursor: hand;");
                categoryButton.setEffect(null);
            });

            categoryButton.setOnAction(e -> {
                // Fade out current grid.
                FadeTransition fadeOut = new FadeTransition(FADE_DURATION, mainScrollPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    // Show loading overlay
                    Label loadingLabel = new Label("Loading...");
                    loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-family: 'Comic Sans MS';");
                    StackPane loadingPane = new StackPane(loadingLabel);
                    loadingPane.setAlignment(Pos.CENTER);
                    loadingPane.setStyle("-fx-background: linear-gradient(to bottom right, #111111, #333333); -fx-padding: 15;");
                    root.setCenter(loadingPane);

                    PauseTransition pause = new PauseTransition(Duration.millis(300));
                    pause.setOnFinished(ev -> {
                        displayCategoryImages(category);
                        // Restore the main scroll pane with fade in.
                        root.setCenter(mainScrollPane);
                        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, mainScrollPane);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    });
                    pause.play();
                });
                fadeOut.play();
            });
            categoryMenu.getChildren().add(categoryButton);
        }
        root.setLeft(categoryMenu);

        thumbnailGrid = new GridPane();
        thumbnailGrid.setHgap(15);
        thumbnailGrid.setVgap(15);
        // Display the first category by default
        displayCategoryImages(categorizedImages.keySet().iterator().next());

        mainScrollPane = new ScrollPane(thumbnailGrid);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background: linear-gradient(to bottom right, #111111, #333333); -fx-padding: 15;");
        root.setCenter(mainScrollPane);

        // Create a background pane and animate it.
        Pane backgroundPane = new Pane();
        backgroundPane.setPrefSize(1100, 700);
        backgroundPane.setStyle("-fx-background-color: linear-gradient(to bottom, #222222, #111111);");

        Timeline bgTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(backgroundPane.opacityProperty(), 0.9)),
                new KeyFrame(Duration.seconds(5), new KeyValue(backgroundPane.opacityProperty(), 1.0))
        );
        bgTimeline.setCycleCount(Animation.INDEFINITE);
        bgTimeline.setAutoReverse(true);
        bgTimeline.play();

        // Put background and main UI into a StackPane.
        StackPane mainStack = new StackPane(backgroundPane, root);

        Scene scene = new Scene(mainStack, 1100, 700);
        primaryStage.setTitle("LIMKOKWING GALLERY");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Now passes the current category and index to createThumbnail.
    private void displayCategoryImages(String category) {
        thumbnailGrid.getChildren().clear();
        List<String> images = categorizedImages.getOrDefault(category, Collections.emptyList());

        for (int i = 0; i < images.size(); i++) {
            String path = images.get(i);
            // Wrap thumbnail and caption in a StackPane.
            StackPane thumbWrapper = new StackPane();
            ImageView thumbnailView = createThumbnail(path, i, category);
            Label caption = new Label(new File(path).getName());
            caption.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-font-size: 10px; -fx-font-family: 'Comic Sans MS';");
            StackPane.setAlignment(caption, Pos.BOTTOM_CENTER);
            thumbWrapper.getChildren().addAll(thumbnailView, caption);
            thumbnailGrid.add(thumbWrapper, i % 5, i / 5);
        }
    }

    // Modified to include interactive hover border effect and additional parameters for carousel.
    private ImageView createThumbnail(String path, int index, String category) {
        Image thumbnail = new Image("file:" + path, 120, 120, true, true);
        ImageView imageView = new ImageView(thumbnail);
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);

        imageView.setOnMouseEntered(e -> {
            imageView.setScaleX(1.1);
            imageView.setScaleY(1.1);
            imageView.setEffect(new DropShadow(20, Color.SKYBLUE));
            imageView.setStyle("-fx-border-color: #ff6347; -fx-border-width: 3;");
        });
        imageView.setOnMouseExited(e -> {
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
            imageView.setEffect(null);
            imageView.setStyle("-fx-border-color: transparent;");
        });
        imageView.setOnMouseClicked(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), imageView);
            scaleTransition.setToX(1.3);
            scaleTransition.setToY(1.3);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);
            scaleTransition.play();
            showFullSizeImage(path, index, category);
        });

        return imageView;
    }

    // Updated to support carousel and extra features: index label, favorite toggle.
    private void showFullSizeImage(String path, int index, String category) {
        List<String> images = categorizedImages.get(category);
        ImageView fullSizeImageView = new ImageView(new Image("file:" + path));
        fullSizeImageView.setPreserveRatio(true);
        fullSizeImageView.setFitWidth(900);
        fullSizeImageView.setStyle("-fx-effect: dropshadow(gaussian, deepskyblue, 20, 0.5, 0, 0);");

        BorderPane imagePane = new BorderPane();
        imagePane.setCenter(fullSizeImageView);
        imagePane.setStyle("-fx-background-color: #111; -fx-padding: 15; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: deepskyblue; -fx-border-width: 2;");

        // Index label at the top (Feature 2)
        Label indexLabel = new Label("Image " + (index + 1) + " of " + images.size());
        indexLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: 'Comic Sans MS';");
        BorderPane.setAlignment(indexLabel, Pos.TOP_CENTER);
        imagePane.setTop(indexLabel);
        BorderPane.setMargin(indexLabel, new Insets(10));

        // Favorite toggle button (Feature 3)
        Button favoriteButton = new Button(favoriteImages.contains(path) ? "♥" : "♡");
        favoriteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 24px; -fx-cursor: hand;");
        favoriteButton.setOnAction(e -> {
            if (favoriteImages.contains(path)) {
                favoriteImages.remove(path);
                favoriteButton.setText("♡");
            } else {
                favoriteImages.add(path);
                favoriteButton.setText("♥");
            }
        });
        BorderPane.setAlignment(favoriteButton, Pos.TOP_RIGHT);
        imagePane.setRight(favoriteButton);
        BorderPane.setMargin(favoriteButton, new Insets(10));

        // Create Previous button for carousel.
        Button prevButton = new Button("Previous");
        prevButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Comic Sans MS'; -fx-cursor: hand;");
        prevButton.setOnAction(e -> {
            int prevIndex = (index - 1 + images.size()) % images.size();
            showFullSizeImage(images.get(prevIndex), prevIndex, category);
        });

        // Create Next button for carousel.
        Button nextButton = new Button("Next");
        nextButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Comic Sans MS'; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            int nextIndex = (index + 1) % images.size();
            showFullSizeImage(images.get(nextIndex), nextIndex, category);
        });

        // Create a return button with creative styling.
        Button returnButton = new Button("Return to Thumbnail");
        returnButton.setStyle("-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-font-family: 'Comic Sans MS'; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        returnButton.setOnMouseEntered(e -> {
            returnButton.setScaleX(1.1);
            returnButton.setScaleY(1.1);
            returnButton.setEffect(new DropShadow(10, Color.WHITE));
        });
        returnButton.setOnMouseExited(e -> {
            returnButton.setScaleX(1.0);
            returnButton.setScaleY(1.0);
            returnButton.setEffect(null);
        });
        returnButton.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(FADE_DURATION, imagePane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> root.setCenter(mainScrollPane));
            fadeOut.play();
        });

        HBox navBox = new HBox(15, prevButton, returnButton, nextButton);
        navBox.setAlignment(Pos.CENTER);
        navBox.setPadding(new Insets(15));
        imagePane.setBottom(navBox);

        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, imagePane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        root.setCenter(imagePane);
    }

    private Map<String, List<String>> loadCategorizedImages() {
        Map<String, List<String>> categorizedImages = new LinkedHashMap<>();
        File mainDir = new File(IMAGE_DIRECTORY_PATH);

        if (mainDir.exists() && mainDir.isDirectory()) {
            for (File categoryFolder : Objects.requireNonNull(mainDir.listFiles(File::isDirectory))) {
                List<String> images = new ArrayList<>();
                for (File imageFile : Objects.requireNonNull(categoryFolder.listFiles((d, name) -> name.matches(".*\\.(jpg|jpeg|png|gif)")))) {
                    images.add(imageFile.getAbsolutePath());
                }
                categorizedImages.put(categoryFolder.getName(), images);
            }
        }
        return categorizedImages;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
