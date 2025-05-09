package org.infinispan.server.resp.commands.list;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.infinispan.multimap.impl.EmbeddedMultimapListCache;
import org.infinispan.server.resp.AclCategory;
import org.infinispan.server.resp.Resp3Handler;
import org.infinispan.server.resp.RespCommand;
import org.infinispan.server.resp.RespRequestHandler;
import org.infinispan.server.resp.commands.Resp3Command;
import org.infinispan.server.resp.serialization.ResponseWriter;

import io.netty.channel.ChannelHandlerContext;

/**
 * LINSERT
 *
 * @see <a href="https://redis.io/commands/linsert/">LINSERT</a>
 * @since 15.0
 */
public class LINSERT extends RespCommand implements Resp3Command {

   public static final String BEFORE = "BEFORE";
   public static final String AFTER = "AFTER";

   public LINSERT() {
      super(5, 1, 1, 1, AclCategory.WRITE.mask() | AclCategory.LIST.mask() | AclCategory.SLOW.mask());
   }

   @Override
   public CompletionStage<RespRequestHandler> perform(Resp3Handler handler,
                                                      ChannelHandlerContext ctx,
                                                      List<byte[]> arguments) {

      byte[] key = arguments.get(0);
      String position = new String(arguments.get(1)).toUpperCase();
      boolean isBefore = position.equals(BEFORE);
      if (!isBefore && !position.equals(AFTER)) {
         handler.writer().syntaxError();
         return handler.myStage();
      }

      byte[] pivot = arguments.get(2);
      byte[] element = arguments.get(3);

      EmbeddedMultimapListCache<byte[], byte[]> listMultimap = handler.getListMultimap();
      return handler.stageToReturn(listMultimap.insert(key, isBefore, pivot, element), ctx, ResponseWriter.INTEGER);
   }
}
