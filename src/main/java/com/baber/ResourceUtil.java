package com.baber;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * helper util class that knows all the magic technics for loading files in java
 */
@Slf4j
@SuppressWarnings({
        "PMD.UseProperClassLoader", "PMD.AvoidFileStream"
})
public final class ResourceUtil
{
    /**
     * private util constructor
     */
    private ResourceUtil()
    {
        // nothing to see here
    }

    /**
     * try to open a file using the various java techniques
     */
    public static InputStream openFile(final String resource)
    {
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(resource);
        }
        catch (final FileNotFoundException e)
        {
            log.warn("Couldn't find file {} in current directory", resource);
        }
        if (inputStream == null)
        {
            inputStream = ResourceUtil.class.getResourceAsStream(resource);
        }
        if (inputStream == null)
        {
            inputStream = ResourceUtil.class.getClassLoader().getResourceAsStream(resource);
        }
        if (inputStream == null)
        {
            inputStream = ClassLoader.getSystemResourceAsStream(resource);
        }

        return inputStream;
    }
}
