/*
 The MIT License

 Copyright (c) 2009 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package joptsimple.util;

import static joptsimple.internal.Strings.*;

/**
 * <p>A simple string key/string value pair.</p>
 *
 * <p>This is useful as an argument type for options whose values take on the form
 * <kbd>key=value</kbd>, such as JVM command line system properties.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: KeyValuePair.java,v 1.9 2009/08/13 00:34:36 pholser Exp $
 */
public final class KeyValuePair {
    public final String key;
    public final String value;

    private KeyValuePair( String key, String value ) {
        this.key = key;
        this.value = value;
    }

    /**
     * Parses a string assumed to be of the form <kbd>key=value</kbd> into its parts.
     *
     * @param stringRepresentation key-value string
     * @return a key-value pair
     * @throws NullPointerException if {@code stringRepresentation} is {@code null}
     */
    public static KeyValuePair valueOf( String stringRepresentation ) {
        int equalsIndex = stringRepresentation.indexOf( '=' );
        if ( equalsIndex == -1 )
            return new KeyValuePair( stringRepresentation, EMPTY );

        String aKey = stringRepresentation.substring( 0, equalsIndex );
        String aValue;

        if ( equalsIndex == stringRepresentation.length() - 1 )
            aValue = EMPTY;
        else
            aValue = stringRepresentation.substring( equalsIndex + 1 );

        return new KeyValuePair( aKey, aValue );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object that ) {
        if ( !( that instanceof KeyValuePair ) )
            return false;

        KeyValuePair other = (KeyValuePair) that;
        return key.equals( other.key ) && value.equals( other.value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return key + '=' + value;
    }
}
