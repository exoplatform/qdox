package com.thoughtworks.qdox.model;

public class DefaultJavaParameterTest extends JavaParameterTest<DefaultJavaParameter>
{

    public DefaultJavaParameterTest( String s )
    {
        super( s );
    }

    @Override
    protected DefaultJavaParameter newJavaParameter( Type type, String name )
    {
        return new DefaultJavaParameter( type, name );
    }

    @Override
    protected DefaultJavaParameter newJavaParameter( Type type, String name, boolean varArgs )
    {
        return new DefaultJavaParameter( type, name, varArgs );
    }

    @Override
    protected void setMethod( DefaultJavaParameter parameter, JavaMethod method )
    {
        parameter.setParentMethod( method );
    }

}