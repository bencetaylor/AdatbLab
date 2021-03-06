package dal.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import model.JaratFejlec;
import model.Person;
import model.Jarat;
import model.JaratVonatszam;

/**
 * Initial implementation for the DataAccessLayer for the 30-VASUT exercise.
 */
public class VasutDal implements DataAccessLayer<JaratFejlec, Jarat, JaratVonatszam> {
	private Connection connection;
	protected static final String driverName = "oracle.jdbc.driver.OracleDriver";
	protected static final String databaseUrl = "jdbc:oracle:thin:@rapid.eik.bme.hu:1521:szglab";

	private void checkConnected() throws NotConnectedException {
		if (connection == null) {
			throw new NotConnectedException();
		}
	}

	@Override
	public void connect(String username, String password) throws CouldNotConnectException, ClassNotFoundException {
		try {
			if (connection == null || !connection.isValid(30)) {
				if (connection == null) {
					// Load the specified database driver
					Class.forName(driverName);
				} else {
					connection.close();
				}

				// Create new connection and get metadata
				connection = DriverManager.getConnection(databaseUrl, username, password);
			}
		} catch (SQLException e) {
			throw new CouldNotConnectException();
		}
	}

	@Override
	public List<JaratFejlec> search(String keyword) throws NotConnectedException {
		checkConnected();
		
		List<JaratFejlec> jaratok = new ArrayList<>();
		
		try (Statement statement = connection.createStatement()) 
		{
			// Eredmenytabla
			ResultSet resultSet;
			
			// Ha a kereso mezoben nincs ertek kilistazzuk az osszes megrendelest
			if(keyword.equals(""))
			{
				resultSet = statement.executeQuery("SELECT vonatszam, tipus, megjegyzes FROM jarat ");
				while (resultSet.next()) 
				{
					JaratFejlec jarat = new JaratFejlec();
					jarat.setVonatszam(resultSet.getInt("vonatszam"));
					jarat.setTipus(resultSet.getString("tipus"));
					jarat.setMegjegyzes(resultSet.getString("megjegyzes"));
					
					jaratok.add(jarat);
				}
			}
			else {
				// Egyebkent a kulcsszora tortenjen szures
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT vonatszam, tipus, megjegyzes FROM jarat WHERE vonatszam LIKE ? ESCAPE '@'");
				//"SELECT * FROM orders WHERE description LIKE ? ESCAPE '@'"
				// Beallitjuk a feltetelt				
				preparedStatement.setString(1, keyword);
				// Majd vegrehajtjuk a lekerdezest
				resultSet = preparedStatement.executeQuery();
				// Feltoltjuk az eredmenytabla elemeivel a listat amit visszaadunk
				while (resultSet.next()) 
				{
					JaratFejlec jarat = new JaratFejlec();
					jarat.setVonatszam(resultSet.getInt("vonatszam"));
					jarat.setTipus(resultSet.getString("tipus"));
					jarat.setMegjegyzes(resultSet.getString("megjegyzes"));
					
					jaratok.add(jarat);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return jaratok;
	}

	@Override
	public List<JaratVonatszam> getStatistics() throws NotConnectedException {
		checkConnected();
		return null;
	}

	@Override
	public boolean commit() throws NotConnectedException {
		checkConnected();
		try {
			connection.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		//return false;
	}

	@Override
	public boolean rollback() throws NotConnectedException {
		checkConnected();
		try {
			connection.rollback();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		//return false;
	}

	@Override
	public ActionResult insertOrUpdate(Jarat entity, Integer foreignKey)
			throws NotConnectedException, EntityNotFoundException {
		checkConnected();
		
		ActionResult actionResult = ActionResult.ErrorOccurred;
		
		try{
			// A lekerdezes "szovege"
			String query;
			
			// bekapcsoljuk az autocommit-ot
			//connection.setAutoCommit(true);
			
			// Megnezzuk letezik e az allomas, ha nem hibaval visszaterunk 
			PreparedStatement checkAllomas = connection.prepareStatement("SELECT id FROM allomas WHERE id LIKE ?");
			checkAllomas.setInt(1, foreignKey);
			ResultSet allomasResultSet = checkAllomas.executeQuery();
			if(!allomasResultSet.next()){
				actionResult = ActionResult.ErrorOccurred;
				return actionResult;
			}
			
			// majd megnezzuk, hogy a kapott orderId szerpel e az adatbazisban
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT vonatszam FROM jarat WHERE vonatszam LIKE ?");
			preparedStatement.setInt(1, entity.getVonatszam());
			
			ResultSet resultSet = preparedStatement.executeQuery();
			
			// Ha a resultSet ures, azt jelenti hogy nincs a megadott orderID-val megegyezo bejegyzes, 
			// tehat beszurast kell vegeznunk
			if(!resultSet.next()){
				// A lekerdezes maga
				
				query = "INSERT INTO jarat (vonatszam, tipus, nap, kezd, vege, megjegyzes) VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement insertPreparedStatement = connection.prepareStatement(query);
				// Feltoltjuk a lekerdezes parametereit
				insertPreparedStatement.setInt(1, entity.getVonatszam());
				insertPreparedStatement.setString(2, entity.getTipus());
				insertPreparedStatement.setString(3, entity.getNap());
				
				if(entity.getKezd() == null)
					insertPreparedStatement.setDate(4, null);
				else
					insertPreparedStatement.setDate(4, Date.valueOf(entity.getKezd()));
				if(entity.getVege() == null)
					insertPreparedStatement.setDate(5, null);
				else
					insertPreparedStatement.setDate(5, Date.valueOf(entity.getVege()));
				
				insertPreparedStatement.setString(6, entity.getMegjegyzes());
				
				insertPreparedStatement.executeUpdate();
				
				// Megnezzuk melyik a legnagyobb id a megall tablaban, majd ezt megnovelve felhasznaljuk az uj ertek beszurasakor
				Statement megallId = connection.createStatement();
				ResultSet megallIdResult = megallId.executeQuery("SELECT MAX(id) as value FROM megall");
				megallIdResult.next();
				int newMegallId = megallIdResult.getInt("value");
				newMegallId++;
				
				query = "INSERT INTO megall (id, vonatszam, allomas_id, erk, ind) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement megallPreparedStatement = connection.prepareStatement(query);
				megallPreparedStatement.setInt(1, newMegallId);
				megallPreparedStatement.setInt(2, entity.getVonatszam());
				megallPreparedStatement.setInt(3, foreignKey);
				megallPreparedStatement.setInt(4, 1015);
				megallPreparedStatement.setInt(5, 1030);
				
				megallPreparedStatement.executeUpdate();
		
				// insert visszateresi ertek
				actionResult = ActionResult.InsertOccurred;
				return actionResult;
			}
			
			// Ha az eredmenytabla nem ures, mindekeppen Update-et kell vegeznunk
			else {
				query = "UPDATE jarat SET tipus=?, nap=?, kezd=?, vege=?, megjegyzes=? WHERE vonatszam LIKE ?";
				PreparedStatement updatePreparedStatement = connection.prepareStatement(query);
				
				updatePreparedStatement.setString(1, entity.getTipus());
				updatePreparedStatement.setString(2, entity.getNap());
				// Mivel a datum megadasa nem kotelezo ellenorizzuk hogy null e
				if(entity.getKezd() == null)
					updatePreparedStatement.setDate(3, null);
				else
					updatePreparedStatement.setDate(3, Date.valueOf(entity.getKezd()));
				
				if(entity.getVege() == null)
					updatePreparedStatement.setDate(4, null);
				else
					updatePreparedStatement.setDate(4, Date.valueOf(entity.getVege()));
				
				updatePreparedStatement.setString(5, entity.getMegjegyzes());
				updatePreparedStatement.setInt(6, entity.getVonatszam());
				
				updatePreparedStatement.executeUpdate();
				
				// Beallitjuk a visszateresi erteket
				actionResult = ActionResult.UpdateOccurred;
				return actionResult;
			}
		}
		catch (SQLException e){
			e.printStackTrace();
			actionResult = ActionResult.ErrorOccurred;
			rollback();
			return actionResult;
		}
	}

	@Override
	public boolean setAutoCommit(boolean value) throws NotConnectedException {
		checkConnected();
		try {
			connection.setAutoCommit(value);
			if(connection.getAutoCommit() == value)
				return true;
			else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Person> sampleQuery() throws NotConnectedException {
		checkConnected();
		List<Person> result = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rset = stmt.executeQuery("SELECT nev, szemelyi_szam FROM OKTATAS.SZEMELYEK "
					+ "ORDER BY NEV "
					+ "OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY")){
				while (rset.next()) {
					Person p = new Person(rset.getString("nev"), rset.getString("szemelyi_szam"));
					result.add(p);
				}
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
