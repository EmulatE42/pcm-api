package gov.samhsa.c2s.vss.service;

public class CodeSystemVersionNotFoundException extends Exception {

	private static final long serialVersionUID = 8942118794723776513L;

	public CodeSystemVersionNotFoundException() {
		super();
	}

	public CodeSystemVersionNotFoundException(String message) {
		super(message);
	}
}
