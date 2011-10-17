package example.helloworld;

import org.seamless.xhtml.XHTML;
import example.util.DocletTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CustomizingAnchorOptions extends DocletTest {

    /**
     * Disabling Javadoc titles
     * <p>
     * When Lemma reads your Javadoc comment, it will use the "first sentence" (as defined by Javadoc) of the
     * comment as the title of the citation section. For example, {@link HelloWorld the HelloWorld class shown previously}
     * has the first sentence title "Just print a hello.". Lemma will remove the period at the end of the first
     * sentence automatically. You can disable this behavior with the {@code read-title} option:
     * </p>
     * <a class="citation" href="example/helloworld/example02_input.xhtml" style="include: LABEL">Citation</a>
     * <p>
     * Your Javadoc comment is now cited as it is, with no special treatment of the "first sentence". Alternatively,
     * you can use the {@code title} attribute of the anchor to override any "first sentence" title:
     * </p>
     * <a class="citation" href="example/helloworld/example03_input.xhtml" style="include: LABEL">Citation</a>
     */
    @Test
    public void javadocCustomTitle() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example02_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example02_output.xhtml")
        );

        output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example03_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example03_output.xhtml")
        );
    }

    /**
     * Including and excluding Javacode fragments
     * <p>
     * A {@code javacode://} citation which references a Java package, class, or method, will by default
     * include all lines of code of that package, class, or method. Very often you only want a fragment of the
     * code lines cited. First, you have to declare the fragments with line comments in your Java source file:
     * </p>
     * <a class="citation" href="javacode://example.helloworld.HelloWorld" id="javacode_HelloWorld_raw" style="clean-labels: false">Citation</a>
     * <p>
     * Within this Java source file, three fragments have been declared. The first two fragment are multi-line block
     * fragments, the third is a single-line fragment. The rules are: A line comment starting with a {@code // DOC:} prefix marks
     * the beginning of a fragment. The {@code DOC:} prefix is followed by a <em>fragment label</em>, which has to
     * match the following regular expression: {@code [A-Z_-]+[0-9]*} If the label is not repeated in a subsequent
     * line, the fragment only includes a single line. If the label is repeated in a subsequent line, all lines in
     * between and including the labeled lines are considered part of the same multi-line fragment block. A fragment
     * label comment can be placed in an empty line or at the end of a line, after content you wish to include in
     * the fragment.
     * </p>
     * <p>
     * You can now use the labeled fragments to selectively include and/or exclude content in citations:
     * </p>
     * <a class="citation" href="example/helloworld/example04_input.xhtml" style="include: LABEL">Citation</a>
     * <p>
     * This citation only includes the two named fragments, producing the following output:
     * </p>
     * <a class="citation" href="example/helloworld/example04_output.xhtml" style="include: LABEL;">Citation</a>
     * <p>
     * Exclusion of fragments occurs after inclusion, as the following example demonstrates:
     * </p>
     * <a class="citation" href="example/helloworld/example05_input.xhtml" style="include: LABEL">Citation</a>
     * <p>
     * The excluded fragment is now missing from the output:
     * </p>
     * <a class="citation" href="example/helloworld/example05_output.xhtml" style="include: LABEL;">Citation</a>
     */
    @Test
    public void javacodeFragments() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example04_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example04_output.xhtml")
        );

        output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example05_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example05_output.xhtml")
        );
    }

    /**
     * Removing fragment labels
     * <p>
     * Lemma will automatically remove any fragment labels - the specially formatted comments - from cited
     * content. It is sometimes useful, for example to document the actual usage of Lemma, to keep the
     * fragment labels in a piece of cited source code. For example, given the same {@code HelloWorld} class marked
     * with three fragments from the previous example, the following citation would preserve the labels:
     * </p>
     * <a class="citation" href="example/helloworld/example06_input.xhtml" style="include: LABEL">Citation</a>
     * <p>
     * This produces the following output:
     * </p>
     * <a class="citation" href="example/helloworld/example06_output.xhtml" style="include: LABEL;">Citation</a>
     * <p>
     * As a special case, you can also preserve only the fragment labels within a fragment block, and still
     * remove the labels that defined the actual fragment block:
     * </p>
     * <a class="citation" href="example/helloworld/example07_input.xhtml" style="include: LABEL">Citation</a>
     * <a class="citation" href="example/helloworld/example07_output.xhtml" style="include: LABEL;">Citation</a>
     */
    @Test
    public void javacodeCleanLabels() throws Exception {
        XHTML output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example06_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example06_output.xhtml")
        );

        output = getTemplatePipeline().execute(
                parseDocument("example/helloworld/example07_input.xhtml")
        );

        assertEquals(
                getParser().print(output),
                getContent("example/helloworld/example07_output.xhtml")
        );
    }

}
