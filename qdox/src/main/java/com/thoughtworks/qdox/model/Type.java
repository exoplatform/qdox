package com.thoughtworks.qdox.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Type implements Serializable {

    public static final Type VOID = new Type("void");

    private String name;
    private JavaClassParent context;
    private String fullName;
    private int dimensions;
    private List<Type> actualArgumentTypes;

    public Type(String fullName, String name, int dimensions, JavaClassParent context) {
        this.fullName = fullName;
        this.name = name;
        this.dimensions = dimensions;
        this.context = context;
    }
    
    public Type(String fullName, int dimensions, JavaClassParent context) {
        this(fullName, (String) null, dimensions, context);
    }

    public Type(String fullName, int dimensions) {
        this(fullName, dimensions, null);
    }

    public Type(String fullName) {
        this(fullName, 0);
    }
    
	public static Type createUnresolved(String name, int dimensions, JavaClassParent context) {
        return new Type(null, name, dimensions, context);
    }
    
	public JavaClassParent getJavaClassParent() {
        return context;
    }

    /**
     * Returns the FQN of an Object or the handler of a Type
     * If the name of the can't be resolved based on the imports and the classes on the classpath the name will be returned
     * InnerClasses will use the $ sign
     * 
     * Some examples how names will be translated 
     * <pre>
     * Object > java.lang.Object
     * java.util.List > java.util.List
     * ?  > ?
     * T  > T
     * anypackage.Outer.Inner > anypackage.Outer$Inner
     * </pre>
     * 
     * @return
     */
    public String getFullyQualifiedName() {
        
        return isResolved() ? fullName : name;
    }

    /**
     * The FQN representation of an Object for code usage
     * This implementation ignores generics
     *
     * Some examples how Objects will be translated
     * <pre>
     * Object > java.lang.object
     * java.util.List<T> > java.util.List
     * ? > ?
     * T > T
     * anypackage.Outer.Inner > anypackage.Outer.Inner
     * </pre>
     * 
     * @return type representation for code usage
     */
    public String getValue() {
        String fqn = getFullyQualifiedName();
        return ( fqn == null ? "" : fqn.replaceAll( "\\$", "." ) );
    }
    
    /**
     * The FQN representation of an Object for code usage
     * This implementation ignores generics
     *
     * Some examples how Objects will be translated
     * <pre>
     * Object > java.lang.object
     * java.util.List<T> > java.util.List
     * ? > ?
     * T > T
     * anypackage.Outer.Inner > anypackage.Outer.Inner
     * </pre>

     * @since 1.8
     * @return generic type representation for code usage 
     */
    public String getGenericValue() {
    	StringBuffer result = new StringBuffer(getValue());
    	if(actualArgumentTypes != null && actualArgumentTypes.size() > 0) {
    		result.append("<");
    		for(Iterator<Type> iter = actualArgumentTypes.iterator();iter.hasNext();) {
    			result.append(iter.next().getGenericValue());
    			if(iter.hasNext()) {
    				result.append(",");
    			}
    		}
    		result.append(">");
    	}
    	for (int i = 0; i < dimensions; i++) result.append("[]");
        return result.toString();
    }
    
    protected String getGenericValue(List<TypeVariable> typeVariableList) {
    	StringBuffer result = new StringBuffer(getResolvedValue(typeVariableList));
    	if(actualArgumentTypes != null && actualArgumentTypes.size() > 0) {
    		for(int index = 0;index < actualArgumentTypes.size(); index++) {
    			result.append(actualArgumentTypes.get(index).getResolvedGenericValue(typeVariableList));   			
    			if(index + 1 != actualArgumentTypes.size()) {
    				result.append(",");
    			}
    		}
    	}
        return result.toString();
    }
    
    protected String getResolvedValue(List<TypeVariable> typeParameters) {
    	String result = getValue();
    	for(TypeVariable typeParameter : typeParameters) {
			if(typeParameter.getName().equals(getValue())) {
				result = typeParameter.getValue();
				break;
			}
		}
    	return result;
    }
    
    protected String getResolvedGenericValue(List<TypeVariable> typeParameters) {
    	String result = getGenericValue(typeParameters);
    	for(TypeVariable typeParameter : typeParameters) {
			if(typeParameter.getName().equals(getValue())) {
				result = typeParameter.getGenericValue();
				break;
			}
		}
    	return result;
    }

    /**
     * Checks if the FQN of this Type is resolved 
     * 
     * @return 
     */
    public boolean isResolved() {
        if (fullName == null && context != null) {
            fullName = context.resolveType(name);
        }
        return (fullName != null);
    }

    /**
     * Returns true if this Type is an array
     * 
     * @return
     */
    public boolean isArray() {
        return dimensions > 0;
    }

    /**
     * Returns the depth of this array, 0 if it's not an array
     * 
     * @return The depth of this array
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * 
     * @return the actualTypeArguments or null
     */
    public List<Type> getActualTypeArguments()
    {
        return actualArgumentTypes;
    }
    
    public void setActualArgumentTypes( List<Type> actualArgumentTypes )
    {
        this.actualArgumentTypes = actualArgumentTypes;
    }
    
    /**
     * Returns getValue() extended with the array information 
     * 
     * @return
     */
    public String toString() {
        if (dimensions == 0) return getValue();
        StringBuffer buff = new StringBuffer(getValue());
        for (int i = 0; i < dimensions; i++) buff.append("[]");
        String result = buff.toString();
        return result;
    }

    /**
     * Returns getGenericValue() extended with the array information
     * 
     * <pre>
     * Object > java.lang.Object
     * Object[] > java.lang.Object[]
     * List<Object> > java.lang.List<java.lang.Object>
     * Outer.Inner > Outer.Inner 
     * Outer.Inner<Object>[][] > Outer.Inner<java.lang.Object>[][] 
     * </pre>
     * @return 
     */
    public String toGenericString() {
        if (dimensions == 0) return getGenericValue();
        StringBuffer buff = new StringBuffer(getGenericValue());
        for (int i = 0; i < dimensions; i++) buff.append("[]");
        String result = buff.toString();
        return result;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        Type t = (Type) obj;
        return getValue().equals(t.getValue()) && t.getDimensions() == getDimensions();
    }

    public int hashCode() {
        return getValue().hashCode();
    }

    public JavaClass getJavaClass() {
    	JavaClass result = null;
    	
        JavaClassParent javaClassParent = getJavaClassParent();
        if (javaClassParent != null) {
        	result = javaClassParent.getNestedClassByName(getFullyQualifiedName());
	        if(result == null) {
	            if(javaClassParent.getJavaClassLibrary() != null) {
	                result = javaClassParent.getJavaClassLibrary().getJavaClass( getFullyQualifiedName(), true );
	            }
	        }
        }
        return result;
    }

    /**
     * @since 1.3
     */
    public boolean isA(Type type) {
        if (this.equals(type)) {
            return true;
        } else {
            JavaClass cls = getJavaClass();
            if (cls != null) {
                // ask our interfaces
                for (Type implementz : cls.getImplements()) {
                    if (implementz.isA(type)) {
                        return true;
                    }
                }

                // ask our superclass
                Type supertype = cls.getSuperClass();
                if (supertype != null) {
                    if (supertype.isA(type)) {
                        return true;
                    }
                }
            }
        }
        // We'we walked up the hierarchy and found nothing.
        return false;
    }

    /**
     * @since 1.6
     */
    public boolean isPrimitive() {
        String value = getValue();
        if (value == null || value.length() == 0 || value.indexOf('.') > -1) {
            return false;
        } else {
           return "void".equals(value)           
            || "boolean".equals(value)
            || "byte".equals(value)
            || "char".equals(value)
            || "short".equals(value)
            || "int".equals(value)
            || "long".equals(value)
            || "float".equals(value)
            || "double".equals(value);
        }
    }

    /**
     * @since 1.6
     */
    public boolean isVoid() {
        return "void".equals(getValue());
    }

    /**
     * 
     * @param parentClass
     * @param subclass
     * @return
     * @since 1.12
     */
    protected Type resolve( JavaClass parentClass, JavaClass subclass )
    {
        Type result = this;

        int typeIndex = -1;
        for(ListIterator<TypeVariable> iter = parentClass.getTypeParameters().listIterator();iter.hasNext();) {
            if(iter.next().getFullyQualifiedName().equals( getFullyQualifiedName())) {
                typeIndex = iter.previousIndex();
                break;
            }
        }

        if ( typeIndex >= 0 )
        {
            String fqn = parentClass.getFullyQualifiedName();
            if ( subclass.getSuperClass() != null && fqn.equals( subclass.getSuperClass().getFullyQualifiedName() ) ) {
                result = subclass.getSuperClass().getActualTypeArguments().get(typeIndex);    
            }
            else if ( subclass.getImplementedInterfaces() != null )
            {
                for ( int i = 0; i < subclass.getImplementedInterfaces().size(); i++ )
                {
                    if ( fqn.equals( subclass.getImplements().get(i).getFullyQualifiedName() ) ) 
                    {
                        JavaClass argument = subclass.getImplementedInterfaces().get(i);
                        result = subclass.getImplements().get(i).getActualTypeArguments().get(typeIndex).resolve(argument,argument);
                        break;
                    }
                }
                //no direct interface available, try indirect
            }
        }
        
        if ( this.actualArgumentTypes != null ) {
            result = new Type( this.fullName, this.name, this.dimensions, this.context );
            
            result.actualArgumentTypes = new LinkedList<Type>();
            for (Type actualArgType : getActualTypeArguments())
            {
                result.actualArgumentTypes.add(actualArgType.resolve( parentClass, subclass ));
            }
        }
        return result;
    }

}