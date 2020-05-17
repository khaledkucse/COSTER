package org.usask.srlab.coster.dictionary;

public abstract class APIElement {
	
	private int nameId;
	
	public APIElement(int id) {
		this.nameId = id;
	}
	
	int getNameId() {
		return nameId;
	}

	String getName() {
		return APIDictionary.getName(nameId);
	}
	
	abstract public String getFQN();
	
}
