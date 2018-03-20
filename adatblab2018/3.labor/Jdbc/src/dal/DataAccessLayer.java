package dal;

import java.util.List;

import dal.exceptions.CouldNotConnectException;
import dal.exceptions.EntityNotFoundException;
import dal.exceptions.NotConnectedException;
import model.Person;

/**
 * Data access layer for JDBC laboratory.
 *
 * @param <T1> The model class for storing the data for first exercise.
 * @param <T2> The model class for storing the data for the 2-3-4 exercises.
 * @param <T3> The model class for storing the data for the fifth exercise.
 */
public interface DataAccessLayer<T1, T2, T3> {
	/**
	 * Connect to the database.
	 * @param username The Oracle username
	 * @param password The Oracle password
	 * @throws CouldNotConnectException Connection failed.
	 * @throws ClassNotFoundException Oracle driver not found.
	 */
	void connect(String username, String password) throws CouldNotConnectException, ClassNotFoundException;
	
	/**
	 * Sample query.
	 */
	List<Person> sampleQuery() throws NotConnectedException;
	
	/**
	 * Method for solving the first exercise.
	 * @param keyword The search keyword
	 * @return List of the entites.
	 * @throws NotConnectedException Thrown when the user is not connected to the database.
	 */
	List<T1> search(String keyword) throws NotConnectedException;
	
	/**
	 * Method for solving the fifth exercise.
	 * @return List of the entities.
	 * @throws NotConnectedException Thrown when the user is not connected to the database.
	 */
	List<T3> getStatistics() throws NotConnectedException;
	
	/**
	 * Method for solving the second exercise.
	 * @param entity The entity to insert or update.
	 * @param foreignKey The foreign key value (can be null if not solved).
	 * @return The result (update, insert or error occurred).
	 * @throws NotConnectedException Thrown when the user is not connected to the database. 
	 * @throws EntityNotFoundException Thrown when the foreign key is invalid.
	 */
	ActionResult insertOrUpdate(T2 entity, Integer foreignKey) throws NotConnectedException, EntityNotFoundException;
	
	/**
	 * Method for solving the fourth exercise.
	 * @return Was the commit successful.
	 * @throws NotConnectedException Thrown when the user is not connected to the database.
	 */
	boolean commit() throws NotConnectedException;
	
	/**
	 * Method for rollback.
	 * @return Was the rollback successful
	 * @throws NotConnectedException Thrown when the user is not connected to the database.
	 */
	boolean rollback() throws NotConnectedException;
	
	/**
	 * Method for changing the auto commit (4th exercise).
	 * @param value Is auto commit enabled.
	 * @return Was it successful
	 * @throws NotConnectedException Thrown when the user is not connected to the database.
	 */
	boolean setAutoCommit(boolean value) throws NotConnectedException;
	
	/**
	 * Disconnect the database.
	 */
	void disconnect();
}
