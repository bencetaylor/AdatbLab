package application;

import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import dal.exceptions.ValidationException;
import dal.impl.VasutDal;
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
import model.JaratFejlec;
import model.Jarat;
import model.JaratVonatszam;

/**
 * Class for implementing the logic.
 */
public class Controller implements Initializable {
	private DataAccessLayer<JaratFejlec, Jarat, JaratVonatszam> dal;

	@FXML
	TextField usernameField;
	@FXML
	TextField passwordField;
	@FXML
	Button connectButton;
	@FXML
	Label connectionStateLabel;
	@FXML
	ComboBox<ComboBoxItem<String>> sampleCombo;

	@FXML
	TableColumn<JaratFejlec, String> vonatszamColumn;

	@FXML
	TableColumn<JaratFejlec, String> tipusColumn;
	
	@FXML
	TableColumn<JaratFejlec, String> megjegyzesColumn;
	
	@FXML
	TextField searchTextField;

	@FXML
	TableView<JaratFejlec> searchTable;
	
	// Edit panel mezoi
	@FXML
	TextField vonatszamTextField;
	
	@FXML
	ComboBox<ComboBoxItem<String>> tipusComboBox;
	
	@FXML
	TextField napTextField;
	
	@FXML
	TextField kezdTextField;
	
	@FXML
	TextField vegeTextField;
	
	@FXML
	TextField megjegyzesTextField;
	
	@FXML
	TextField allomasTextField;
	
	@FXML
	Label actionResultStateLabel;

	public Controller() {
		dal = new VasutDal();
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

	@FXML
	public void searchEventHandler() {
		try {
			String keyword = "";
			
			List<JaratFejlec> jaratok;
			
			if(searchTextField.getText().equals("")) {
				jaratok = dal.search(keyword);
			}
			else {
				keyword = "%" + searchTextField.getText() + "%";
				jaratok = dal.search(keyword);
			}
			searchTable.setItems(FXCollections.observableArrayList(jaratok));
			
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void commitEventHandler() {
		try {
			if(dal.commit()){
				actionResultStateLabel.setText("CommitOccured");
				actionResultStateLabel.setTextFill(Paint.valueOf("green"));
			}
			else{
				dal.rollback();
				actionResultStateLabel.setText("ErrorOccured, Rollback!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void editEventHandler() {
		try 
		{
			dal.setAutoCommit(false);
			// Letrehozunk egy uj valtozot, amellyel meghivjuk az insertOrUpdate fuggvenyt
			Jarat jarat = new Jarat();
			
			// Az adatokat a parseXXX fuggvennyel inicializaljuk
			jarat.parseVonatszam(vonatszamTextField.getText());
			jarat.parseTipus(tipusComboBox.getValue().getLabel());
			jarat.parseNap(napTextField.getText());
			jarat.parseKezd(kezdTextField.getText());
			jarat.parseVege(vegeTextField.getText());
			jarat.parseMegjegyzes(megjegyzesTextField.getText());
			
			
			// Meghivjuk az insertOrUpdate fuggvenyt, majd a vegeredmenyrol ertesitjuk a felhasznalot
			ActionResult actionResult = dal.insertOrUpdate(jarat, parseAllomas(allomasTextField.getText()));
			
			actionResultStateLabel.setText(actionResult.toString());
			//actionResultStateLabel.setTextFill(Paint.valueOf("green"));
			
		}
		catch (NotConnectedException e) {
			e.printStackTrace();
		}
		catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		catch (ValidationException e) {
			if(e.getFieldName().equals("vege")){
				// Hibas datum kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("A vege datum formatuma hibas!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("kezd")){
				// Hibas datum kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("A kezd datum formatuma hibas!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("vonatszam")){
				// Hibas orderID kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("A vonatszam hibas!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("quantity")){
				// Hibas mennyiseg kivetel elkapasa - ertesitjuk a felhasznalot
				actionResultStateLabel.setText("Kerem szamot irjon a mezobe!");
				actionResultStateLabel.setTextFill(Paint.valueOf("red"));
			}
			if(e.getFieldName().equals("allomas")){
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
		searchTextField.setPromptText("Ird be a vonatszamot");
		
		vonatszamColumn.setCellValueFactory(new PropertyValueFactory<>("vonatszam"));
		tipusColumn.setCellValueFactory(new PropertyValueFactory<>("tipus"));
		megjegyzesColumn.setCellValueFactory(new PropertyValueFactory<>("megjegyzes"));
		
		//vonatszamTextField.setPromptText("number only!");
		tipusComboBox.getItems().add(new ComboBoxItem<String>("Gyors", "Gyors"));
		tipusComboBox.getItems().add(new ComboBoxItem<String>("IC", "IC"));
		tipusComboBox.getItems().add(new ComboBoxItem<String>("EC", "EC"));
		tipusComboBox.getItems().add(new ComboBoxItem<String>("Szemely", "szemely"));
		napTextField.setPromptText("0111011");
		kezdTextField.setPromptText("YYYY-MM-DD");
		vegeTextField.setPromptText("YYYY-MM-DD");
		allomasTextField.setPromptText("5 digit number!");
		//megjegyzesTextField.setPromptText("megjegyzes");
	}

	public void disconnect() {
		dal.disconnect();
	}
	
	public int parseAllomas(String allomas) throws ValidationException{
		if (!Pattern.matches("[0-9]\\d{0,4}", allomas)) {
			throw new ValidationException("allomas");		
		}
		else
			return Integer.parseInt(allomas);
	}

}
