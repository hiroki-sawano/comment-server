package commentserver;

import java.net.*;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This application is a socket server that communicates with clients whose purpose is to allow users to comments on videos they are watching. 
 * The server performs the following operations in order : 
 * 1. Reading the configuration (refer to config.properties)
 * 2. Accepting clients' requests
 * 3. Receiving a video id
 * 4. Sending a comment to users who are watching the same video every time the server receives requests from clients
 * 
 * @author Hiroki Sawano
 * @see ServerSocket
 * @see Socket
 * @see Logger
 * @see Config
 * @see Environment
 * @see UserManagement
 * @see CommentServerThread
 * @since 1.0
 */
public class CommentServer {

    private static ServerSocket commentServerSocket;
    private static Socket client;
    private static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            Config config = Config.getInstance();
            config.init();

            commentServerSocket = new ServerSocket(config.getPort());
            
            logger.info("Comment server started");
            
            while (true) {
                client = commentServerSocket.accept();
                CommentServerThread thread = new CommentServerThread(client);
            }
        } catch (IOException e) {
            logger.error("io error");
        } finally {
            try {
                commentServerSocket.close();
            } catch (IOException e) {
                logger.error("io error");
            }
        }
    }
}
