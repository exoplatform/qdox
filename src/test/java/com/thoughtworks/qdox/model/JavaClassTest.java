package com.thoughtworks.qdox.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public abstract class JavaClassTest<C extends JavaClass> extends TestCase {

    private C cls;
    private JavaSource src;

    public JavaClassTest(String s) {
        super(s);
    }
    
    //contructors
    public abstract C newJavaClass();
    public abstract C newJavaClass(String name);
    
    //setters
    public abstract void setComment(C clazz, String comment);
    public abstract void setEnum(C clazz, boolean isEnum);
    public abstract void setFields(C clazz, List<JavaField> fields);
    public abstract void setImplementz(C clazz, List<Type> implementz);
    public abstract void setInterface(C clazz, boolean isInterface);
    public abstract void setMethods(C clazz, List<JavaMethod> method);
    public abstract void setModifiers(C clazz, List<String> modifiers);
    public abstract void setName(C clazz, String name);
    public abstract void setSuperClass(C clazz, Type type);
    public abstract void setSource( C clazz, JavaSource source );
    
    public abstract JavaPackage newJavaPackage(String name);
    public abstract JavaParameter newJavaParameter(Type type, String name);
    public abstract JavaParameter newJavaParameter(Type type, String name, boolean varArgs);
    public abstract JavaSource newJavaSource();
    public abstract Type newType(String fullname);
    
    public abstract void setPackage(JavaSource source, JavaPackage pckg);
    
    public abstract void addClass(JavaClass clazz, JavaClass innerClazz);
    public abstract void addClass(JavaPackage pckg, JavaClass clazz);
    public abstract void addClass(JavaSource source, JavaClass clazz);

    protected void setUp() throws Exception {
        super.setUp();
        src = newJavaSource();
        cls = newJavaClass();
        addClass(src, cls);
    }

    public void testGetCodeBlockSimpleClass() throws Exception {
        setName(cls, "MyClass");
        String expected = ""
                + "class MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockSimpleInterface() throws Exception {
        setName(cls, "MyClass");
        setInterface(cls, true);
        String expected = ""
                + "interface MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockSimpleEnum() throws Exception {
        setName(cls, "MyEnum");
        setEnum(cls, true);
        String expected = ""
                + "enum MyEnum {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassExtends() throws Exception {
        setName(cls, "MyClass");
        setSuperClass(cls, newType("SuperClass"));
        String expected = ""
                + "class MyClass extends SuperClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockInterfaceExtends() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface"}));
        setInterface(cls, true);
        String expected = ""
                + "interface MyClass extends SomeInterface {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockInterfaceExtendsTwo() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface", "AnotherInterface"}));
        setInterface(cls, true);
        String expected = ""
                + "interface MyClass extends SomeInterface, AnotherInterface {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockInterfaceExtendsThree() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface", "AnotherInterface", "Thingy"}));
        setInterface(cls, true);
        String expected = ""
                + "interface MyClass extends SomeInterface, AnotherInterface, Thingy {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassImplements() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface"}));
        String expected = ""
                + "class MyClass implements SomeInterface {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassImplementsTwo() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface", "AnotherInterface", "Xx"}));
        String expected = ""
                + "class MyClass implements SomeInterface, AnotherInterface, Xx {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassImplementsAndExtends() throws Exception {
        setName(cls, "MyClass");
        setImplementz(cls, type(new String[]{"SomeInterface", "AnotherInterface", "Xx"}));
        setSuperClass(cls, newType("SubMarine"));
        String expected = ""
                + "class MyClass extends SubMarine implements SomeInterface, AnotherInterface, Xx {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockModifers() throws Exception {
        setName(cls, "MyClass");
        setModifiers(cls, Arrays.asList(new String[]{"public", "final"}));
        String expected = ""
                + "public final class MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockModifersProtectionAlwaysFirst() throws Exception {
        setName(cls, "MyClass");
        setModifiers(cls, Arrays.asList(new String[]{"final", "public"}));
        String expected = ""
                + "public final class MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());

        setModifiers(cls, Arrays.asList(new String[]{"abstract", "protected"}));
        expected = ""
                + "protected abstract class MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithOneMethod() throws Exception {
        setName(cls, "MyClass");
        JavaMethod mth = mock(JavaMethod.class);
        when(mth.getName()).thenReturn( "doStuff" );
        when(mth.getReturns()).thenReturn( newType("void") );
        
        setMethods(cls, Collections.singletonList( mth ));
        String expected = ""
                + "class MyClass {\n"
                + "\n"
                + "\tvoid doStuff();\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithThreeMethods() throws Exception {
        setName(cls, "MyClass");
        List<JavaMethod> methods = new ArrayList<JavaMethod>();
        {
            JavaMethod mth = mock(JavaMethod.class);
            when(mth.getName()).thenReturn( "doStuff" );
            when(mth.getReturns()).thenReturn( newType("void") );
            methods.add(mth);
        }

        {
            JavaMethod mth = mock(JavaMethod.class);
            when(mth.getName()).thenReturn( "somethingElse" );
            when(mth.getReturns()).thenReturn( newType("Goose") );
            methods.add(mth);
        }

        {
            JavaMethod mth = mock(JavaMethod.class);
            when(mth.getName()).thenReturn( "eat" );
            when(mth.getReturns()).thenReturn( newType("void") );
            methods.add(mth);
        }
        setMethods( cls, methods );

        String expected = ""
                + "class MyClass {\n"
                + "\n"
                + "\tvoid doStuff();\n"
                + "\n"
                + "\tGoose somethingElse();\n"
                + "\n"
                + "\tvoid eat();\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithTwoFields() throws Exception {
        setName(cls, "MyClass");
        List<JavaField> fields = new ArrayList<JavaField>();
        {
            JavaField fld = mock( JavaField.class );
            when(fld.getName()).thenReturn( "count" );
            when(fld.getType()).thenReturn( newType("int") );
            when(fld.getDeclaringClass()).thenReturn( cls );
            fields.add( fld );
        }

        {
            JavaField fld = mock( JavaField.class );
            when(fld.getName()).thenReturn( "thing" );
            when(fld.getType()).thenReturn( newType("String") );
            when(fld.getModifiers()).thenReturn( Collections.singletonList( "public" ) );
            when(fld.getDeclaringClass()).thenReturn( cls );
            fields.add( fld );
        }
        setFields( cls, fields );

        String expected = ""
                + "class MyClass {\n"
                + "\n"
                + "\tint count;\n"
                + "\n"
                + "\tpublic String thing;\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithInnerClass() throws Exception {
        setName(cls, "Outer");
        C innerClass = newJavaClass();
        setName(innerClass, "Inner");
        addClass(cls, innerClass);

        String expected = ""
                + "class Outer {\n"
                + "\n"
                + "\tclass Inner {\n"
                + "\n"
                + "\t}\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithInnerEnum() throws Exception {
        setName(cls, "Outer");
        C innerEnum = newJavaClass();
        setEnum(innerEnum, true);
        setName(innerEnum, "Inner");
        addClass(cls, innerEnum);

        String expected = ""
                + "class Outer {\n"
                + "\n"
                + "\tenum Inner {\n"
                + "\n"
                + "\t}\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockEnumWithInnerClass() throws Exception {
        setName(cls, "Outer");
        setEnum(cls, true);
        C innerClass = newJavaClass();
        setName(innerClass, "Inner");
        addClass(cls, innerClass);

        String expected = ""
                + "enum Outer {\n"
                + "\n"
                + "\tclass Inner {\n"
                + "\n"
                + "\t}\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }


    public void testGetCodeBlockClassWithComment() throws Exception {
        setName(cls, "MyClass");
        setComment(cls, "Hello World");

        String expected = ""
                + "/**\n"
                + " * Hello World\n"
                + " */\n"
                + "class MyClass {\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testGetCodeBlockClassWithIndentedCommentsForFieldAndMethod() throws Exception {
        setName(cls, "MyClass");
        setComment(cls, "Hello World");

        JavaMethod mth = mock(JavaMethod.class);
        when(mth.getName()).thenReturn( "thingy" );
        when(mth.getReturns()).thenReturn( newType("String") );
        when(mth.getComment()).thenReturn( "Hello Method" );
        setMethods( cls, Collections.singletonList( mth ) );
        
        JavaField fld = mock(JavaField.class);
        when(fld.getType()).thenReturn( newType("String") );
        when(fld.getName()).thenReturn( "thing" );
        when(fld.getComment()).thenReturn( "Hello Field" );
        when(fld.getDeclaringClass()).thenReturn( cls );
        setFields( cls, Collections.singletonList( fld ) );

        String expected = ""
                + "/**\n"
                + " * Hello World\n"
                + " */\n"
                + "class MyClass {\n"
                + "\n"
                + "\t/**\n"
                + "\t * Hello Field\n"
                + "\t */\n"
                + "\tString thing;\n"
                + "\n"
                + "\t/**\n"
                + "\t * Hello Method\n"
                + "\t */\n"
                + "\tString thingy();\n"
                + "\n"
                + "}\n";
        assertEquals(expected, cls.getCodeBlock());
    }

    public void testIsPublic() {
        setName(cls, "MyClass");
        assertTrue(!cls.isPublic());

        setModifiers(cls, Arrays.asList(new String[]{"public"}));
        assertTrue(cls.isPublic());
    }

    public void testQualifiedType() throws Exception {
        setPackage(src, newJavaPackage("com.thoughtworks.qdox"));

        setName(cls, "MyClass");

        assertEquals("MyClass", cls.getName());
        assertEquals("com.thoughtworks.qdox", cls.getPackage().getName());
        assertEquals("com.thoughtworks.qdox", cls.getPackageName());
        assertEquals("com.thoughtworks.qdox.MyClass",
                cls.getFullyQualifiedName());
        assertTrue(cls.asType().isResolved());
        assertEquals("com.thoughtworks.qdox.MyClass", cls.asType().getValue());
    }

    public void testGetClassNamePrefix() {
        setPackage(src, newJavaPackage("foo.bar"));
        setName(cls, "Stanley");
        assertEquals("foo.bar.Stanley$", cls.getClassNamePrefix());
    }
    
    public void testInnerClass() throws Exception {
        setPackage(src, newJavaPackage("foo.bar"));

        C outer = newJavaClass();
        setName(outer, "Outer");
        addClass(src, outer);

        C inner = newJavaClass();
        setName(inner, "Inner");
        addClass(outer, inner);

        assertEquals("Inner", inner.getName());
        assertEquals("foo.bar", inner.getPackage().getName());
        assertEquals("foo.bar", inner.getPackageName());
        assertEquals("foo.bar.Outer$Inner",
                inner.getFullyQualifiedName());
    }
    
    public void testDefaultPackageClass() {
    	setPackage(src, null);
    	setName(cls, "DefaultPackageClass");
    	
    	assertEquals("", src.getClasses().get(0).getPackageName());
    	assertEquals("DefaultPackageClass", src.getClasses().get(0).getFullyQualifiedName());
    }

    public void testDefaultClassSuperclass() throws Exception {
        setName(cls, "MyClass");
        assertEquals("java.lang.Object", cls.getSuperClass().getValue());
        setSuperClass(cls, newType("x.X"));
        assertEquals("x.X", cls.getSuperClass().getValue());
    }

    public void testDefaultInterfaceSuperclass() throws Exception {
        setName(cls, "MyInterface");
        setInterface(cls, true);
        assertNull(cls.getSuperClass());
        setSuperClass(cls, newType("x.X"));
        assertEquals("x.X", cls.getSuperClass().getValue());
    }

    public void testEnumSuperclass() throws Exception {
        setName(cls, "MyEnum");
        setEnum(cls, true);
        assertEquals("java.lang.Enum", cls.getSuperClass().getValue());
    }

    public void testEnumCannotExtendAnythingElse() throws Exception {
        setName(cls, "MyEnum");
        setEnum(cls, true);
        try {
            setSuperClass(cls, newType("x.X"));
            fail("expected an exception");
        } catch (IllegalArgumentException e) {
            assertEquals("enums cannot extend other classes", e.getMessage());
        }
    }

    public void testCanGetFieldByName() throws Exception {
        JavaField fredField = mock(JavaField.class);
        when(fredField.getName()).thenReturn( "fred" );
        when(fredField.getType()).thenReturn( newType("int") );
        when(fredField.getDeclaringClass()).thenReturn( cls );
        setFields( cls, Collections.singletonList( fredField ) );

        assertEquals(fredField, cls.getFieldByName("fred"));
        assertEquals(null, cls.getFieldByName("barney"));
    }

    public void testCanGetMethodBySignature() {
        final String methodName = "doStuff";
        final List<Type> parameterTypes = type(new String[]{"int", "double"});
        JavaMethod method = mock(JavaMethod.class);
        when(method.getName()).thenReturn(methodName);
        //both signatureMatches-methods are allowed
        when(method.signatureMatches( "doStuff", parameterTypes )).thenReturn( true );
        when(method.signatureMatches( "doStuff", parameterTypes, false )).thenReturn( true );
        setMethods(cls, Collections.singletonList( method ));

        assertSame(
                method,
                cls.getMethodBySignature("doStuff", parameterTypes)
        );
        assertEquals(
                null,
                cls.getMethodBySignature("doStuff", new ArrayList<Type>())
        );
        assertEquals(
                null,
                cls.getMethodBySignature("sitIdlyBy", parameterTypes)
        );
    }

    public void testCanGetInnerClassByName() throws Exception {
        C innerClass = newJavaClass();
        setName(innerClass, "Inner");
        addClass(cls, innerClass);

        assertEquals(innerClass, cls.getNestedClassByName("Inner"));
        assertEquals(null, cls.getNestedClassByName("Bogus"));
    }

    public void testResolveTypeDefaultsToParentScope() throws Exception {
        setName(cls, "X");
        assertEquals("int", cls.resolveType("int"));
    }
    
    public void testResolveTypeInnerClass() throws Exception {
        setPackage(src, newJavaPackage("p"));
        setName(cls, "X");
        C innerClass = newJavaClass();
        setName(innerClass, "DogFood");
        addClass(cls, innerClass);
        assertEquals("p.X$DogFood", cls.resolveType("DogFood"));
        assertEquals(null, cls.resolveType("Food"));
    }

    public void testGetBeanPropertiesReturnsEmptyForEmptyClass() throws Exception {
        assertEquals(0, cls.getBeanProperties().size());
    }

    public void testGetBeanPropertiesFindsSimpleProperties() throws Exception {
        List<JavaMethod> methods = new ArrayList<JavaMethod>();
        JavaMethod setFooMethod = mock(JavaMethod.class);
        when(setFooMethod.getName()).thenReturn( "setFoo" );
        when(setFooMethod.getParameters()).thenReturn( Collections.singletonList( newJavaParameter(newType("int"), "foo") ) );
        when(setFooMethod.isPropertyMutator()).thenReturn( true );
        when(setFooMethod.getPropertyName()).thenReturn( "foo" );
        when(setFooMethod.getPropertyType()).thenReturn( newType("int") );
        methods.add(setFooMethod);

        JavaMethod getFooMethod = mock(JavaMethod.class);
        when(getFooMethod.getName()).thenReturn( "getFoo" );
        when(getFooMethod.getReturns()).thenReturn( newType("int") );
        when(getFooMethod.isPropertyAccessor()).thenReturn( true );
        when(getFooMethod.getPropertyName()).thenReturn( "foo" );
        when(getFooMethod.getPropertyType()).thenReturn( newType("int") );
        methods.add( getFooMethod );
        
        setMethods( cls, methods );
        
        assertEquals(1, cls.getBeanProperties().size());
        BeanProperty fooProp = cls.getBeanProperties().get(0);
        assertEquals("foo", fooProp.getName());
        assertEquals(newType("int"), fooProp.getType());
        assertEquals(getFooMethod, fooProp.getAccessor());
        assertEquals(setFooMethod, fooProp.getMutator());
    }
    
    public void testToStringClass() {
    	setName(cls, "com.MyClass");
    	assertEquals("class com.MyClass", cls.toString());
    }
    
    public void testInnerClassToString() throws Exception {
    	JavaPackage jPackage = newJavaPackage("com.thoughtworks.qdox.model");
    	JavaClass jOuterClass = newJavaClass("OuterClass");
    	addClass(jPackage, jOuterClass);
    	JavaClass jInnerClass = newJavaClass("InnerClass");
    	addClass(jOuterClass, jInnerClass);
    	assertEquals("class com.thoughtworks.qdox.model.OuterClass$InnerClass", jInnerClass.toString());
    }
    
    public void testInnerClassType() {
        JavaPackage jPackage = newJavaPackage("com.thoughtworks.qdox.model");
        JavaClass jOuterClass = newJavaClass("OuterClass");
        addClass(jPackage, jOuterClass);
        JavaClass jInnerClass = newJavaClass("InnerClass");
        addClass(jOuterClass, jInnerClass);
        assertEquals("com.thoughtworks.qdox.model.OuterClass.InnerClass", jInnerClass.asType().getValue());
    }
    
    public void testInnerInterfaceToString() {
    	JavaPackage jPackage = newJavaPackage("com.thoughtworks.qdox.model");
    	JavaClass jOuterClass = newJavaClass("OuterClass");
    	addClass(jPackage, jOuterClass);
    	C jInnerInterface = newJavaClass("InnerInterface");
    	setInterface(jInnerInterface, true);
    	addClass(jOuterClass, jInnerInterface);
    	assertEquals("interface com.thoughtworks.qdox.model.OuterClass$InnerInterface", jInnerInterface.toString());
    }
    
    public void testToStringInterface() {
    	setName(cls, "com.MyClass");
    	setInterface(cls, true);
    	assertEquals("interface com.MyClass", cls.toString());
    }
    
    

    /**
     * @codehaus.jira QDOX-59
     */
    public void testBeanPropertiesAreReturnedInOrderDeclared() {
        List<JavaMethod> methods = new ArrayList<JavaMethod>();
        JavaMethod getFooMethod = mock(JavaMethod.class);
        when(getFooMethod.getName()).thenReturn( "getFoo" );
        when(getFooMethod.getReturns()).thenReturn( newType("int") );
        when(getFooMethod.getPropertyName()).thenReturn( "foo" );
        when(getFooMethod.isPropertyAccessor()).thenReturn( true );
        methods.add( getFooMethod );

        JavaMethod getBarMethod = mock(JavaMethod.class);
        when(getBarMethod.getName()).thenReturn( "getBar" );
        when(getBarMethod.getReturns()).thenReturn( newType("int") );
        when(getBarMethod.getPropertyName()).thenReturn( "bar" );
        when(getBarMethod.isPropertyAccessor()).thenReturn( true );
        methods.add( getBarMethod );
        
        JavaMethod getMcFNordMethod = mock(JavaMethod.class);
        when(getMcFNordMethod.getName()).thenReturn( "getMcFnord" );
        when(getMcFNordMethod.getReturnType()).thenReturn( newType("String") );
        when(getMcFNordMethod.getPropertyName()).thenReturn( "mcFnord" );
        when(getMcFNordMethod.isPropertyAccessor()).thenReturn( true );
        methods.add( getMcFNordMethod );
        setMethods( cls, methods );

        List<BeanProperty> properties = cls.getBeanProperties();
        assertEquals(3, properties.size());
        assertEquals("foo", properties.get(0).getName());
        assertEquals("bar", properties.get(1).getName());
        assertEquals("mcFnord", properties.get(2).getName());        
    }
    
    private List<Type> type(String[] typeNames) {
        List<Type> result = new LinkedList<Type>();
        for (int i = 0; i < typeNames.length; i++) {
            result.add(newType(typeNames[i]));
        }
        return result;
    }
    
    // QDOX-201
    public void testGetVarArgMethodSignature() {
        List<JavaMethod> methods = new ArrayList<JavaMethod>();
        JavaMethod simpleMethod = mock(JavaMethod.class);
        
        //both signatureMatches-methods are allowed
        when(simpleMethod.signatureMatches( "doSomething", Collections.singletonList( newType("String") ) )).thenReturn( true );
        when(simpleMethod.signatureMatches( "doSomething", Collections.singletonList( newType("String") ), false )).thenReturn( true );
        methods.add( simpleMethod );
        
        JavaMethod varArgMethod = mock(JavaMethod.class);
        when(varArgMethod.signatureMatches( "doSomething", Collections.singletonList( newType("String") ), true )).thenReturn( true );
        methods.add( varArgMethod );
        
        setMethods( cls, methods );
        
        assertEquals( simpleMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ) ) );
        assertEquals( simpleMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), false ) );
        assertEquals( simpleMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), true ) );
        assertEquals( simpleMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), false, false ) );
        assertEquals( varArgMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), false, true ) );
        assertEquals( simpleMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), true, false ) );
        assertEquals( varArgMethod, cls.getMethodBySignature( "doSomething", Collections.singletonList( newType("String") ), true, true ) );
    }
 
    public void testJavaLangObjectAsDefaultSuperClass() throws Exception {
        JavaClass clazz = newJavaClass( "a.b.Sample" );
        assertEquals( "java.lang.Object", clazz.getSuperClass().getJavaClass().getFullyQualifiedName() );
    }
    
}