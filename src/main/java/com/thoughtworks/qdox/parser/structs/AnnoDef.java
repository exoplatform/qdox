package com.thoughtworks.qdox.parser.structs;

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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.qdox.parser.expression.AnnotationTransformer;
import com.thoughtworks.qdox.parser.expression.ElemValueDef;

public class AnnoDef extends LocatedDef implements ElemValueDef
{
    public TypeDef typeDef;
    public final Map<String, ElemValueDef> args = new LinkedHashMap<String, ElemValueDef>();

    @Override
    public boolean equals(Object obj) {
        AnnoDef annoDef = (AnnoDef) obj;
        return annoDef.typeDef.equals(typeDef) && annoDef.args.equals(args);
    }

    @Override
    public int hashCode() {
        return typeDef.hashCode() + args.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append('@');
        result.append(typeDef.name);
        if( !args.isEmpty() ) {
        	result.append('(');
            for(Iterator<Map.Entry<String, ElemValueDef>> i = args.entrySet().iterator(); i.hasNext();) {
            	result.append( i.next());
            	if(i.hasNext()) {
            		result.append(',');
            	}
            } 
            result.append(')');
        }
        return result.toString();
    }
    
    public AnnoDef getValue() {
    	return this;
    }
    
    public <U> U transform(AnnotationTransformer<U> transformer) {
    	return transformer.transform(this);
    }
}