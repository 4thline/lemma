/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package example.advanced;

/**
 * HelloWorld with Callouts
 * <p>
 * Have a look at the following example:
 * </p>
 * <a class="citation"
 *    href="javacode://this"
 *    style="include: SAY_HELLO; exclude: EXC;"/>
 * <p>
 * There should be some documentation items listed above.
 * </p>
 * <p>
 * Another example with callouts not in a list:
 * </p>
 * <a class="citation"
 *    href="javacode://this"
 *    id="HelloWorldCallouts_sayHelloTwo"
 *    style="include: SAY_HELLO_TWO;"/>
 * <p>
 * When the aliens arrived, they are friendly at first (1). Obviously (2), they
 * have some <em>demands!</em>
 * </p>
 *
 */
public class HelloWorldCallouts {

    public void sayHello() { // DOC:SAY_HELLO
        /**
         * Just a regular comment, not a callout.
         */
        /* DOC:CALLOUT
            When the aliens arrived, they are friendly at first.
        */
        System.out.println("Hello World!");

        /**
         * DOC:CALLOUT Obviously, they have
         * some <em>demands</em>!
         */
        System.out.println("Take me to your leader!");
        System.out.println("Not documented..."); // DOC:EXC
    } // DOC:SAY_HELLO

    public void sayHelloTwo() { // DOC:SAY_HELLO_TWO
        /* DOC:CALLOUT */
        System.out.println("Hello World!");

        /**
         * DOC:CALLOUT */
        System.out.println("Take me to your leader!");
    } // DOC:SAY_HELLO_TWO
}
