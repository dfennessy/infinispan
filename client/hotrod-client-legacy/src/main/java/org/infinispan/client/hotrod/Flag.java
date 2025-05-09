package org.infinispan.client.hotrod;

/**
 * Defines all the flags available in the Hot Rod client that can influence the behavior of operations.
 * Available flags:
 * <ul>
 *    <li>{@link #FORCE_RETURN_VALUE} - By default, previously existing values for {@link java.util.Map} operations are not
 *                                      returned. E.g. {@link RemoteCache#put(Object, Object)} does <i>not</i> return
 *                                      the previous value associated with the key.  By applying this flag, this default
 *                                      behavior is overridden for the scope of a single invocation, and the previous
 *                                      existing value is returned.</li>
 *    <li>{@link #DEFAULT_LIFESPAN}     This flag can either be used as a request flag during a put operation to mean
 *                                      that the default server lifespan should be applied or as a response flag meaning that
 *                                      the return entry has a default lifespan value</li>
 *    <li>{@link #DEFAULT_MAXIDLE}      This flag can either be used as a request flag during a put operation to mean
 *                                      that the default server maxIdle should be applied or as a response flag meaning that
 *                                      the return entry has a default maxIdle value</li>
 *    <li>{@link #SKIP_CACHE_LOAD}      Skips loading an entry from any configured
 *                                      <code>org.infinispan.persistence.spi.CacheLoader</code>}s.</li>
 *    <li>{@link #SKIP_INDEXING}        Used by the Query module only, it will prevent the indexes to be updated as a result
 *                                      of the current operations.
 *    <li>{@link #SKIP_LISTENER_NOTIFICATION}   Used when an operation wants to skip notifications to the registered listeners
 * </ul>
 *
 * @author Mircea.Markus@jboss.com
 * @since 4.1
 */
public enum Flag {

   /**
    * By default, previously existing values for {@link java.util.Map} operations are not returned. E.g. {@link RemoteCache#put(Object, Object)}
    * does <i>not</i> return the previous value associated with the key.
       * By applying this flag, this default behavior is overridden for the scope of a single invocation, and the previous
    * existing value is returned.
    */
   FORCE_RETURN_VALUE(0x0001),
   /**
    * This flag can either be used as a request flag during a put operation to mean that the default
    * server lifespan should be applied or as a response flag meaning that the return entry has a
    * default lifespan value
    */
   DEFAULT_LIFESPAN(0x0002),
   /**
    * This flag can either be used as a request flag during a put operation to mean that the default
    * server maxIdle should be applied or as a response flag meaning that the return entry has a
    * default maxIdle value
    */
   DEFAULT_MAXIDLE(0x0004),
   /**
    * Skips loading an entry from any configured <code>org.infinispan.persistence.spi.CacheLoader</code>s.
    */
   SKIP_CACHE_LOAD(0x0008),
   /**
    * Used by the Query module only, it will prevent the indexes to be updated as a result of the current operations.
    */
   SKIP_INDEXING(0x0010),
   /**
    * It will skip client listeners to be notified.
    * @since 9.4.15
    */
   SKIP_LISTENER_NOTIFICATION(0x0020)
   ;

   private final int flagInt;

   Flag(int flagInt) {
      this.flagInt = flagInt;
   }

   public int getFlagInt() {
      return flagInt;
   }
}
