package org.apache.xmlgraphics.store;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Storer of InputStreams
 */
public final class InputStreamStorer extends Storer {

    /** an inputstream */
    private final InputStream in;

    /**
     * Constructor
     *
     * @param store our resource store
     * @param in an inputstream
     */
    protected InputStreamStorer(FileStore store, InputStream in) {
        super(store);
        this.in = in;
    }

    /** {@inheritDoc} */
    protected void doStore() throws IOException {
        IOUtils.copy(in, store.fos);
    }
}