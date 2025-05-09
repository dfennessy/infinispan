package org.infinispan.commands.functional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.commands.CommandInvocationId;
import org.infinispan.commands.remote.CacheRpcCommand;
import org.infinispan.commands.write.ValueMatcher;
import org.infinispan.commands.write.WriteCommand;
import org.infinispan.context.impl.FlagBitSets;
import org.infinispan.encoding.DataConversion;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.functional.impl.Params;
import org.infinispan.marshall.protostream.impl.MarshallableMap;
import org.infinispan.metadata.impl.PrivateMetadata;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.ByteString;
import org.infinispan.util.concurrent.locks.RemoteLockCommand;

public abstract class AbstractWriteManyCommand<K, V> implements CacheRpcCommand, WriteCommand, FunctionalCommand<K, V>, RemoteLockCommand {

   @ProtoField(1)
   ByteString cacheName;

   @ProtoField(2)
   final CommandInvocationId commandInvocationId;

   @ProtoField(3)
   boolean forwarded = false;

   @ProtoField(4)
   int topologyId = -1;

   @ProtoField(5)
   Params params;
   // TODO: this is used for the non-modifying read-write commands. Move required flags to Params
   // and make sure that ClusteringDependentLogic checks them.
   @ProtoField(6)
   long flags;

   @ProtoField(7)
   DataConversion keyDataConversion;

   @ProtoField(8)
   DataConversion valueDataConversion;

   @ProtoField(9)
   MarshallableMap<Object, PrivateMetadata> getInternalMetadata() {
      return MarshallableMap.create(internalMetadataMap);
   }

   Map<Object, PrivateMetadata> internalMetadataMap;
   Address origin;

   // ProtoFactory constructor
   protected AbstractWriteManyCommand(ByteString cacheName, CommandInvocationId commandInvocationId, boolean forwarded, int topologyId,
                                      Params params, long flags, DataConversion keyDataConversion,
                                      DataConversion valueDataConversion, MarshallableMap<Object, PrivateMetadata> internalMetadataMap) {
      this(cacheName, commandInvocationId, forwarded, topologyId, params, flags, keyDataConversion, valueDataConversion,
            MarshallableMap.unwrap(internalMetadataMap));
   }

   private AbstractWriteManyCommand(ByteString cacheName, CommandInvocationId commandInvocationId, boolean forwarded,
                                      int topologyId, Params params, long flags, DataConversion keyDataConversion,
                                      DataConversion valueDataConversion, Map<Object, PrivateMetadata> internalMetadataMap) {
      this.cacheName = cacheName;
      this.commandInvocationId = commandInvocationId;
      this.forwarded = forwarded;
      this.topologyId = topologyId;
      this.params = params;
      this.flags = flags;
      this.keyDataConversion = keyDataConversion;
      this.valueDataConversion = valueDataConversion;
      this.internalMetadataMap = internalMetadataMap;
   }

   protected AbstractWriteManyCommand(ByteString cacheName, CommandInvocationId commandInvocationId,
                                      Params params,
                                      DataConversion keyDataConversion,
                                      DataConversion valueDataConversion) {
      this(cacheName, commandInvocationId, false, -1, params, params.toFlagsBitSet(), keyDataConversion ,
            valueDataConversion, new ConcurrentHashMap<>());
   }

   protected AbstractWriteManyCommand(AbstractWriteManyCommand<K, V> c) {
      this(c.cacheName, c.commandInvocationId, false, c.topologyId, c.params, c.flags, c.keyDataConversion,
            c.valueDataConversion, new ConcurrentHashMap<>(c.internalMetadataMap));
   }

   @Override
   public void init(ComponentRegistry componentRegistry) {
      componentRegistry.wireDependencies(keyDataConversion);
      componentRegistry.wireDependencies(valueDataConversion);
   }

   @Override
   public ByteString getCacheName() {
      return cacheName;
   }

   @Override
   public Address getOrigin() {
      return origin;
   }

   @Override
   public void setOrigin(Address origin) {
      this.origin = origin;
   }

   @Override
   public int getTopologyId() {
      return topologyId;
   }

   @Override
   public void setTopologyId(int topologyId) {
      this.topologyId = topologyId;
   }

   public boolean isForwarded() {
      return forwarded;
   }

   public void setForwarded(boolean forwarded) {
      this.forwarded = forwarded;
   }

   @Override
   public ValueMatcher getValueMatcher() {
      return ValueMatcher.MATCH_ALWAYS;
   }

   @Override
   public void setValueMatcher(ValueMatcher valueMatcher) {
      // No-op
   }

   @Override
   public boolean isReturnValueExpected() {
      return true;
   }

   @Override
   public boolean isSuccessful() {
      return true;
   }

   @Override
   public boolean isConditional() {
      return false;
   }

   @Override
   public void fail() {
      throw new UnsupportedOperationException();
   }

   @Override
   public long getFlagsBitSet() {
      return flags;
   }

   @Override
   public void setFlagsBitSet(long bitSet) {
      this.flags = bitSet;
   }

   @Override
   public Params getParams() {
      return params;
   }

   public void setParams(Params params) {
      this.params = params;
   }

   @Override
   public Object getKeyLockOwner() {
      return commandInvocationId;
   }

   @Override
   public CommandInvocationId getCommandInvocationId() {
      return commandInvocationId;
   }

   @Override
   public boolean hasZeroLockAcquisition() {
      return hasAnyFlag(FlagBitSets.ZERO_LOCK_ACQUISITION_TIMEOUT);
   }

   @Override
   public boolean hasSkipLocking() {
      return hasAnyFlag(FlagBitSets.SKIP_LOCKING);
   }

   @Override
   public DataConversion getKeyDataConversion() {
      return keyDataConversion;
   }

   @Override
   public DataConversion getValueDataConversion() {
      return valueDataConversion;
   }

   @Override
   public PrivateMetadata getInternalMetadata(Object key) {
      return internalMetadataMap.get(key);
   }

   @Override
   public void setInternalMetadata(Object key, PrivateMetadata internalMetadata) {
      this.internalMetadataMap.put(key, internalMetadata);
   }
}
