package com.thoughtworks.qdox;

import java.io.*;
import java.util.List;

import junit.framework.TestCase;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

public class JavaDocBuilderTest extends TestCase {

	JavaDocBuilder builder = new JavaDocBuilder();

	public JavaDocBuilderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		createFile("tmp/sourcetest/com/blah/Thing.java", "com.blah", "Thing");
		createFile("tmp/sourcetest/com/blah/Another.java", "com.blah", "Another");
		createFile("tmp/sourcetest/com/blah/subpackage/Cheese.java", "com.blah.subpackage", "Cheese");
		createFile("tmp/sourcetest/com/blah/Ignore.notjava", "com.blah", "Ignore");
	}

	public void testParsingMultipleJavaFiles(){
		builder.addSource(new StringReader(createTestClassList()));
		builder.addSource(new StringReader(createTestClass()));
		JavaSource[] sources = builder.getSources();
		assertEquals(2, sources.length);

		JavaClass testClassList = sources[0].getClasses()[0];
		assertEquals("TestClassList", testClassList.getName());
		assertEquals("com.thoughtworks.util.TestClass", testClassList.getSuperClass().getValue());

		JavaClass testClass = sources[1].getClasses()[0];
		assertEquals("TestClass", testClass.getName());


		JavaClass testClassListByName = builder.getClassByName("com.thoughtworks.qdox.TestClassList");
		assertEquals("TestClassList", testClassListByName.getName());

		JavaClass testClassByName = builder.getClassByName("com.thoughtworks.util.TestClass");
		assertEquals("TestClass", testClassByName.getName());

		assertNull(builder.getClassByName("this.class.should.not.Exist"));
	}

	private String createTestClassList(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("package com.thoughtworks.qdox;");
		buffer.append("import com.thoughtworks.util.*;");
		buffer.append("public class TestClassList extends TestClass{");
		buffer.append("private int numberOfTests;");
		buffer.append("public int getNumberOfTests(){return numberOfTests;}");
		buffer.append("public void setNumberOfTests(int numberOfTests){this.numberOfTests = numberOfTests;}");
		buffer.append("}");
		return buffer.toString();
	}

	private String createTestClass(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("package com.thoughtworks.util;");
		buffer.append("public class TestClass{");
		buffer.append("public void test(){}");
		buffer.append("}");
		return buffer.toString();
	}

	public void testParseWithInnerClass(){
		builder.addSource(new StringReader(createOuter()));
		JavaSource[] sources = builder.getSources();
		assertEquals(1, sources.length);

		JavaClass outer = sources[0].getClasses()[0];
		assertEquals("Outer", outer.getName());
		assertEquals("foo.bar.Outer", outer.getFullyQualifiedName());

		assertEquals(1, outer.getFields().length);
		assertEquals("int", outer.getFields()[0].getType().getValue());

		assertEquals(1, outer.getMethods().length);
		assertEquals("outerMethod", outer.getMethods()[0].getName());

		assertEquals(1, outer.getClasses().length);
		JavaClass inner = outer.getClasses()[0];
		assertEquals("Inner", inner.getName());
		assertEquals("foo.bar.Outer.Inner", inner.getFullyQualifiedName());
		
		assertEquals(1, inner.getMethods().length);
		assertEquals("innerMethod", inner.getMethods()[0].getName());
	}

	private String createOuter(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("package foo.bar;");
		buffer.append("public class Outer {");
		buffer.append("  private int numberOfTests;");
		buffer.append("  class Inner {");
		buffer.append("    public int innerMethod(){return System.currentTimeMillis();}");
		buffer.append("  }");
		buffer.append("  public void outerMethod(int count){}");
		buffer.append("}");
		return buffer.toString();
	}

	public void testSourceTree() throws Exception {
		builder.addSourceTree(new File("tmp/sourcetest"));

		assertNotNull(builder.getClassByName("com.blah.Thing"));
		assertNotNull(builder.getClassByName("com.blah.Another"));
		assertNotNull(builder.getClassByName("com.blah.subpackage.Cheese"));
		assertNull(builder.getClassByName("com.blah.Ignore"));
	}

	public void testRecordFile() throws Exception {
		builder.addSource(new File("tmp/sourcetest/com/blah/Thing.java"));

		JavaSource[] sources = builder.getSources();
		assertEquals(1, sources.length);
		assertEquals(new File("tmp/sourcetest/com/blah/Thing.java"),
					 sources[0].getFile());
	}

	public void testSearcher() throws Exception {
		builder.addSourceTree(new File("tmp/sourcetest"));

		List results = builder.search(new Searcher() {
			public boolean eval(JavaClass cls) {
				return cls.getPackage().equals("com.blah");
			}
		});

		assertEquals(2, results.size());
		assertEquals("Another", ((JavaClass)results.get(0)).getName());
		assertEquals("Thing", ((JavaClass)results.get(1)).getName());
	}

	private void createFile(String fileName, String packageName, String className) throws Exception {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		writer.write("// this file generated by JavaDocBuilderTest - feel free to delete it\n");
		writer.write("package " + packageName + ";\n\n");
		writer.write("public class " + className + " {\n\n  // empty\n\n}\n");
		writer.close();
	}

	public void testDefaultClassLoader() throws Exception {
		String in = ""
			+ "package x;"
			+ "import java.util.*;"
			+ "import java.awt.*;"
			+ "class X extends List {}";
		builder.addSource(new StringReader(in));
		JavaClass cls = builder.getClassByName("x.X");
		assertEquals("java.util.List", cls.getSuperClass().getValue());
	}

	public void testAddMoreClassLoaders() throws Exception {

		builder.getClassLibrary().addClassLoader(new ClassLoader() {
			public Class loadClass(String name) throws ClassNotFoundException {
				return name.equals("com.thoughtworks.Spoon") ? this.getClass() : null;
			}
		});

		builder.getClassLibrary().addClassLoader(new ClassLoader() {
			public Class loadClass(String name) throws ClassNotFoundException {
				return name.equals("com.thoughtworks.Fork") ? this.getClass() : null;
			}
		});

		String in = ""
			+ "package x;"
			+ "import java.util.*;"
			+ "import com.thoughtworks.*;"
			+ "class X {"
			+ " Spoon a();"
			+ " Fork b();"
			+ " Cabbage c();"
			+ "}";
		builder.addSource(new StringReader(in));

		JavaClass cls = builder.getClassByName("x.X");
		assertEquals("com.thoughtworks.Spoon", cls.getMethods()[0].getReturns().getValue());
		assertEquals("com.thoughtworks.Fork", cls.getMethods()[1].getReturns().getValue());
		// unresolved
		assertEquals("Cabbage", cls.getMethods()[2].getReturns().getValue());

	}

	public void testSerializable() throws Exception {
		builder.addSource(new StringReader("package test; public class X{}"));
		assertEquals("X", builder.getSources()[0].getClasses()[0].getName());

		// serialize
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(buffer);
		oos.writeObject(builder);
		oos.close();
		builder = null;

		// unserialize
		ByteArrayInputStream input = new ByteArrayInputStream(buffer.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(input);
		JavaDocBuilder newBuilder = (JavaDocBuilder)ois.readObject();

		assertEquals("X", newBuilder.getSources()[0].getClasses()[0].getName());

	}

	public void testSaveAndRestore() throws Exception {
		File file = new File("tmp/sourcetest/cache.obj");
		builder.addSourceTree(new File("tmp/sourcetest"));
		builder.save(file);

		JavaDocBuilder newBuilder = JavaDocBuilder.load(file);
		assertNotNull(newBuilder.getClassByName("com.blah.subpackage.Cheese"));
		assertNull(newBuilder.getClassByName("com.blah.Ignore"));

		newBuilder.addSource(new StringReader("package x; import java.util.*; class Z extends List{}"));
		assertEquals("java.util.List", newBuilder.getClassByName("x.Z").getSuperClass().getValue());

	}


}
