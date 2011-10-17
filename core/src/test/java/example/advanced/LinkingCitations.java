package example.advanced;

import example.helloworld.HelloWorld;
import org.seamless.xhtml.XHTML;
import example.util.DocletTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Creating links to citations
 * <p>
 * Lemma will recognize any {@code @link} tags in your Javadoc comments and try to
 * match them by cross-referencing all your citations for the same Java package, method, or class.
 * Consider the following example:
 * </p>
 * <a class="citation" href="javacode://example.advanced.LinkingCitations" style="include: FRAG1"/>
 * <p>
 * When you include this Javadoc comment with a Lemma citation, a regular XHTML anchor will
 * be created automatically. Lemma will try to match the target of the link to any of your citations.
 * So if you also have a citation of the {@code HelloWorld} class in your manual, the following link
 * will be automatically generated:
 * </p>
 * <a class="citation" href="example/advanced/example01_output.xhtml" style="include: FRAG1"/>
 * <p>
 * The title of the anchor will match the title of the target - if the target has a title. If neither
 * the target or its title can be found, a warning message will be logged during processing and broken
 * link placeholder will be set in the output XHTML.
 * </p>
 */
public class LinkingCitations extends DocletTest {

    // DOC: FRAG1
    /**
     * This method calls
     * the {@link example.helloworld.HelloWorld} class.
     */
    public void callHelloWorld() {
        new HelloWorld();
    }
    // DOC: FRAG1

    /**
     * This is a broken link to
     * the {@link HelloWorld#getMessage} method
     * followed by {@link HelloWorld#sayHello this link} to the source of
     * another method.
     */
    public void brokenLinks() {
    }

    @Test
    public void processDocumentation() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/advanced/example01_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/advanced/example01_output.xhtml")
        );
    }

}
