package org.infinispan.statetransfer;

import static org.infinispan.factories.KnownComponentNames.TIMEOUT_SCHEDULE_EXECUTOR;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.infinispan.commons.IllegalLifecycleStateException;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.factories.annotations.ComponentName;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.util.concurrent.CompletableFutures;
import org.infinispan.util.concurrent.ConditionFuture;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

/**
 * {@code StateTransferLock} implementation.
 *
 * @author anistor@redhat.com
 * @author Dan Berindei
 * @since 5.2
 */
@Scope(Scopes.NAMED_CACHE)
public class StateTransferLockImpl implements StateTransferLock {
   private static final Log log = LogFactory.getLog(StateTransferLockImpl.class);
   private static final int TOPOLOGY_ID_STOPPED = Integer.MAX_VALUE;

   private final ReadWriteLock ownershipLock = new ReentrantReadWriteLock();

   private volatile int topologyId = -1;
   private ConditionFuture<StateTransferLockImpl> topologyFuture;

   private volatile int transactionDataTopologyId = -1;
   private ConditionFuture<StateTransferLockImpl> transactionDataFuture;

   private long stateTransferTimeout;
   private long remoteTimeout;

   @Inject
   void inject(@ComponentName(TIMEOUT_SCHEDULE_EXECUTOR) ScheduledExecutorService timeoutExecutor,
               Configuration configuration) {
      topologyFuture = new ConditionFuture<>(timeoutExecutor);
      transactionDataFuture = new ConditionFuture<>(timeoutExecutor);

      stateTransferTimeout = configuration.clustering().stateTransfer().timeout();
      remoteTimeout = configuration.clustering().remoteTimeout();
   }

   @Stop
   void stop() {
      notifyTopologyInstalled(TOPOLOGY_ID_STOPPED);
      notifyTransactionDataReceived(TOPOLOGY_ID_STOPPED);
   }

   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   @Override
   public void acquireExclusiveTopologyLock() {
      ownershipLock.writeLock().lock();
   }

   @Override
   public void releaseExclusiveTopologyLock() {
      ownershipLock.writeLock().unlock();
   }

   @SuppressWarnings("LockAcquiredButNotSafelyReleased")
   @Override
   public void acquireSharedTopologyLock() {
      ownershipLock.readLock().lock();
   }

   @Override
   public void releaseSharedTopologyLock() {
      ownershipLock.readLock().unlock();
   }

   @Override
   public void notifyTransactionDataReceived(int topologyId) {
      if (topologyId < transactionDataTopologyId) {
         log.debugf("Trying to set a topology id (%d) that is lower than the current one (%d)", topologyId,
                    this.topologyId);
         return;
      }
      if (log.isTraceEnabled()) {
         log.tracef("Signalling transaction data received for topology %d", topologyId);
      }
      transactionDataTopologyId = topologyId;

      transactionDataFuture.update(this);
   }

   @Override
   public CompletionStage<Void> transactionDataFuture(int expectedTopologyId) {
      if (topologyId == TOPOLOGY_ID_STOPPED)
         return CompletableFutures.completedExceptionFuture(new IllegalLifecycleStateException());

      if (transactionDataTopologyId >= expectedTopologyId)
         return CompletableFutures.completedNull();

      if (log.isTraceEnabled()) {
         log.tracef("Waiting for transaction data for topology %d, current topology is %d", expectedTopologyId,
                    transactionDataTopologyId);
      }

      return transactionDataFuture.newConditionStage(stli -> stli.transactionDataTopologyId >= expectedTopologyId,
                                                     remoteTimeout, TimeUnit.MILLISECONDS);
   }

   @Override
   public boolean transactionDataReceived(int expectedTopologyId) {
      if (log.isTraceEnabled()) log.tracef("Checking if transaction data was received for topology %s, current topology is %s",
            expectedTopologyId, transactionDataTopologyId);
      return transactionDataTopologyId >= expectedTopologyId;
   }

   @Override
   public void notifyTopologyInstalled(int topologyId) {
      if (topologyId < this.topologyId) {
         log.debugf("Trying to set a topology id (%d) that is lower than the current one (%d)", topologyId,
                    this.topologyId);
         return;
      }
      if (log.isTraceEnabled()) {
         log.tracef("Signalling topology %d is installed", topologyId);
      }
      this.topologyId = topologyId;

      topologyFuture.update(this);
   }

   @Override
   public CompletionStage<Void> topologyFuture(int expectedTopologyId) {
      if (topologyId == TOPOLOGY_ID_STOPPED)
         return CompletableFutures.completedExceptionFuture(new IllegalLifecycleStateException());

      if (topologyId >= expectedTopologyId)
         return CompletableFutures.completedNull();

      if (log.isTraceEnabled()) {
         log.tracef("Waiting for topology %d to be installed, current topology is %d", expectedTopologyId, topologyId);
      }

      return topologyFuture.newConditionStage(stli -> stli.topologyId >= expectedTopologyId,
                                              stateTransferTimeout, TimeUnit.MILLISECONDS);
   }

   @Override
   public boolean topologyReceived(int expectedTopologyId) {
      return topologyId >= expectedTopologyId;
   }
}
