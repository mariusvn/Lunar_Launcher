package joptsimple.internal;

/**
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: Objects.java,v 1.1 2009/10/04 00:13:41 pholser Exp $
 */
public class Objects {
    public static void ensureNotNull( Object target ) {
        if ( target == null )
            throw new NullPointerException();
    }
}
