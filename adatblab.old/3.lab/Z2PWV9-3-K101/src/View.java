/**
 * This JavaFX skeleton is provided for the Software Laboratory 5 course. Its structure
 * should provide a general guideline for the students.
 * As suggested by the JavaFX model, we'll have a GUI (view),
 * a controller class (this one) and a model.
 */

package application;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.MapValueFactory;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

// Controller class
public class View {

	private Controller controller;

	@FXML
	private ComboBox<String> tipusComboBox;
	@FXML
	private ComboBox<String> commitComboBox;

	//Layouts
	@FXML
	private VBox rootLayout;
	@FXML
	private HBox connectionLayout;

	//Texts
	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField searchTextField;
	@FXML
	private TextField eszk_azonTextField;
	@FXML
	private TextField nevTextField;
	@FXML
	private TextField markaTextField;
	@FXML
	private TextField napi_ksgTextField;
	@FXML
	private TextField vasarlasTextField;
	@FXML
	private TextArea logTextArea;
	@FXML
	private TextField rend_azonTextField;

	//Buttons
	@FXML
	private Button connectButton;
	@FXML
	private Button commitButton;
	@FXML
	private Button editButton;
	@FXML
	private Button statisticsButton;
	@FXML
	private Button searchButton;


	// Labels
	@FXML
	private Label connectionStateLabel;

	// Tabs
	@FXML
	private Tab editTab;
	@FXML
	private Tab statisticsTab;
	@FXML
	private Tab logTab;
	@FXML
	private Tab searchTab;


	// Tables
	@FXML
	private TableView searchTable;
	@FXML
	private TableView statisticsTable;


	// Titles and map keys of table columns search
	String searchColumnTitles[] = new String[] { "REND_AZON", "MEGRENDELO", "DATUM", "HELYSZIN", "NAPSZAK", "ZENE", "KERETOSSZEG", "VENDEGSZAM", "ORZES" };
	String searchColumnKeys[] = new String[] { "col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9" };

	// Titles and map keys of table columns statistics
	String statisticsColumnTitles[] = new String[] { "MEGRENDELO1", "MEGRENDELO2"};
	String statisticsColumnKeys[] = new String[] { "col1", "col2"};



	/**
	 * View constructor
	 */
	public View() {
		controller = new Controller();
	}

	/**
	 * View initialization, it will be called after view was prepared
	 */
	@FXML
	public void initialize() {

		// Clear username and password textfields and display status
		// 'disconnected'
		usernameField.setText("");
		passwordField.setText("");
		connectionStateLabel.setText("Connection: disconnected");
		connectionStateLabel.setTextFill(Color.web("#ee0000"));

		// Create table (search table) columns
		for (int i = 0; i < searchColumnTitles.length; i++) {
			// Create table column
			TableColumn<Map, String> column = new TableColumn<>(searchColumnTitles[i]);
			// Set map factory
			column.setCellValueFactory(new MapValueFactory(searchColumnKeys[i]));
			// Set width of table column
			column.prefWidthProperty().bind(searchTable.widthProperty().divide(searchColumnTitles.length));
			// Add column to the table
			searchTable.getColumns().add(column);
		}

		// Create table (statistics table) columns
		for (int i = 0; i < statisticsColumnTitles.length; i++) {
			// Create table column
			TableColumn<Map, String> column = new TableColumn<>(statisticsColumnTitles[i]);
			// Set map factory
			column.setCellValueFactory(new MapValueFactory(statisticsColumnKeys[i]));
			// Set width of table column
			column.prefWidthProperty().bind(statisticsTable.widthProperty().divide(statisticsColumnTitles.length));
			// Add column to the table
			statisticsTable.getColumns().add(column);
		}

	}

	/**
	 * Initialize controller with data from AppMain (now only sets stage)
	 *
	 * @param stage
	 *            The top level JavaFX container
	 */
	public void initData(Stage stage) {

		// Set 'onClose' event handler (of the container)
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent winEvent) {
				//TODO 4.2
				// Ideiglenes log letrehozasa
				List<String> log = new ArrayList<>();
				// Commitolas kilepeskor
				controller.commit(log);
			}
		});
	}

	/**
	 * This is called whenever the connect button is pressed
	 *
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void connectEventHandler(ActionEvent event) {
		//Log container
		List<String> log = new ArrayList<>();

		// Controller connect method will do everything for us, just call
		// it
		if (controller.connect(usernameField.getText(), passwordField.getText(), log))
		{
			connectionStateLabel.setText("Connection created");
			connectionStateLabel.setTextFill(Color.web("#009900"));
		}

		//Write log to gui
		for (String string : log) logMsg(string);
	}

	/**
	 * This is called whenever the search button is pressed
	 * Task 1
	 * USE controller search method
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void searchEventHandler(ActionEvent event) {
		//always use log
		List<String> log = new ArrayList<>();
		// Get a reference to the row list of search table
		ObservableList<Map> allRows = searchTable.getItems();
		// Delete all the rows
		allRows.clear();
		// Lekerdezzuk az eredmenytabla sorait
		List<String[]> rows = controller.search(searchTextField.getText(), log);
		// Vegigmegyunk a sorokon
		for (int i = 0; i < rows.size(); i++) {
			// Minden sorba felveszunk egy Map-et, amely majd osszerendeli a megfelelo
			// oszlopnevet az adatokkal
			Map<String, String> dataRow = new HashMap<>();
			// Vegigmegyunk az oszlopokon
			for (int j = 0; j < searchTable.getColumns().size(); j++) {
				dataRow.put(searchColumnKeys[j], (rows.get(i))[j]);
			}
			allRows.add(dataRow);
		}
		//and write it to gui
		for (String string : log) logMsg(string);
	}


	/**
	 * This is called whenever the edit button is pressed
	 * Task 2,3,4
	 * USE controller modify method (verify data in controller !!!)
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void editEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		//TODO task 2,3,4
		// A data-ban adjuk at a kulcsokat es a TextField-ekbol
		// beolvasott reszeket a kiertekelo metodusnak
		Map<String, String> data = new HashMap<>();
		data.put("eszk_azon", eszk_azonTextField.getText());
		data.put("nev", nevTextField.getText());
		data.put("marka", markaTextField.getText());
		data.put("tipus", tipusComboBox.getValue());
		data.put("napi_ksg", napi_ksgTextField.getText());
		data.put("vasarlas", vasarlasTextField.getText());
		data.put("rend_azon", rend_azonTextField.getText());
		// Itt hivodik meg a modosito metodus
		controller.modifyData(data, false, log);
		for (String string : log) logMsg(string);
	}


	/**
	 * This is called whenever the commit button is pressed
	 * Task 4
	 * USE controller commit method
	 * Don't forget SET the commit button disable state
	 * LOG:
	 * 	commit ok: if commit return true
	 *  commit failed: if commit return false
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void commitEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		//TODO task 4
		// Ha sikeres volt a commit, azt felvesszuk a log-ba
		if (controller.commit(log)) log.add("commit ok");
		// Ha pedig nem, akkor azt, hogy nem volt sikeres
		else log.add("commit failed");
		for (String string : log) logMsg(string);
	}



	/**
	 * This is called whenever the statistics button is pressed
	 * Task 5
	 * USE controller getStatistics method
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void statisticsEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		ObservableList<Map> allRows = statisticsTable.getItems();
		allRows.clear();
		// Eredmenytabla lekerdezese
		List<String[]> rows = controller.getStatistics(log);
		// Hasonloan a searchEventHandler() -hez
		for (int i = 0; i < rows.size(); i++) {
			Map<String, String> dataRow = new HashMap<>();
			for (int j = 0; j < statisticsTable.getColumns().size(); j++) {
				dataRow.put(statisticsColumnKeys[j], (rows.get(i))[j]);
			}
			allRows.add(dataRow);
		}
		for (String string : log) logMsg(string);
	}

	/**
	 * Appends the message (with a line break added) to the log
	 *
	 * @param message
	 *            The message to be logged
	 */
	protected void logMsg(String message) {

		logTextArea.appendText(message + "\n");

	}

}
