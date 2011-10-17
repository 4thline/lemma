package example.helloworld;

/**
 * Just print a hello.
 * <p>
 * This class doesn't do much, its only purpose is to print a
 * message to <code>System.out</code> without any fuzz.
 * </p>
 */
public class HelloWorld {

    // DOC:FRAGMENT1

    /**
     * Prints "Hello World".
     */
    public void sayHello() {
        System.out.println(getMessage());
    }

    // DOC:FRAGMENT1

    public String getMessage() {    // DOC: FRAGMENT2
        return "Hello World!";      // DOC: FRAGMENT3
    }                               // DOC: FRAGMENT2

}
