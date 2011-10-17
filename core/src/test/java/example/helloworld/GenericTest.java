package example.helloworld;

import com.sun.tools.javac.util.Pair;
import org.seamless.xhtml.XHTML;
import example.util.DocletTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests that are not documented in the manual but probably should be documented later.
 */
public class GenericTest extends DocletTest {

    @DataProvider(name = "samples")
    public Object[][] getSamples() {
        return new Object[][]{
                {new Pair<String, String>("example/helloworld/generic01_input.xhtml", "example/helloworld/generic01_output.xhtml")},
                {new Pair<String, String>("example/helloworld/generic02_input.xhtml", "example/helloworld/generic02_output.xhtml")},
                {new Pair<String, String>("example/helloworld/generic03_input.xhtml", "example/helloworld/generic03_output.xhtml")},
                {new Pair<String, String>("example/helloworld/generic04_input.xhtml", "example/helloworld/generic04_output.xhtml")},
                {new Pair<String, String>("example/helloworld/generic05_input.xhtml", "example/helloworld/generic05_output.xhtml")},
                {new Pair<String, String>("example/helloworld/generic06_input.xhtml", "example/helloworld/generic06_output.xhtml")},
        };
    }

    @DataProvider(name = "errorSamples")
    public Object[][] getErrorSamples() {
        return new Object[][]{
                {"example/helloworld/error01_input.xhtml"},
        };
    }

    @Test(dataProvider = "samples")
    public void process(Pair<String, String> sample) throws Exception {

        XHTML output = getTemplatePipeline().execute(parseDocument(sample.fst));
        assertEquals(getParser().print(output), getContent(sample.snd));

    }

    @Test(dataProvider = "errorSamples", expectedExceptions = IllegalStateException.class)
    public void processFail(String sample) throws Exception {
        getTemplatePipeline().execute(parseDocument(sample));
    }

    /**
     * This is a nested class.
     * <p>
     * And this is its documentation for testing purposes.
     * </p>
     */
    public static class MyNestedClass {

        public class HelloWorld {
            public String getMessage() {
                return "Hello World";
            }
        }

        /**
         * Documentation of the print method.
         * <p>
         * Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum vel tellus eros,
         * quis molestie lectus. Integer rutrum imperdiet enim in ullamcorper.
         * </p>

         * @param args The arguments.
         */
        public void printHelloWorld(String[] args) {

            String message = new HelloWorld().getMessage();
            System.out.println(message);

            System.out.println("Arguments: " + args.length);
        }

        public void sayHello() {
            System.out.println("Hello!");
            System.out.println("World!");
        }
    }

}
