package dal.exceptions;

/**
 * Exception for signaling that the validation of a field is failed.
 *
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 378883886879907289L;
	private String fieldName;

	public ValidationException(String fieldName) {
		super("syntax error: " + fieldName);
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
