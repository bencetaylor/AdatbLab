package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import application.Model.ModifyResult;

import java.sql.*;

public class Controller {
	//model instance, controller communicate just with the model
	//Don't use javaFX imports classes, etc.
	private Model model;

	public Controller(){
		model = new Model();
	}

	/**
	 * Connect to DB with model
	 * @param userName Your DB username
	 * @param password Your DB password
	 * @param log Log container
	 * @return true if connect success else false
	 */
	public boolean connect(String userName, String password, List<String> log){
		if (model.connect(userName, password)) {
			// Test the connection
			String results = model.testConnection();
			if (results != null) {
				log.add("Connection seems to be working.");
				log.add("Connected to: '" + model.getDatabaseUrl() + "'");
				log.add(String.format("DBMS: %s, version: %s", model.getDatabaseProductName(),
						model.getDatabaseProductVersion()));
				log.add(results);
				return true;
			}
		}
		//always log
		log.add(model.getLastError());
		return false;
	}

	/**
	 * Task 1: Search with keyword
	 * USE: model.search
	 * Don't forget close the statement!
	 * @param keyword the search keyword
	 * @param log Log container
	 * @return every row in a String[],and the whole table in List<String[]>
	 */
	public List<String[]> search(String keyword, List<String> log){
		List<String[]> result = new ArrayList<>();
		//TODO Task 1
		try {
			// Itt kapjuk meg az eredmenytablat
			ResultSet rs = model.search(keyword);
			// Az osszes soran vegigmegyunk
			while (rs.next()) {
				// Felveszunk egy String[]-ot, amiben az egy sorhoz tartozo adatok lesznek
				String strings[] = new String[9];
				strings[0] = rs.getString(1);
				strings[1] = rs.getString(2);
				strings[2] = rs.getDate(3).toString();
				strings[3] = rs.getString(4);
				// Kis szepeszeti kiegeszites, "n" helyett "nappali"
				if (rs.getString(5).equals("n")) strings[4] = "nappali";
				// "e" helyett "ejjeli" jelenjen meg a tablaban
				else if (rs.getString(5).equals("e")) strings[4] = "ejjeli";
				// Ugyanigy itt is
				if (rs.getString(6).equals("i")) strings[5] = "igen";
				else if (rs.getString(6).equals("n")) strings[5] = "nem";
				strings[6] = Integer.toString(rs.getInt(7));
				strings[7] = Integer.toString(rs.getInt(8));
				// Es itt is
				if (rs.getString(9).equals("i")) strings[8] = "igen";
				else if (rs.getString(9).equals("n")) strings[8] = "nem";
				result.add(strings);
			}
		} catch (SQLException e) {
			log.add(model.getLastError());
		}
		return result;
	}

	/**
	 * Task 2 and 3: Modify data (task 2) and (before) verify(task 3) it, and disable autocommit (task 4.1)
	 * USE: model.modifyData and Model.ModifyResult
	 * @param data Modify data
	 * @param AutoCommit autocommit parameter
	 * @param log Log container
	 * @return true if verify ok else false
	 */
	public boolean modifyData(Map data, boolean AutoCommit, List<String> log){
		Model.ModifyResult result = Model.ModifyResult.Error;
		//TODO Task 2,3,4.1
		// Formatum ellenorzese a feltetel, ha megfelel, meghivhatjuk a modosito fuggvenyt
		if (this.verifyData(data, log)) {
			try {
				// Itt hivodik meg a modosito fuggveny
				result = model.modifyData(data, AutoCommit);
				// Ha beszuras tortent, akkor azt adjuk hozza a loghoz
				if (result == Model.ModifyResult.InsertOccured) {
					log.add("insert occured");
				}
				// Ha frissites tortent, akkor azt adjuk hozza a loghoz
				else if (result == Model.ModifyResult.UpdateOccured){
					log.add("update occured");
				}
				// Ha hiba tortent, akkor kerjuk le a hibauzenetet
				else if (result == Model.ModifyResult.Error) {
					log.add(model.getLastError());
				}
				return true;
			}
			catch (Exception e) {
				log.add(model.getLastError());
				return false;
			}
		}
		else return false;
	}

	/**
	 * Task 5: get statistics
	 * USE: model.getStatistics
	 * Don't forget close the statement!
	 * @param log Log container
	 * @return every row in a String[],and the whole table in List<String[]>
	 */
	public List<String[]> getStatistics(List<String> log){
		List<String[]> result = new ArrayList<>();
		//TODO task 5
		try {
			// Itt kerjuk le az eredmenytablat
			ResultSet rs = model.getStatistics();
			// Hasonloan iteralunk, mint az 1. feladatnal
			while (rs.next()) {
				String[] string = new String[2];
				string[0] = rs.getString(1);
				string[1] = rs.getString(2);
				result.add(string);
			}
			return result;
		}
		catch (SQLException e) {
			log.add(model.getLastError());
			return null;
		}
		catch (NullPointerException e) {
			log.add(model.getLastError());
			return result;
		}
	}

	/**
	 * Commit all uncommitted changes
	 * USE: model.commit
	 * @param log Log container
	 * @return true if model.commit true else false
	 */
	public boolean commit(List<String> log){
		// Ha sikeres a commit, adjon vissza true-t
		if (model.commit()) return true;
		// Maskulonben rollback-eljen es ezt kozolje a felhasznaloval
		else {
			model.rollback();
			log.add("rollback occured");
			return false;
		}
	}

	/**
	 * Verify all fields value
	 * USE it to modifyData function
	 * USE regular expressions, try..catch
	 * @param data Modify data
	 * @param log Log container
	 * @return true if all fields in Map is correct else false
	 */
	private boolean verifyData(Map data, List<String> log) {
		//TODO task 3
		// Napi_ksg attributum formatumanak ellenorzese
		// A hozza tartozo regularis kifejezes
		String reg1 = "[1-9]\\d*";
		// A megvizsgalando String
		String compare1 = (String) data.get("napi_ksg");
		// Itt tortenik az osszehasonlitas
		if (!Pattern.matches(reg1, compare1)) {
			// Ha nem felel meg az elvarasoknak az ellenorzendo String formatuma,
			// kozoljuk a felhasznaloval
			log.add("napi_ksg fieldname syntax error: number format required");
			return false;
		}
		// Vasarlas attributum formatumanak ellenorzese
		// Hasonloan a fentihez
		String reg2 = "[1-9]\\d{3}\\-[0-1]\\d\\-[0-3]\\d";
		String compare2 = (String) data.get("vasarlas");
		if (!Pattern.matches(reg2, compare2)) {
			log.add("vasarlas fieldname syntax error: the format should be like: YYYY-MM-DD");
			return false;
		}
		// Ha egyik esetben sem volt hiba, akkor minden a formatumnak megfelelo
		return true;
	}

}
