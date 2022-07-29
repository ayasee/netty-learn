package netty.demo.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class EchoClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("用法：java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new EchoClientHandler());

            ChannelFuture f = b.connect(hostName, portNumber).sync();

            Channel channel = f.channel();
            ByteBuffer writeBuffer = ByteBuffer.allocate(32);
            try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
                String userInput;
                while ((userInput = stdIn.readLine()) != null) {
                    writeBuffer.put(userInput.getBytes());
                    writeBuffer.flip();
                    writeBuffer.rewind();

                    ByteBuf buf = Unpooled.copiedBuffer(writeBuffer);

                    channel.writeAndFlush(buf);

                    writeBuffer.clear();
                }
            } catch (UnknownHostException e) {
                System.err.println("不明主机，主机名为：" + hostName);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("不能从主机中获取I/O，主机名为："+ hostName);
                System.exit(1);
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
