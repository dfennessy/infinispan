package org.infinispan.test.hibernate.cache.commons.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.infinispan.Cache;

/**
 * Support class for tracking and cleaning up objects used in tests.
 *
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 */
public class CacheTestSupport {
	private static final String PREFER_IPV4STACK = "java.net.preferIPv4Stack";

    private final Set<Cache> caches = new HashSet<>();
    private final Set<TestRegionFactory> factories = new HashSet<>();
    private Exception exception;
    private String preferIPv4Stack;

    public void registerCache(Cache cache) {
        caches.add(cache);
    }

    public void registerFactory(TestRegionFactory factory) {
        factories.add(factory);
    }

    public void unregisterCache(Cache cache) {
        caches.remove( cache );
    }

    public void unregisterFactory(TestRegionFactory factory) {
        factories.remove( factory );
    }

    public void setUp() throws Exception {
        // Try to ensure we use IPv4; otherwise cluster formation is very slow
        preferIPv4Stack = System.getProperty(PREFER_IPV4STACK);
        System.setProperty(PREFER_IPV4STACK, "true");

        cleanUp();
        throwStoredException();
    }

    public void tearDown() throws Exception {
        if (preferIPv4Stack == null)
            System.clearProperty(PREFER_IPV4STACK);
        else
            System.setProperty(PREFER_IPV4STACK, preferIPv4Stack);

        cleanUp();
        throwStoredException();
    }

    private void cleanUp() {
        for (Iterator<TestRegionFactory> it = factories.iterator(); it.hasNext(); ) {
            try {
                it.next().stop();
            }
            catch (Exception e) {
                storeException(e);
            }
            finally {
                it.remove();
            }
        }
        factories.clear();

        for (Iterator<Cache> it = caches.iterator(); it.hasNext(); ) {
            try {
                it.next().stop();
            }
            catch (Exception e) {
                storeException(e);
            }
            finally {
                it.remove();
            }
        }
        caches.clear();
    }

    private void storeException(Exception e) {
        if (this.exception == null) {
            this.exception = e;
        }
    }

    private void throwStoredException() throws Exception {
        if (exception != null) {
            Exception toThrow = exception;
            exception = null;
            throw toThrow;
        }
    }

}
