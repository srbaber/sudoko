package com.baber;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class SudokuUI extends Application  {
    public static final String APP_PNG = "app.png";

    public static Controller controller;


    public static void main(final String[] args)
    {
        launch(args);
    }

    private void initializeController(final Stage primaryStage)
    {
        controller = Controller.getSINGLETON();
        controller.init(primaryStage);
    }

    public Node load(final String filename) throws IOException
    {
        final FXMLLoader loader = new FXMLLoader();
        final InputStream fxmlFile = ResourceUtil.openFile(filename);
        return loader.load(fxmlFile);
    }

    private void registerFA()
    {
        final InputStream fontAwesomeFile = ResourceUtil.openFile("fontawesome-webfont.ttf");
        final GlyphFont fontAwesome = new FontAwesome(fontAwesomeFile);
        GlyphFontRegistry.register(fontAwesome);
    }

    @Override
    public void start(final Stage primaryStage)
    {
        try
        {
            registerFA();

            final BorderPane root = (BorderPane) load("SudokuFX.fxml");
            final Scene jmsSpyScene = new Scene(root, 800, 600);
            final URL applicationCss = getClass().getResource("/application.css");
            if (applicationCss == null)
            {
                log.error("Unable to load application.css");
                return;
            }

            jmsSpyScene.getStylesheets().add(applicationCss.toExternalForm());
            primaryStage.setScene(jmsSpyScene);

            // Set icon on the application bar
            configureDock(primaryStage);

            primaryStage.show();

            initializeController(primaryStage);

            primaryStage.setOnCloseRequest(event -> controller.exit(event));
        }
        catch (final IOException e)
        {
            log.error("Exception:", e);
        }
    }

    /**
     * mac needs a special awt code
     */
    private void configureDock(final Stage primaryStage)
    {
        final Image appIcon = new Image(APP_PNG);
        primaryStage.getIcons().add(appIcon);

        if (Taskbar.isTaskbarSupported())
        {
            final Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE))
            {
                final URL url = Thread.currentThread().getContextClassLoader().getResource(APP_PNG);
                if (url == null)
                {
                    log.error("Unable to assign program icon {}", APP_PNG);
                }
                else
                {
                    final ImageIcon icon = new ImageIcon(url);
                    taskbar.setIconImage(icon.getImage());
                }
            }
        }
    }
}
