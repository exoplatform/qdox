package com.thoughtworks.qdox.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Type implements Comparable, Serializable {

    private List imports;
    private String name;
	private JavaSource parentSource;
    private ClassLibrary classLibrary;
    private String packge;
    private String fullName = null;
    private int dimensions;

    public Type(String name, int dimensions, JavaSource parentSource) {
        this.name = name;
        this.dimensions = dimensions;
		this.parentSource = parentSource;
    }
	
    public Type(String fullName, int dimensions) {
        this.fullName = fullName;
        this.dimensions = dimensions;
    }

    public Type(String fullName) {
        this(fullName, 0);
    }

    public ClassLibrary getClassLibrary() {
        return classLibrary;
    }

	public JavaSource getParentSource() {
		return parentSource;
	}

	public void setParentSource(JavaSource javaSource) {
		parentSource = javaSource;
	}

    public String getValue() {
        return isResolved() ? fullName : name;
    }

    public boolean isResolved() {
        if (fullName == null && parentSource != null) {
            fullName = parentSource.resolveType(name);
		}
        return (fullName != null);
    }

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		if (!(o instanceof Type))
			return 0;

		return getValue().compareTo(((Type)o).getValue());
	}

	public boolean isArray() {
		return dimensions > 0;
	}

	public int getDimensions() {
		return dimensions;
	}

	public boolean equals(Object obj) {
		Type t = (Type)obj;
		return t.getValue().equals(getValue()) && t.getDimensions() == getDimensions();
	}

	public int hashCode() {
		return getValue().hashCode();
	}

}
