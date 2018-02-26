/**
 * This JavaFX skeleton is provided for the Software Laboratory 5 course. Its structure
 * should provide a general guideline for the students.
 * As suggested by the JavaFX model, we'll have a GUI (view),
 * a controller class and a model (this one).
 */

package application;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Random;


// Model class
public class Model {

	// Database driver and URL
	protected static final String driverName = "oracle.jdbc.driver.OracleDriver";
	protected static final String databaseUrl = "jdbc:oracle:thin:@rapid.eik.bme.hu:1521:szglab";

	// Product name and product version of the database
	protected String databaseProductName = null;
	protected String databaseProductVersion = null;

	// Connection object
	protected Connection connection = null;

	// Enum structure for Exercise #2
	protected enum ModifyResult {
		InsertOccured, UpdateOccured, Error
	}

	// String containing last error message
	protected String lastError = "";
	
	// Boolean tarolja a hibakat/vagy azt, hogy nem volt hiba
	private boolean commitready = false;

	/**
	 * Model constructor
	 */
	public Model() {
	}

	/**
	 * Gives product name of the database
	 *
	 * @return Product name of the database
	 */
	public String getDatabaseProductName() {

		return databaseProductName;

	}

	/**
	 * Gives product version of the database
	 *
	 * @return Product version of the database
	 */
	public String getDatabaseProductVersion() {

		return databaseProductVersion;

	}

	/**
	 * Gives database URL
	 *
	 * @return Database URL
	 */
	public String getDatabaseUrl() {

		return databaseUrl;

	}

	/**
	 * Gives the message of last error
	 *
	 * @return Message of last error
	 */
	public String getLastError() {

		return lastError;

	}

	/**
	 * Tries to connect to the database
	 *
	 * @param userName
	 *            User who has access to the database
	 * @param password
	 *            User's password
	 * @return True on success, false on fail
	 */
	public boolean connect(String userName, String password) {

		try {

			// If connection status is disconnected
			if (connection == null || !connection.isValid(30)) {

				if (connection == null) {

					// Load the specified database driver
					Class.forName(driverName);

					// Driver is for Oracle 12cR1 (certified with JDK 7 and JDK
					// 8)
					if (java.lang.System.getProperty("java.vendor").equals("Microsoft Corp.")) {
						DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
					}
				} else {

					connection.close();

				}

				// Create new connection and get metadata
				connection = DriverManager.getConnection(databaseUrl, userName, password);
				DatabaseMetaData dbmd = connection.getMetaData();

				databaseProductName = dbmd.getDatabaseProductName();
				databaseProductVersion = dbmd.getDatabaseProductVersion();

			}

			return true;

		} catch (SQLException e) {

			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return false;

		} catch (ClassNotFoundException e) {
			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return false;

		}

	}

	/**
	 * Tests the database connection by submitting a query
	 *
	 * @return True on success, false on fail
	 */
	public String testConnection() {

		try {

			// Create SQL query and execute it
			// If user input has to be processed, use PreparedStatement instead!
			Statement stmt = connection.createStatement();
			ResultSet rset = stmt.executeQuery("SELECT count(*) FROM oktatas.igazolvanyok");

			// Process the results
			String result = null;
			while (rset.next()) {
				result = String.format("Total number of rows in 'Igazolvanyok' table in 'Oktatas' schema: %s",
						rset.getString(1));
			}

			// Close statement
			stmt.close();

			return result;

		} catch (SQLException e) {
			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return null;

		}
	}

	/**
	 * Method for Exercise #1
	 * @param keyword Search keyword
	 * @return Result of the query
	 */
	public ResultSet search(String keyword) {
		//TODO task 1
		try {
			// Ebben lesz tarolva az eredmenytabla
			ResultSet rs = null;
			// Ha nem adott meg a felhasznalo keresesi erteket
			if (keyword.equals("")) {
				// Akkor listazzuk a tabla osszes elemet
				Statement st = connection.createStatement();
				rs = st.executeQuery("SELECT * FROM rendezvenyek");
			}
			else {
				// Egyebkent a kulcsszora tortenjen szures
				PreparedStatement ps = connection.prepareStatement("SELECT * FROM rendezvenyek WHERE rend_azon LIKE ? ESCAPE '@'");
				// Ahhoz, hogy a specialis karakterek kereshetoseget lehetove tegyuk
				// olyasmi fog kelleni, mint pl.: javaban a "\\"
				// Nalam ezt a szerepet a "@" fogja szolgalni
				keyword = keyword.replaceAll("%", "@%");
				keyword = keyword.replaceAll("_", "@_");
				// Azt is figyelembe vesszuk, hogy csonka a megadott keresesi feltetel
				ps.setString(1, "%" + keyword + "%");
				rs = ps.executeQuery();
			}
			return rs;
		} catch (SQLException e) {
			// Ha barmilyen SQL hiba tortent a muveletek soran, azt jelezzuk a felhasznalonak
			lastError = "Search error " + e.toString();
			return null;
		}
	}

	/**
	 * Method for Exercise #2-#3
	 *
	 * @param data
	 *            New or modified data
	 * @param AutoCommit set the connection type (use default true, and 4.1 use false
	 * @return Type of action has been performed
	 */
	public ModifyResult modifyData(Map data, boolean AutoCommit) {
		ModifyResult result = ModifyResult.Error;
		//TODO task 2,3,4.1
		try {
			// Beallitjuk hogy legyen-e autocommit
			connection.setAutoCommit(AutoCommit);
			// Ezzel a lekerdezessel dontjuk el, hogy insert, vagy update muvelet fog kelleni
			// A lenyege: ha mar szerepel a eszkozok tablaban olyan rekord, amelynek az azonositoja
			// 			  megegyezik a felhasznalo altal megadotteval, akkor update, egyebkent insert
			PreparedStatement st0 = connection.prepareStatement("SELECT eszk_azon FROM eszkozok WHERE eszk_azon LIKE ?");
			st0.setString(1, (String) data.get("eszk_azon"));
			ResultSet rs0 = st0.executeQuery();
			// Az next() metodus true-val ter vissza, ha van legalabb 1 sor az eredmenytablaban, tehat ha !next(),
			// akkor az eredmenytabla ures, insert muvelet lesz
			if (!(rs0.next())) {
				// Most, hogy mar tudjuk, hogy insert lesz, gyorsan be is allitjuk a visszateresi ertekre
				result = Model.ModifyResult.InsertOccured;
				// Ez a String tartalmazza a parameteres lekerdezest
				String sql = "INSERT INTO eszkozok VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement st = connection.prepareStatement(sql);
				// Parameterek felvetele
				st.setString(1, (String) data.get("eszk_azon"));
				st.setString(2, (String) data.get("nev"));
				st.setString(3, (String) data.get("marka"));
				st.setString(4, (String) data.get("tipus"));
				st.setInt(5, Integer.parseInt((String) data.get("napi_ksg")));
				try {
					// A datum formatumanak felvetele
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					// A datumot ugy vesszuk fel, hogy a String-kent atadott datumot az elobb
					// megadott formatum szerint olvassuk ki
					java.util.Date date0 = sdf.parse((String) data.get("vasarlas"));
					// Converter java.util.Date és java.sql.Date kozott
					java.sql.Date date1 = new Date(date0.getTime());
					st.setDate(6, date1);
				} catch (java.text.ParseException e) {
					// Ha valami nem sikerult a datum formatumanak ellenorzese soran, itt jelezzuk,
					// es vissza is terunk
					lastError = "Wrong DateFormat " + e.toString();
					commitready = false;
					return Model.ModifyResult.Error;
				}
				// Itt mar vegre tudjuk hajtani az insert utasitast
				st.executeUpdate();
				// Miutan megvan az uj eszkoz felvetele, akkor a foglalast kell elintezni
				// Eloszor ellenorizzuk, hogy kitoltotte-e a felhasznalo a rend_azon mezojet
				if (data.get("rend_azon") != null) {
					// Most leellenorizzuk, hogy jo rend_azon-t adott-e meg
					// a mar fentebb megismert modon
					PreparedStatement st1 = connection.prepareStatement("SELECT rend_azon FROM rendezvenyek WHERE rend_azon = ?");
					st1.setString(1, (String) data.get("rend_azon"));
					ResultSet rs1 = st1.executeQuery();
					if (rs1.next()) {
						// Ha sikerult jo rend_azon-t megadnia, akkor felvehetjuk a foglalast
						// ehhe az alabbi parameteres lekerdezest hasznaltam fel:
						PreparedStatement ps = connection.prepareStatement("INSERT INTO foglalas VALUES (?, ?, 'Herceg', 'busz', 'busz', Null)");
						// Itt beallitjuk a parametereket
						ps.setString(1, (String) data.get("eszk_azon"));
						ps.setString(2, (String) data.get("rend_azon"));
						// Vegrehajtjuk a muveletet
						ps.executeQuery();
						// Ha eddig nem volt hiba, ezutan se lesz, a muvelet commit ready
						commitready = true;
					}
					else {
						// Itt ertesitjuk a felhasznalot, hogyha olyan rend_azon-t adott meg, ami meg nem letezik
						lastError = "error rend_azon does not exist yet";
						// Termeszetesen ekkor a muvelet nem commit ready, nem engedelyehetunk commitot
						commitready = false;
						return Model.ModifyResult.Error;
					}
				}
				else {
					// Itt ertesitjuk a felhasznalot, hogyha nem adott meg rend_azon-t
					lastError = "error rend_azon field is empty";
					commitready = false;
					return Model.ModifyResult.Error;
				}
			}
			else {
				// Ha az eredménytabla nem ures, akkor mindenfelekeppen update tortent
				// at is allitjuk a visszateresi erteket
				result = Model.ModifyResult.UpdateOccured;
				// Az parameteres SQL muvelet szovege
				String sql = "UPDATE eszkozok SET nev = ?, marka = ?, tipus = ?, napi_ksg = ?, vasarlas = ? WHERE eszk_azon LIKE ?";
				PreparedStatement st = connection.prepareStatement(sql);
				// Parameterek beallitasa
				st.setString(1, (String) data.get("nev"));
				st.setString(2, (String) data.get("marka"));
				st.setString(3, (String) data.get("tipus"));
				st.setInt(4, Integer.parseInt((String) data.get("napi_ksg")));
				try {
					// Datum formazasa, mint korabban
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					java.util.Date date0 = sdf.parse((String) data.get("vasarlas"));
					java.sql.Date date1 = new Date(date0.getTime());
					st.setDate(5, date1);
				} catch (java.text.ParseException e) {
					lastError = "Wrong DateFormat " + e.toString();
					commitready = false;
					return Model.ModifyResult.Error;
				}
				st.setString(6, (String) data.get("eszk_azon"));
				// Muvelet vegrehajtasa
				st.executeUpdate();
				// Commit ready
				commitready = true;
			}
			// Bezarhatjuk a muveletet
			st0.close();
			// Visszaadjuk a megkapott erteket
			return result;
		}
		catch (SQLException e) {
			// Ha a muvelet vegrehajtasa kozben barmifele SQL hiba tortent, azt jelezzuk a felhasznalonak 
			lastError = "Modify SQL error " + e.toString();
			// Termeszetesen ekkor nem engeduk commitolni
			commitready = false;
			// Es hibat is dobunk vissza
			return Model.ModifyResult.Error;
		}
	}


	/**
	 * Method for Exercise #4
	 *
	 * @return True on success, false on fail
	 */
	public boolean commit() {
		//TODO task 4
		// Ha a tranzakcio commit ready, mert nem tortent problema a felvetellel, frissitessel
		if (commitready == true) {
			try {
				// Commitolunk
				connection.commit();
				// Es ha sikeres volt, true-val terunk vissza
				return true;
			} catch (SQLException e) {
				// Ha nem sikerult, tudatjuk a felhasznaloval
				lastError = "Commit error " + e.toString();
				// Es false-szal terunk vissza
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Method for Exercise #4
	 */
	public void rollback(){
		//TODO task 4
		try {
			// Rollback-eles
			connection.rollback();
		}
		catch (SQLException e) {
			// Ha nem sikerult, tudatjuk a felhasznaloval
			lastError = "Rollback error " + e.toString();
		}
	}

	/**
	 * Method for Exercise #5
	 *
	 * @return Result of the query
	 */
	public ResultSet getStatistics() {
		//TODO task 5
		try {
			// Az eredmenytabla ebben lesz tarolva
			ResultSet rs = null;
			// A lekerdezes szovege
			String sql = "SELECT DISTINCT a.megrendelo, b. megrendelo FROM rendezvenyek a, rendezvenyek b, eszkozok, foglalas c, foglalas d WHERE a.rend_azon = c.hova AND c.mit = d.mit AND c.mit = eszkozok.ESZK_AZON  AND eszkozok.nev = 'Peacekép' AND c.hova != d.hova AND c.hova = a.rend_azon AND d.hova = b.rend_azon";
			Statement st = connection.createStatement();
			// Itt megkapjuk az eredmenytablankat
			rs = st.executeQuery(sql);
			// Amit vissza is adunk
			return rs;
		} catch (SQLException e) {
			// Ha barmifele SQL hiba tortent, akkor arrol a felhasznalot tajekoztatjuk
			lastError = "Statistics error " + e.toString();
			return null;
		}
	}

}
