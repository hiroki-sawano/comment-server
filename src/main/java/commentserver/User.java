package commentserver;

import org.w3c.dom.Document;

/**
 * User class
 *
 * @author Hiroki Sawano
 *
 */
public class User {

    private boolean isUsed = false;
    private CommentServerThread thread = null;
    private String movieId = null;
    private Document commentList = null;

    public void setIsUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public boolean isUsed() {
        return this.isUsed;
    }

    public void setThread(CommentServerThread thread) {
        this.thread = thread;
    }

    public CommentServerThread getThread() {
        return this.thread;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieId() {
        return this.movieId;
    }

    public void setCommentList(Document commentList) {
        this.commentList = commentList;
    }

    public Document getCommentList() {
        return this.commentList;
    }
}
