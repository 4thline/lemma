package example.misc;

import example.util.DocletTest;
import org.testng.annotations.Test;

/**
 *
 */
public class ReaderSelectionTests extends DocletTest {

    @Test
    public void selectReader() throws Exception {
        
/*
        JavadocCitationProcessor processor = new JavadocCitationProcessor(getRootDoc());

        Document xmlDocument = getParser().newDocument();
        Element element = xmlDocument.createElement("a");
        element.setAttribute(Constants.ATTR_TYPE, Constants.TYPE_CITATION);

        element.setAttribute(Constants.ATTR_ADDRESS, "javadoc://com.myorg.MyClass");
        Anchor citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), JavadocReader.class);

        element.setAttribute(Constants.ATTR_ADDRESS, "javacode://com.myorg.MyClass");
        citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), JavacodeReader.class);

        element.setAttribute(Constants.ATTR_ADDRESS, "file://com/myorg/MyClass.java");
        citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), JavacodeRawReader.class);

        element.setAttribute(Constants.ATTR_ADDRESS, "file://foo/bar.txt");
        citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), PlaintextReader.class);
        element.setAttribute(Constants.ATTR_ADDRESS, "file://foo/bar.xhtml");

        citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), XMLReader.class);

        element.setAttribute(Constants.ATTR_OPTIONS, AnchorOption.OPT_READER + ": " + PlaintextReader.class.getName());
        citation = new Anchor(element);
        assertEquals(processor.getReader(citation).getClass(), PlaintextReader.class);
*/
    }
}
