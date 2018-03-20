package dal.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dal.ActionResult;
import dal.DataAccessLayer;
import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import model.Order1;
import model.Person;
import model.Order234;
import model.OrderReport;

/**
 * Initial implementation for the DataAccessLayer for the 26-SZALL exercise.
 */
public class SzallDal implements DataAccessLayer<Order1, Order234, OrderReport> {
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
	
	/**
	 * 1. feladat megvalositasa
	 * 
	 */
	@Override
	public List<Order1> search(String keyword) throws NotConnectedException
	{
		checkConnected();
		
		// visszateresi lista
		List<Order1> result = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) 
		{
			// Eredmenytabla
			ResultSet rset;
			// Ha a kereso mezoben nincs ertek kilistazzuk az osszes megrendelest
			if(keyword.equals(""))
			{
				rset = stmt.executeQuery("SELECT * FROM orders "
						+ "ORDER BY DEADLINE_DATE " );
				while (rset.next()) 
				{
					// Feltoltjuk az eredmenytabla elemeivel a listat amit visszaadunk
					Order1 ord = new Order1();
					ord.setOrderId(rset.getString("Order_ID"));
					ord.setDescription(rset.getString("Description"));
					ord.setVehicleType(rset.getString("Vehicle_Type"));
					ord.setQuantity(rset.getInt("Quantity"));
					ord.setOrigin(rset.getString("Origin"));
					ord.setDestination(rset.getString("Destination"));
					ord.setDeadline(rset.getDate("Deadline_date").toLocalDate());
					result.add(ord);
				}
			}
			else {
				// Egyebkent a kulcsszora tortenjen szures
				PreparedStatement ps = connection.prepareStatement(
						"SELECT * FROM orders WHERE description LIKE ? ESCAPE '@'");
				// Beallitjuk a feltetelt				
				ps.setString(1, keyword);
				// Majd vegrehajtjuk a lekerdezest
				rset = ps.executeQuery();
				// Feltoltjuk az eredmenytabla elemeivel a listat amit visszaadunk
				while (rset.next()) 
				{
					Order1 ord = new Order1();
					ord.setOrderId(rset.getString("Order_ID"));
					ord.setDescription(rset.getString("Description"));
					ord.setVehicleType(rset.getString("Vehicle_Type"));
					ord.setQuantity(rset.getInt("Quantity"));
					ord.setOrigin(rset.getString("Origin"));
					ord.setDestination(rset.getString("Destination"));
					ord.setDeadline(rset.getDate("Deadline_date").toLocalDate());
					result.add(ord);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	@Override
	public List<OrderReport> getStatistics() throws NotConnectedException
	{
		checkConnected();
		
		return null;
	}

	@Override
	public boolean commit() throws NotConnectedException {
		checkConnected();
		try {
			connection.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean rollback() throws NotConnectedException {
		checkConnected();
		try {
			connection.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 2.Feladat megvalositasa
	 */
	@Override
	public ActionResult insertOrUpdate(Order234 entity, Integer foreignKey)
			throws NotConnectedException, EntityNotFoundException {
		checkConnected();
		
		ActionResult result = ActionResult.ErrorOccurred;
		// A lekerdezes "szovege"
		String sql;
		
		try{
			// bekapcsoljuk az autocommit-ot
			connection.setAutoCommit(true);
			// majd megnezzuk, hogy a kapott orderId szerpel e az adatbazisban
			PreparedStatement ps = connection.prepareStatement("SELECT order_id FROM orders WHERE order_id LIKE ?");
			ps.setString(1, entity.getOrderId());
			ResultSet rs = ps.executeQuery();
			
			// Ha a resultSet ures, azt jelenti hogy nincs a megadott orderID-val megegyezo bejegyzes, 
			// tehat beszurast kell vegeznunk
			if(!(rs.next())){
				// A lekerdezes maga
				sql = "INSERT INTO orders (ORDER_ID, DESCRIPTION, VEHICLE_TYPE, QUANTITY, ORIGIN, DESTINATION, DEADLINE_DATE) VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement insert = connection.prepareStatement(sql);
				// Feltoltjuk a lekerdezes parametereit
				insert.setString(1, entity.getOrderId());
				insert.setString(2, entity.getDescription());
				insert.setString(3, entity.getVehicleType());
				insert.setInt(4, entity.getQuantity());
				insert.setString(5, entity.getOrigin());
				insert.setString(6, entity.getDestination());
				insert.setDate(7, Date.valueOf(entity.getDeadline()));
				
				insert.executeUpdate();
				// insert visszateresi ertek
				result = ActionResult.InsertOccurred;
				return result;
			}
			// Ha az eredmenytabla nem ures, mindekeppen Update-et kell vegeznunk
			else {
				sql = "UPDATE orders SET DESCRIPTION = ?, VEHICLE_TYPE = ?, QUANTITY = ?, ORIGIN = ?, DESTINATION = ?, DEADLINE_DATE = ? WHERE Order_ID LIKE ?";
				PreparedStatement update = connection.prepareStatement(sql);
				
				update.setString(1, entity.getDescription());
				update.setString(2, entity.getVehicleType());
				update.setInt(3, entity.getQuantity());
				update.setString(4, entity.getOrigin());
				update.setString(5, entity.getDestination());
				update.setDate(6, Date.valueOf(entity.getDeadline()));
				update.setString(7, entity.getOrderId());
				
				update.executeUpdate();
				
				// Beallitjuk a visszateresi erteket
				result = ActionResult.UpdateOccurred;
				return result;
			}
		}
		catch (SQLException e){
			e.printStackTrace();
			result = ActionResult.ErrorOccurred;
			return result;
		}
	}

	@Override
	public boolean setAutoCommit(boolean value) throws NotConnectedException {
		checkConnected();
		try {
			connection.setAutoCommit(value);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void disconnect() {
		
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
