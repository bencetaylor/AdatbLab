/**
 * This JavaFX skeleton is provided for the Software Laboratory 5 course. Its structure
 * should provide a general guideline for the students.
 * It contains the entry point of the application.
 */

package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Application class
 * This class includes the entry point of the application
 */
public class AppMain extends Application implements EventHandler<WindowEvent> {
	/**
	 * 
	 */
	private Controller controller;

	/**
	 * Display GUI window
	 *
	 * @param primaryStage
	 *            The top level JavaFX container
	 *
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		try {

			// Create a loader object and load View and Controller
			final FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("resources/View.fxml"));
			final VBox viewRoot = (VBox) loader.load();
			// Get controller object
			controller = loader.getController();
			// Set scene (and the title of the window) and display it
			Scene scene = new Scene(viewRoot);
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(this);
			primaryStage.setTitle("MyJwsApplication");
			primaryStage.show();

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	/**
	 * Entry point for the application
	 *
	 * @param args
	 *            Command-line arguments
	 */
	public static void main(String[] args) {
		launch(args);

	}

	/**
	 * It is called before application is stopped
	 */
	@Override
	public void stop() {
		controller.disconnect();
	}

	/**
	 * Handling the closing of the main window.
	 */
	@Override
	public void handle(WindowEvent event) {
		// TODO Task 4.2
	}

}
