package commentserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import javax.xml.transform.TransformerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * If a presentation implementation is written as a flash application, this thread returns a policy file when a client connects to the server.
 * Afterwards, it receives a movie id from the client and keep running as long as it has the connection with the client.
 * While running, it accepts comments from the client, passing them to those who are watching the same video.
 * 
 * @author Hiroki Sawano
 * @see Socket
 * @see Logger
 * @see BufferedReader
 * @see InputStreamReader
 * @see PrintWriter
 * @see OutputStreamWriter
 * @see CommentList
 * @see Environment
 * @see UserManagement
 * @since 1.0
 */
public class CommentServerThread extends Thread {

    private static Logger logger = LogManager.getLogger();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int userId = -1;
    private CommentList commentList;

    private String receivedMessage = "";
    private String movieId;
    private String ipAddress;

    /**
     * Constructor
     *
     * @param socket
     * @throws IOException
     */
    public CommentServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF8"));
        this.out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF8"), true);
        this.start();
    }

    /**
     * 
     * @return out 
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * 
     * @return userId 
     */
    public int getUserId() {
        return userId;
    }

    /**
     * 
     * @return ipAddress 
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Communicates with clients, receiving a movie id and comment, sending the
     * comment to those whose movie ids are the same.
     * 
     * @see BufferedReader
     * @see PrintWriter
     * @see Environment
     * @see UserManagement
     * @see Socket
     */
    @Override
    public void run() {
        ipAddress = this.socket.getInetAddress().getHostAddress();

        logger.info("{} connected", ipAddress);

        try {
            int c = in.read();
            while (c != '\0' && c >= 0) {
                receivedMessage += (char) c;
                c = in.read();

                if (c < 0) {
                    break;
                }
            }
            if (c >= 0) {
                // if a client requires the policy file
                if (receivedMessage.startsWith("<policy-file-request/>")) {
                    logger.info("{} required the policy file", ipAddress);

                    // send the policy file in xml
                    out.println("<?xml version=\"1.0\"?><!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">"
                            + "<cross-domain-policy>"
                            + "<site-control permitted-cross-domain-policies=\"master-only\"/>"
                            + "<allow-access-from domain=" + '"' + "*" + '"' + " to-ports=" + '"' + "*" + '"' + "/>"
                            + "</cross-domain-policy>");
                } else {
                    movieId = receivedMessage;

                    logger.info("{} accessed {}", ipAddress, movieId);

                    commentList = new CommentList(movieId);

                    // add a new user
                    userId = Environment.um.addUser(this, movieId, commentList.getCommentList());

                    // can't accept a user anymore
                    if (userId == UserManagement.getMaxNumUser()) {
                        throw new RegisterException();
                    }

                    logger.info("{} 's user ID is {}", ipAddress, userId);

                    commentList.addElapsedTime(new Long(commentList.getElapsedTime()).toString());
                    out.print(commentList + "\0");
                    out.flush();

                    logger.info("{} (user ID:{}) received a comment list (elapsed time was {})", ipAddress, userId, commentList.getElapsedTime());

                    commentList.removeElapsedTime();

                    // receive comments
                    while (true) {
                        receivedMessage = "";

                        c = in.read();
                        while (c != '\0' && c >= 0) {
                            receivedMessage += (char) c;
                            c = in.read();

                            if (c < 0) {
                                break;
                            }
                        }
                        if (c < 0) {
                            break;
                        }

                        logger.info("{} (user ID:{}, movie ID:{}) sent the following comment : {}", ipAddress, userId, movieId, receivedMessage);

                        // distribute the received comment to users who are watching the same movie
                        Environment.um.sendComment(movieId, CommentList.createSendData(receivedMessage));
                        commentList.addComment(receivedMessage);
                    }
                }
            }
        } catch (RegisterException e) {
            logger.error("{} (user ID:{}) exceeded the maximum number of users({})", ipAddress, userId, UserManagement.getMaxNumUser());

            out.print("Net Congestion\0");
            out.flush();
        } catch (Exception e) {
            logger.error("exception");
        } finally {
            logger.info("{} (user ID:{}) disconnected", ipAddress, userId);

            try {
                if (movieId != null) {
                    // the last user renews the comment file
                    if (UserManagement.activeUsers(movieId) == 1) {
                        commentList.writeCommentListToFile();
                    }
                }
                if (userId != -1) {
                    Environment.um.removeUser(userId);
                }
                out.close();
                in.close();
                socket.close();
            } catch (TransformerException | IOException ex) {
                logger.error("exception");
            }
        }
    }
}
