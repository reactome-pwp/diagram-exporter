package org.reactome.server.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Unit test for simple DiagramExporter.
 */
public class DiagramExporterTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName getName of the test case
     */
    public DiagramExporterTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DiagramExporterTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testAlignment() {
        final List<String> lines = new LinkedList<>();
        lines.add(String.format("\t%,10d entries", 12345));
        lines.add(String.format("\t%,10d entries", 543));
        lines.add(String.format("\t%,10d entries", 1000000));
        Collections.sort(lines);
        lines.forEach(System.out::println);
    }
}
