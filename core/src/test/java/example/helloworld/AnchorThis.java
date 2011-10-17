package example.helloworld;

import example.util.DocletTest;
import org.seamless.xhtml.XHTML;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Referencing 'this'
 * <p>
 * Although URLs which reference the fully qualified name of a class, method, or package are
 * safe for refactoring in a good IDE, an equally safe shorthand is available with the
 * special path "<code>this</code>." For example, the following Javadoc will contain a
 * citation from the method it is written on:
 * </p>
 * <a class="citation" href="javacode://this" style="include: FRAG1"/>
 * <p>
 * You can use this shorthand for any citation anchor in Javadoc on classes, methods, and
 * packages. If your citation anchor is within the Javadoc comment of a class, 'this'
 * followed by a fragment references a method of the class:
 * </p>
 * <a class="citation" id="anchorthis_classwithfragment" href="javacode://this" style="include: FRAG2"/>
 */
public class AnchorThis extends DocletTest {

    // DOC: FRAG1
    /**
     * Calling Hello World
     * <p>
     * A anchor which cites this method's code:
     * </p>
     * <a class="citation" href="javacode://this"/>
     */
    public void callHelloWorld() {
        new HelloWorld();
    }
    // DOC: FRAG1

    @Test
    public void processDocumentation() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example08_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example08_output.xhtml")
        );
    }

    // DOC: FRAG2
    /**
     * This is My Class
     * <p>
     * And this is a citation anchor which references the source
     * of a method of this class:
     * </p>
     * <a class="citation" href="javacode://this#myMethod"/>
     */
    public static class MyClass {

        public void myMethod() {
            System.out.println("Hello World!");
        }
    }
    // DOC: FRAG2


}
