package application;

import java.net.URL;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import dal.exceptions.ValidationException;
import dal.impl.SzallDal;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import model.Order1;
import model.Person;
import model.Order234;
import model.OrderReport;

/**
 * Class for implementing the logic.
 */
public class Controller implements Initializable {
	private DataAccessLayer<Order1, Order234, OrderReport> dal;

	@FXML
	TextField usernameField;
	@FXML
	TextField passwordField;
	@FXML
	Button connectButton;
	@FXML
	Label connectionStateLabel;
	
	
	@FXML
	ComboBox<ComboBoxItem<String>> searchComboBox;
	
	// Kereso mezo
	@FXML
	TextField searchTextField;
	
	// Tablazat elemei
	@FXML
	TableView<Order1> searchTable;
	@FXML
	TableColumn<Order1, String> orderIDColumn;
	@FXML
	TableColumn<Order1, String> descriptionColumn;
	@FXML
	TableColumn<Order1, String> vehicleTypeColumn;
	@FXML
	TableColumn<Order1, Integer> quantityColumn;
	@FXML
	TableColumn<Order1, String> originColumn;
	@FXML
	TableColumn<Order1, String> destinationColumn;
	@FXML
	TableColumn<Order1, String> deadlineDateColumn;
	
	// Az Edit panel elemei
	@FXML
	TextField orderIdTextField;
	@FXML
	TextField descriptionTextField;
	@FXML
	TextField vehicleTypeTextField;
	@FXML
	ComboBox<ComboBoxItem<String>> vehicleTypeComboBox;
	@FXML
	TextField quantityTextField;
	@FXML
	TextField originTextField;
	@FXML
	TextField destinationTextField;
	@FXML
	TextField deadlineDateTextField;
	@FXML
	Label actionResultStateLabel;	
	
	
	public Controller() {
		dal = new SzallDal();
	}

	@FXML
	public void connectEventHandler(ActionEvent event) {
		//Getting the input from the UI.
		String username = usernameField.getText();
		String password = passwordField.getText();

		try {
			//Connect to the database, and update the UI
			dal.connect(username, password);
			connectionStateLabel.setText("Connection created!");
			connectionStateLabel.setTextFill(Paint.valueOf("green"));
		} catch (ClassNotFoundException e) {
			//Driver is not found
			connectionStateLabel.setText("JDBC driver not found!");
			connectionStateLabel.setTextFill(Paint.valueOf("red"));
		} catch (CouldNotConnectException e) {
			//Could not connect, e.g. invalid username or password
			connectionStateLabel.setText("Could not connect to the server!");
			connectionStateLabel.setTextFill(Paint.valueOf("red"));
		}
	}

	
	/**
	 * Az elso feladatot megvalosito fuggveny
	 */
	@FXML
	public void searchEventHandler() {
		try {
			// A kulcsszo amelyre keresunk
			String keyword = "";
			
			// Teljes egyezes, ekkor a keyword pontosan egyezik a beirt szoveggel
			if( searchComboBox.getValue().getLabel().equals("Full")) {
				keyword = searchTextField.getText();
			}
			
			// Szo eleji egyezes, ekkor a keyword csak a szoveg elejere illeszkedik
			if(searchComboBox.getValue().getLabel().equals("Begin")) {
				//szoveg eleji egyezes
				keyword = searchTextField.getText() + "%";
			}
			
			// Tartalmazo egyezes, ekkor a keyword megtalalhato a keresett szovegben
			if(searchComboBox.getValue().getLabel().equals("Inner")) {
				//a szoveg tartalmazza a kulcsszot
				keyword = "%" + searchTextField.getText() + "%";
			}
			
			List<Order1> orders = dal.search(keyword);
			// feltoltjuk a tablazat elemeit
			searchTable.setItems(FXCollections.observableArrayList(orders));
		}
		catch (NotConnectedException e){
			e.printStackTrace();
		}
		catch (RuntimeException e){
			e.printStackTrace();
		}
	}

	@FXML
	public void commitEventHandler() {
		//TODO: handle the click of the commit button.
	}

	@FXML
	public void editEventHandler() {
		try 
		{
			// Letrehozunk egy uj valtozot, amellyel meghivjuk az insertOrUpdate fuggvenyt
			Order234 newOrder = new Order234();
			
			// Az adatokat a parseXXX fuggvennyel inicializaljuk
			newOrder.parseOrderId(orderIdTextField.getText());
			newOrder.parseDescription(descriptionTextField.getText());
			newOrder.parseVehicleType(vehicleTypeComboBox.getValue().getLabel());
			newOrder.parseQuantity(quantityTextField.getText());
			newOrder.parseOrigin(originTextField.getText());
			newOrder.parseDestination(destinationTextField.getText());
			newOrder.parseDeadline(deadlineDateTextField.getText());
			
			// Meghivjuk az insertOrUpdate fuggvenyt, majd a vegeredmenyrol ertesitjuk a felhasznalot
			ActionResult ar = dal.insertOrUpdate(newOrder, null);
			
			actionResultStateLabel.setText(ar.toString());
			actionResultStateLabel.setTextFill(Paint.valueOf("green"));
			
		}
		catch (NotConnectedException e) {
			e.printStackTrace();
		}
		catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		catch (ValidationException e) {
			if(e.getFieldName().equals("deadline")){
				// Hibas datum kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("Hibas a datum formatuma! Probalja ujra!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("orderId")){
				// Hibas orderID kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("Hibas az OrderID formatuma!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("quantity")){
				// Hibas mennyiseg kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("Kerem szamot irjon a mezobe!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

	@FXML
	public void statisticsEventHandler() {
		//TODO: handle the click of the statistics button
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// A kereso comboBox beallitasa
		searchComboBox.getItems().add(new ComboBoxItem<String>("Full", "a"));
		searchComboBox.getItems().add(new ComboBoxItem<String>("Begin", "b"));
		searchComboBox.getItems().add(new ComboBoxItem<String>("Inner", "c"));
		
		searchTextField.setPromptText("Chose a search pattern first!");
		
		// A jarmutipusok kivalasztasat segito comboBox
		vehicleTypeComboBox.getItems().add(new ComboBoxItem<String>("A", "A"));
		vehicleTypeComboBox.getItems().add(new ComboBoxItem<String>("C", "C"));
		vehicleTypeComboBox.getItems().add(new ComboBoxItem<String>("D", "D"));
		vehicleTypeComboBox.getItems().add(new ComboBoxItem<String>("L", "L"));
		vehicleTypeComboBox.getItems().add(new ComboBoxItem<String>("T", "T"));
		
		// letrehozzuk a tablazatot
		orderIDColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
		vehicleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		originColumn.setCellValueFactory(new PropertyValueFactory<>("origin"));
		destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
		deadlineDateColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
		
		// A kritikus mezoknek pelda mintat adunk
		orderIdTextField.setPromptText("yyyy/xxxxxx");
		quantityTextField.setPromptText("number only");
		deadlineDateTextField.setPromptText("YYYY-MM-DD");
		
	}

	public void disconnect() {
		dal.disconnect();
	}

}
