// ========================================================================
// Copyright (c) 2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================
package org.eclipse.jetty.annotations;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jetty.annotations.AnnotationParser.AnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationParser.AnnotationNode;




public class TestAnnotationParser extends TestCase
{
    
    public void testSampleAnnotation ()
    throws Exception
    {      
        
        
        String[] classNames = new String[]{"org.eclipse.jetty.annotations.ClassA"};
        AnnotationParser parser = new AnnotationParser();
        
        
        class SampleAnnotationHandler implements AnnotationHandler
        {
            List<String> methods = Arrays.asList("a", "b", "c", "d", "l");
            
            

            public void handleClass(String className, int version, int access, String signature, String superName, String[] interfaces, String annotation,
                                    List<AnnotationNode> values)
            {
                assertEquals ("org.eclipse.jetty.annotations.ClassA", className);
            }

            public void handleField(String className, String fieldName, int access, String fieldType, String signature, Object value, String annotation,
                                   List<AnnotationNode> values)
            {
              assertEquals ("m", fieldName);
              assertEquals (org.objectweb.asm.Type.OBJECT, org.objectweb.asm.Type.getType(fieldType).getSort());
              assertEquals (1, values.size());
              AnnotationNode anv1 = values.get(0);
              assertEquals ("value", anv1.getName());
              assertEquals (7, anv1.getValue());

            }

            public void handleMethod(String className, String methodName, int access, String params, String signature, String[] exceptions, String annotation,
                                     List<AnnotationNode> values)
            {
               assertEquals("org.eclipse.jetty.annotations.ClassA", className);
               assertTrue(methods.contains(methodName));
               assertEquals("org.eclipse.jetty.annotations.Sample", annotation);
            }
            
        }
        
        parser.registerAnnotationHandler("org.eclipse.jetty.annotations.Sample", new SampleAnnotationHandler());
        
        long start = System.currentTimeMillis();
        parser.parse(classNames, new ClassNameResolver () 
        {
            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }
            
        });
        long end = System.currentTimeMillis();

        System.err.println("Time to parse class: "+((end-start)));
    } 

    
    public void testMultiAnnotation ()
    throws Exception
    {
        String[] classNames = new String[]{"org.eclipse.jetty.annotations.ClassB"};
        AnnotationParser parser = new AnnotationParser();
        
        
        class MultiAnnotationHandler implements AnnotationHandler
        {
            public void handleClass(String className, int version, int access, String signature, String superName, String[] interfaces, String annotation,
                                    List<AnnotationNode> values)
            {
                assertTrue("org.eclipse.jetty.annotations.ClassB".equals(className));
               
                for (AnnotationNode anv: values)
                {
                   System.err.println(anv.toString());
                }
            }

            public void handleField(String className, String fieldName, int access, String fieldType, String signature, Object value, String annotation,
                                    List<AnnotationNode> values)
            {
                //there should not be any
                fail();
            }

            public void handleMethod(String className, String methodName, int access, String params, String signature, String[] exceptions, String annotation,
                                     List<AnnotationNode> values)
            { 
                assertTrue("org.eclipse.jetty.annotations.ClassB".equals(className));
                assertTrue("a".equals(methodName));
                for (AnnotationNode anv: values)
                {
                    System.err.println(anv.toString());
                }
            }
        }
        
        parser.registerAnnotationHandler("org.eclipse.jetty.annotations.Multi", new MultiAnnotationHandler());
        parser.parse(classNames, null);
    }
}
