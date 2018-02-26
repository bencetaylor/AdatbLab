package application;

/**
 * Class for displaying on item in a combobox. 
 * @param <T> The type of the value of the item.
 */
public class ComboBoxItem<T> {
	private String label;
	private T value;
	
	public ComboBoxItem(String label, T value) {
		super();
		this.label = label;
		this.value = value;
	}

	/**
	 * 
	 * @return The label to display on the UI.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Updates the label.
	 * @param label The new label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * The value stored in the ComboBoxItem.
	 * @return The generic value.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Updates the stored value for this item.
	 * @param value The new value.
	 */
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return label;
	}
}
