package com.thoughtworks.qdox.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.thoughtworks.qdox.model.annotation.AnnotationFieldRef;
import com.thoughtworks.qdox.model.annotation.AnnotationVisitor;
import com.thoughtworks.qdox.model.annotation.RecursiveAnnotationVisitor;
import com.thoughtworks.qdox.parser.Builder;
import com.thoughtworks.qdox.parser.structs.ClassDef;
import com.thoughtworks.qdox.parser.structs.FieldDef;
import com.thoughtworks.qdox.parser.structs.MethodDef;
import com.thoughtworks.qdox.parser.structs.PackageDef;
import com.thoughtworks.qdox.parser.structs.TagDef;
import com.thoughtworks.qdox.parser.structs.TypeDef;
import com.thoughtworks.qdox.parser.structs.TypeVariableDef;

/**
 * @author <a href="mailto:joew@thoughtworks.com">Joe Walnes</a>
 * @author Robert Scholte
 */
public class ModelBuilder implements Builder {

    private final DefaultJavaSource source;
    private JavaClass currentClass;
    private JavaMethod currentMethod;
    private List<Annotation> currentAnnoDefs;
    private String lastComment;
    private List<TagDef> lastTagSet;
    private DocletTagFactory docletTagFactory;

    public ModelBuilder(com.thoughtworks.qdox.library.ClassLibrary classLibrary, DocletTagFactory docletTagFactory) {
        this.docletTagFactory = docletTagFactory;
        source = new DefaultJavaSource(classLibrary);
        currentAnnoDefs = new ArrayList<Annotation>();
    }
    
    public void setModelWriterFactory( ModelWriterFactory modelWriterFactory )
    {
        this.source.setModelWriterFactory( modelWriterFactory );
    }

    public void addPackage(PackageDef packageDef) {
        DefaultJavaPackage jPackage = new DefaultJavaPackage(packageDef.name);
        jPackage.setLineNumber(packageDef.lineNumber);
    	setAnnotations(jPackage);
        source.setPackage(jPackage);
    }

    public void addImport(String importName) {
        source.addImport(importName);
    }

    public void addJavaDoc(String text) {
        lastComment = text;
        lastTagSet = new LinkedList<TagDef>();
    }

    public void addJavaDocTag(TagDef tagDef) {
        lastTagSet.add(tagDef);
    }

    public void beginClass(ClassDef def) {
        JavaClass newClass = new JavaClass(source);
        newClass.setLineNumber(def.lineNumber);

        // basic details
        newClass.setName(def.name);
        newClass.setInterface(ClassDef.INTERFACE.equals(def.type));
        newClass.setEnum(ClassDef.ENUM.equals(def.type));
        newClass.setAnnotation(ClassDef.ANNOTATION_TYPE.equals(def.type));

        // superclass
        if (newClass.isInterface()) {
            newClass.setSuperClass(null);
        } else if (!newClass.isEnum()) {
            newClass.setSuperClass(def.extendz.size() > 0 ? createType((TypeDef) def.extendz.toArray()[0], 0) : null);
        }

        // implements
        {
            Set<TypeDef> implementSet = newClass.isInterface() ? def.extendz : def.implementz;
            Iterator<TypeDef> implementIt = implementSet.iterator();
            List<Type> implementz = new LinkedList<Type>();
            while (implementIt.hasNext()) {
                implementz.add(createType(implementIt.next(), 0));
            }
            newClass.setImplementz(implementz);
        }

        // modifiers
        {
            String[] modifiers = new String[def.modifiers.size()];
            def.modifiers.toArray(modifiers);
            newClass.setModifiers(modifiers);
        }
        
        // typeParameters
        if (def.typeParams != null) {
            List<TypeVariable> typeParams = new LinkedList<TypeVariable>();
            for(Iterator<TypeVariableDef> iterator = def.typeParams.iterator(); iterator.hasNext();) {
                TypeVariableDef typeVariableDef = (TypeVariableDef) iterator.next();
                typeParams.add(createTypeVariable(typeVariableDef));
            }
            newClass.setTypeParameters(typeParams);
        }

        // javadoc
        addJavaDoc(newClass);

//        // ignore annotation types (for now)
//        if (ClassDef.ANNOTATION_TYPE.equals(def.type)) {
//        	System.out.println( currentClass.getFullyQualifiedName() );
//            return;
//        }

        // annotations
        setAnnotations( newClass );
        
        if(currentClass != null) {
            currentClass.addClass( newClass );
            newClass.setParentClass( currentClass );
        }
        else {
            source.addClass( newClass );
        }
        currentClass = newClass;
    }

    public void endClass() {
        currentClass = currentClass.getParentClass();
    }

    public Type createType( String typeName, int dimensions ) {
        if( typeName == null || typeName.equals( "" ) )
            return null;
        return createType(new TypeDef(typeName), dimensions);
    }
    
    public Type createType(TypeDef typeDef) {
    	return createType(typeDef, 0);
    }
    
    
    /**
     * this one is specific for those cases where dimensions can be part of both the type and identifier
     * i.e. private String[] matrix[]; //field
     * 		public abstract String[] getMatrix[](); //method  
     *      
     * @param typeDef
     * @param dimensions
     * @return the Type
     */
    public Type createType(TypeDef typeDef, int dimensions) {
    	if(typeDef == null) {
    		return null;
    	}
    	return Type.createUnresolved(typeDef, dimensions, currentClass == null ? source : currentClass);
    }

    private void addJavaDoc(AbstractJavaEntity entity) {
        if (lastComment == null) return;

        entity.setComment(lastComment);
        
        Iterator<TagDef> tagDefIterator = lastTagSet.iterator();
        List<DocletTag> tagList = new ArrayList<DocletTag>();
        while (tagDefIterator.hasNext()) {
            TagDef tagDef = tagDefIterator.next();
            tagList.add( 
                docletTagFactory.createDocletTag(
                    tagDef.name, tagDef.text, 
                    entity, tagDef.lineNumber
                )
            );
        }
        entity.setTags(tagList);
        
        lastComment = null;
    }

    public void addMethod(MethodDef def) {
    	beginMethod();
    	endMethod(def);
    }
    
    public void beginMethod() {
    	currentMethod = new JavaMethod();
    	setAnnotations(currentMethod);
    }
    
    public void endMethod(MethodDef def) {
        currentMethod.setParentClass(currentClass);
        currentMethod.setLineNumber(def.lineNumber);

        // basic details
        currentMethod.setName(def.name);
        currentMethod.setReturns(createType(def.returnType, def.dimensions));
        currentMethod.setConstructor(def.constructor);

        // typeParameters
        if (def.typeParams != null) {
        	List<TypeVariable> typeParams = new LinkedList<TypeVariable>();
        	for(TypeVariableDef typeVariableDef : def.typeParams) {
        		typeParams.add(createTypeVariable(typeVariableDef));
        	}
            currentMethod.setTypeParameters(typeParams);
        }
        
        // exceptions
        {
            List<Type> exceptions = new LinkedList<Type>();
            for (String type : def.exceptions) {
                exceptions.add(createType(type, 0));
            }
            currentMethod.setExceptions(exceptions);
        }

        // modifiers
        {
            String[] modifiers = new String[def.modifiers.size()];
            def.modifiers.toArray(modifiers);
            currentMethod.setModifiers(modifiers);
        }
        
        currentMethod.setSourceCode(def.body);

        // javadoc
        addJavaDoc(currentMethod);

        currentClass.addMethod(currentMethod);
        currentMethod.setParentClass(currentClass);
    }

    public TypeVariable createTypeVariable(TypeVariableDef typeVariableDef) {
    	if(typeVariableDef == null) {
    		return null;
    	}
    	return TypeVariable.createUnresolved(typeVariableDef, currentClass == null ? source : currentClass);

	}

	public TypeVariable createTypeVariable(String name, List<TypeDef> typeParams) {
    	if( name == null || name.equals( "" ) )
            return null;
    	
        return createTypeVariable(new TypeVariableDef(name, typeParams));
	}

	public void addField(FieldDef def) {
        DefaultJavaField currentField = new DefaultJavaField();
        currentField.setParentClass(currentClass);
        currentField.setLineNumber(def.lineNumber);

        currentField.setName(def.name);
        currentField.setType(createType(def.type, def.dimensions));

        // modifiers
        {
            String[] modifiers = new String[def.modifiers.size()];
            def.modifiers.toArray(modifiers);
            currentField.setModifiers(modifiers);
        }
	
        // code body
        currentField.setInitializationExpression(def.body);
	
        // javadoc
        addJavaDoc(currentField);

        // annotations
        setAnnotations( currentField );

        currentClass.addField(currentField);
    }
	
	public void addParameter(FieldDef fieldDef) {
	    DefaultJavaParameter jParam = new DefaultJavaParameter(createType(fieldDef.type, fieldDef.dimensions), fieldDef.name, fieldDef.isVarArgs);
        jParam.setParentMethod( currentMethod );
        setAnnotations( jParam );
        currentMethod.addParameter( jParam );
	}

    private void setAnnotations( final AbstractBaseJavaEntity entity ) {
        if( !currentAnnoDefs.isEmpty() ) {
            AnnotationVisitor visitor = new RecursiveAnnotationVisitor() {
                public Object visitAnnotation( Annotation annotation ) {
                    annotation.setContext( entity );
                    return super.visitAnnotation( annotation );
                }
                
                public Object visitAnnotationFieldRef( AnnotationFieldRef fieldRef ) {
                    fieldRef.setContext( entity );
                    return super.visitAnnotationFieldRef( fieldRef );
                }
            };

            Annotation[] annotations = new Annotation[currentAnnoDefs.size()];
            for( ListIterator<Annotation> iter = currentAnnoDefs.listIterator(); iter.hasNext(); ) {
                Annotation annotation = iter.next();
                annotation.accept(visitor);
                annotations[iter.previousIndex()] = annotation;
            }

            entity.setAnnotations( Arrays.asList( annotations ) );
            currentAnnoDefs.clear();
        }
    }

    // Don't resolve until we need it... class hasn't been defined yet.
    public void addAnnotation( Annotation annotation ) {
        currentAnnoDefs.add( annotation );
    }

    public JavaSource getSource() {
        return source;
    }

}

