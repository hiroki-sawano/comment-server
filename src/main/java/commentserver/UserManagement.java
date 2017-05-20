
import commentserver.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * User management class
 *
 * @author hiroki-sawano
 *
 */
public class UserManagement {
    
    // the maximum number of users
    private static int maxNumUser;
    // a user management area 
    public static User user[];
    private static Logger logger = LogManager.getLogger();

    /**
     * Constructor<br>
     * create a user management area that can accommodate maxNumUser users
     */
    public UserManagement() {
        Config config = Config.getInstance();
        maxNumUser = config.getMaxNumUser();
        user = new User[maxNumUser];
        
        for (int i = 0; i < maxNumUser; i++) {
            user[i] = new User();
        }
    }

    public static int getMaxNumUser() {
        return maxNumUser;
    }

    /**
     * add a user<br>
     * return its user id after setting a thread object, movie id and comment
     * list
     *
     * @param thread
     * @param movieId
     * @param commentList
     * @return i
     */
    synchronized public static int addUser(CommentServerThread thread, String movieId, Document commentList) {
        int i;
        for (i = 0; i < maxNumUser; i++) {
            if (!user[i].isUsed()) {
                user[i].setIsUsed(true);
                user[i].setThread(thread);
                user[i].setMovieId(movieId);
                user[i].setCommentList(commentList);
                break;
            }
        }
        return i;
    }

    /**
     * delete a user
     *
     * @param id
     */
    synchronized void removeUser(int id) {
        user[id].setThread(null);
        user[id].setIsUsed(false);
        user[id].setMovieId(null);
        user[id].setCommentList(null);
    }

    /**
     * send a comment to clients<br>
     * the comment is sent to those who are watching the same movie.
     *
     * @param movieId
     * @param comment
     */
    synchronized void sendComment(String movieId, String comment) {
        for (int i = 0; i < maxNumUser; i++) {
            if (user[i].isUsed() && user[i].getMovieId().equals(movieId)) {
                user[i].getThread().getOut().print(comment + '\0');
                user[i].getThread().getOut().flush();
                logger.info("{} (user ID:{}) received", user[i].getThread().getIpAddress(), user[i].getThread().getUserId());
            }
        }
    }

    /**
     * return how many users are watching the movie whose id is the param
     * 'movieId'
     *
     * @param movieId
     * @return numUsers
     */
    synchronized static int activeUsers(String movieId) {
        int numUsers = 0;
        for (int i = 0; i < maxNumUser; i++) {
            if (user[i].isUsed() && user[i].getMovieId().equals(movieId)) {
                ++numUsers;
            }
        }
        return numUsers;
    }

    /**
     * if users watching a movie exist, return its comment list
     *
     * @param movieId
     * @return commentList
     */
    synchronized public static Document checkFile(String movieId) {
        for (int i = 0; i < maxNumUser; i++) {
            if (user[i].isUsed()) {
                if (user[i].getMovieId().equals(movieId)) {
                    return user[i].getCommentList();
                }
            }
        }
        return null;
    }
}
