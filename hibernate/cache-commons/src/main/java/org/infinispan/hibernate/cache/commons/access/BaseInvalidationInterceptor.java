package org.infinispan.hibernate.cache.commons.access;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.infinispan.Cache;
import org.infinispan.commands.CommandsFactory;
import org.infinispan.commands.FlagAffectedCommand;
import org.infinispan.context.impl.FlagBitSets;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Start;
import org.infinispan.interceptors.impl.BaseRpcInterceptor;
import org.infinispan.jmx.JmxStatisticsExposer;
import org.infinispan.jmx.annotations.DataType;
import org.infinispan.jmx.annotations.MBean;
import org.infinispan.jmx.annotations.ManagedAttribute;
import org.infinispan.jmx.annotations.ManagedOperation;
import org.infinispan.jmx.annotations.MeasurementType;
import org.infinispan.remoting.rpc.RpcOptions;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.ByteString;

@MBean
public abstract class BaseInvalidationInterceptor extends BaseRpcInterceptor implements JmxStatisticsExposer {
	private final AtomicLong invalidations = new AtomicLong(0);

	@Inject protected CommandsFactory commandsFactory;
	@Inject protected DistributionManager distributionManager;
	@Inject protected Cache cache;

	protected ByteString cacheName;
	protected boolean statisticsEnabled;
	protected RpcOptions syncRpcOptions;

	@Start
   void start() {
		this.cacheName = ByteString.fromString(cache.getName());
		this.setStatisticsEnabled(cacheConfiguration.statistics().enabled());
		syncRpcOptions = rpcManager.getSyncRpcOptions();
	}

	@ManagedOperation(
			description = "Resets statistics gathered by this component",
			displayName = "Reset statistics"
	)
	public void resetStatistics() {
		invalidations.set(0);
	}

	@ManagedAttribute(
			displayName = "Statistics enabled",
			description = "Enables or disables the gathering of statistics by this component",
			dataType = DataType.TRAIT,
			writable = true
	)
	public boolean getStatisticsEnabled() {
		return this.statisticsEnabled;
	}

	public void setStatisticsEnabled(boolean enabled) {
		this.statisticsEnabled = enabled;
	}

	@ManagedAttribute(
			description = "Number of invalidations",
			displayName = "Number of invalidations",
			measurementType = MeasurementType.TRENDSUP
	)
	public long getInvalidations() {
		return invalidations.get();
	}

	protected void incrementInvalidations() {
		if (statisticsEnabled) {
			invalidations.incrementAndGet();
		}
	}

	protected List<Address> getMembers() {
		return distributionManager.getCacheTopology().getMembers();
	}

	protected boolean isPutForExternalRead(FlagAffectedCommand command) {
      return command.hasAnyFlag(FlagBitSets.PUT_FOR_EXTERNAL_READ);
   }
}
