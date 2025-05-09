package org.infinispan.test.hibernate.cache.commons.functional.cluster;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.service.spi.Configurable;

import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

/**
 * @author Steve Ebersole
 */
public class DualNodeJtaPlatformImpl implements JtaPlatform, Configurable {
	private String nodeId;

	public DualNodeJtaPlatformImpl() {
	}

	@Override
	public void configure(Map configurationValues) {
		nodeId = (String) configurationValues.get( DualNodeTest.NODE_ID_PROP );
		if ( nodeId == null ) {
		  throw new HibernateException(DualNodeTest.NODE_ID_PROP + " not configured");
	  }
	}

	@Override
	public TransactionManager retrieveTransactionManager() {
		return DualNodeJtaTransactionManagerImpl.getInstance( nodeId );
	}

	@Override
	public UserTransaction retrieveUserTransaction() {
		throw new TransactionException( "UserTransaction not used in these tests" );
	}

	@Override
	public Object getTransactionIdentifier(Transaction transaction) {
		return transaction;
	}

	@Override
	public boolean canRegisterSynchronization() {
		return JtaStatusHelper.isActive( retrieveTransactionManager() );
	}

	@Override
	public void registerSynchronization(Synchronization synchronization) {
		try {
			retrieveTransactionManager().getTransaction().registerSynchronization( synchronization );
		}
		catch (Exception e) {
			throw new TransactionException( "Could not obtain transaction from TM" );
		}
	}

	@Override
	public int getCurrentStatus() throws SystemException {
		return JtaStatusHelper.getStatus( retrieveTransactionManager() );
	}
}
