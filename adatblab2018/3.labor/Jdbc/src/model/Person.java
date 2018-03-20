package model;

/**
 * Sample class to demonstrate queries.
 *
 */
public class Person {
	private String name;
	private String identityNumber;

	public Person(String name, String identityNumber) {
		super();
		this.name = name;
		this.identityNumber = identityNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentityNumber() {
		return identityNumber;
	}

	public void setIdentityNumber(String identityNumber) {
		this.identityNumber = identityNumber;
	}

}
