package org.schemaspy.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by rkasa on 2016-12-18.
 */
public class Version {
    private Properties prop;

    private String getEntryById( String id )
    {
        return prop.getProperty( id );
    }

    public String getVersion() {
        return getEntryById("version");
    }

    public Version()
    {
        InputStream resourceAsStream =
                this.getClass().getResourceAsStream( "/version.properties" );
        this.prop = new Properties();
        try
        {
            this.prop.load( resourceAsStream );
        }
        catch ( IOException e )
        {
            // FIXME: This should be done by using a logging framework like log4j etc.
            e.printStackTrace();
        }

    }
}
