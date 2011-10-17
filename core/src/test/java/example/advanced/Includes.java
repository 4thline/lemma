package example.advanced;

import example.util.DocletTest;
import org.seamless.xhtml.XHTML;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class Includes extends DocletTest {

      @Test
    public void processIncludes() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/advanced/include/master.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/advanced/include/output.xhtml")
        );
    }
}
