package example.misc;

import org.fourthline.lemma.anchor.AnchorAddress;
import org.fourthline.lemma.anchor.Scheme;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 *
 */
public class CitationTests {

    @Test
    public void parseCitationTargets() throws Exception {

        AnchorAddress address =
                AnchorAddress.valueOf("javadoc://com.myorg.MyClass.Nested#someMethod(java.util.String[], Integer)");
        assertEquals(address.getScheme(), Scheme.JAVADOC);
        assertEquals(address.getPath(), "com.myorg.MyClass.Nested");
        assertEquals(address.getFragment(), "someMethod(java.util.String[], Integer)");

        address = AnchorAddress.valueOf("javacode://com.myorg.MyClass");
        assertEquals(address.getScheme(), Scheme.JAVACODE);
        assertEquals(address.getPath(), "com.myorg.MyClass");
        assertEquals(address.getFragment(), null);

        address = AnchorAddress.valueOf("javacode://com.myorg.MyClass#myMethod");
        assertEquals(address.getScheme(), Scheme.JAVACODE);
        assertEquals(address.getPath(), "com.myorg.MyClass");
        assertEquals(address.getFragment(), "myMethod()");

        address = AnchorAddress.valueOf("javacode://com.myorg");
        assertEquals(address.getScheme(), Scheme.JAVACODE);
        assertEquals(address.getPath(), "com.myorg");
        assertEquals(address.getFragment(), null);

        address = AnchorAddress.valueOf("/com/myorg/foo.xhtml");
        assertEquals(address.getScheme(), Scheme.FILE);
        assertEquals(address.getPath(), "com/myorg/foo.xhtml");
        assertEquals(address.getFragment(), null);

        address = AnchorAddress.valueOf("com/myorg/bar.txt");
        assertEquals(address.getScheme(), Scheme.FILE);
        assertEquals(address.getPath(), "com/myorg/bar.txt");
        assertEquals(address.getFragment(), null);
        
        address = AnchorAddress.valueOf("foo.txt");
        assertEquals(address.getScheme(), Scheme.FILE);
        assertEquals(address.getPath(), "foo.txt");
        assertEquals(address.getFragment(), null);

    }


}
