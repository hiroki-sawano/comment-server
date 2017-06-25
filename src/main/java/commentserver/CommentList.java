package commentserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class CommentList {

    private static Logger logger = LogManager.getLogger();

    // where comment list is saved
    private final String commentListDir;
    // comment list file name
    private String fileName = "";
    // movie id corresponding to comment list
    private String movieId = "";
    // comment list xml
    private Document commentList = null;
    // elapsed time when users accesse a movie 
    private long elapsedTime = 0;

    /**
     * Constructor<br>
     * set a movie id and comment list file name, get the comment list ready to
     * be used
     *
     * @param movieId
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public CommentList(String movieId) throws IOException, ParserConfigurationException, SAXException {
        Config config = Config.getInstance();
        commentListDir = config.getCommentListDir();
        setMovieId(movieId);
        setFileName(getMovieId() + ".xml");
        prepareCommentList();
    }

    /**
     *
     * @param commentList
     */
    private void setCommentList(Document commentList) {
        this.commentList = commentList;
    }

    /**
     *
     * @return commentList
     */
    public Document getCommentList() {
        return this.commentList;
    }

    /**
     *
     * @param movieId
     */
    private void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    /**
     *
     * @return movieId
     */
    public String getMovieId() {
        return this.movieId;
    }

    /**
     *
     * @param fileName
     */
    private void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * @return fileName
     */
    public String getFileName() {
        return this.fileName;
    }

    public void setElapsedTime() {
        long createdTime = 0;

        Element root = getCommentList().getDocumentElement();
        NodeList list = root.getElementsByTagName("createdTime");
        Element element = (Element) list.item(0);
        createdTime = new Long(element.getFirstChild().getNodeValue());

        this.elapsedTime = new Date().getTime() - createdTime;
    }

    /**
     *
     * @return elapsedTime
     */
    public long getElapsedTime() {
        return this.elapsedTime;
    }

    /**
     * setting a comment list<br>
     * if a thread that is referring to the same comment file exists, share it,
     * otherwise open it<br>
     * in the case the file doesn't exist, create a new file consisting its
     * created time
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void prepareCommentList() throws IOException, ParserConfigurationException, SAXException {
        // see if other users have already opened the comment list file
        setCommentList(UserManagement.checkFile(getMovieId()));

        // if no one opened the comment list
        if (getCommentList() == null) {
            // publisher accesses if this is live streaming
            // read the comment file
            File fileObject = new File(commentListDir + "/" + getFileName());

            // if it doesn't exist, create a new file
            if (!fileObject.exists()) {
                fileObject.createNewFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(commentListDir + "/" + getFileName()));
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><createdTime>"
                        + new Date().getTime() + "</createdTime></root>");

                writer.close();
                logger.info("Created the comment list : {}", commentListDir + "/" + getFileName());
            }else{
                logger.info("Opened the comment list : {}", commentListDir + "/" + getFileName());
            }
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            setCommentList(docBuilder.parse(fileObject));
        } else {
            // subscriber accesses if this is live streaming
        }

        setElapsedTime();
    }

    /**
     * return comment list in xml format
     */
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();

        TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = tfactory.newTransformer();
            transformer.transform(new DOMSource(getCommentList()), new StreamResult(sw));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return sw.toString();
    }

    public void addElapsedTime(String elapsedTime) {
        // <root>
        Element e_root = getCommentList().getDocumentElement();
        // 	<elapsedTime>
        Element e_elapsedTime = getCommentList().createElement("elapsedTime");
        e_root.appendChild(e_elapsedTime);
        Text t_elapsedTime = getCommentList().createTextNode(elapsedTime);
        e_elapsedTime.appendChild(t_elapsedTime);
    }

    public void removeElapsedTime() {
        Element root = getCommentList().getDocumentElement();
        NodeList list = root.getElementsByTagName("elapsedTime");
        root.removeChild(list.item(0));
    }

    /**
     * append a received comment(csv) to a dom object<br>
     * message example:<br>
     * user123,STUDENT,subscribe,12.34,Hello,SHARE,live,ALL,,30,100
     *
     * @param addMessage
     */
    public void addComment(String addMessage) {
        String[] array = addMessage.split(",");
        // user tag
        String user_id = array[0];
        String role = array[1];
        String from = array[2];
        // message tag
        String time = array[3];
        String value = array[4];
        String type = array[5];
        String target = array[6];
        String scope = array[7];
        String whisper = array[8];
        // style tag
        String fontsize = array[9];
        String place = array[10];

        // <root>
        Element e_root = getCommentList().getDocumentElement();
        // 	<comment>
        Element e_comment = getCommentList().createElement("comment");
        e_root.appendChild(e_comment);
        // 		<user>
        Element e_user = getCommentList().createElement("user");
        e_comment.appendChild(e_user);
        // 			<user_id>
        Element e_user_id = getCommentList().createElement("user_id");
        e_user.appendChild(e_user_id);
        Text t_user_id = getCommentList().createTextNode(user_id);
        e_user_id.appendChild(t_user_id);
        //			</user_id>
        //			<role>
        Element e_role = getCommentList().createElement("role");
        e_user.appendChild(e_role);
        Text t_role = getCommentList().createTextNode(role);
        e_role.appendChild(t_role);
        //			</role>
        //			<from>
        Element e_from = getCommentList().createElement("from");
        e_user.appendChild(e_from);
        Text t_from = getCommentList().createTextNode(from);
        e_from.appendChild(t_from);
        //			</from>
        // 		</user>
        // 		<message>
        Element e_message = getCommentList().createElement("message");
        e_comment.appendChild(e_message);
        // 			<time>
        Element e_time = getCommentList().createElement("time");
        e_message.appendChild(e_time);
        Text t_time = getCommentList().createTextNode(time);
        e_time.appendChild(t_time);
        // 			</time>
        // 			<value>
        Element e_value = getCommentList().createElement("value");
        e_message.appendChild(e_value);
        Text t_value = getCommentList().createTextNode(value);
        e_value.appendChild(t_value);
        // 			</value>
        // 			<type>
        Element e_type = getCommentList().createElement("type");
        e_message.appendChild(e_type);
        Text t_type = getCommentList().createTextNode(type);
        e_type.appendChild(t_type);
        // 			</type>
        // 			<target>
        Element e_target = getCommentList().createElement("target");
        e_message.appendChild(e_target);
        Text t_target = getCommentList().createTextNode(target);
        e_target.appendChild(t_target);
        // 			</target>
        // 			<scope>
        Element e_scope = getCommentList().createElement("scope");
        e_message.appendChild(e_scope);
        Text t_scope = getCommentList().createTextNode(scope);
        e_scope.appendChild(t_scope);
        // 			</scope>
        // 			<whisper>
        Element e_whisper = getCommentList().createElement("whisper");
        e_message.appendChild(e_whisper);
        Text t_whisper = getCommentList().createTextNode(whisper);
        e_whisper.appendChild(t_whisper);
        // 			</whisper>
        // 		</message>
        // 		<style>
        Element e_style = getCommentList().createElement("style");
        e_comment.appendChild(e_style);
        // 			<fontsize>
        Element e_fontsize = getCommentList().createElement("fontsize");
        e_style.appendChild(e_fontsize);
        Text t_fontsize = getCommentList().createTextNode(fontsize);
        e_fontsize.appendChild(t_fontsize);
        // 			</fontsize>
        // 			<place>
        Element e_place = getCommentList().createElement("place");
        e_style.appendChild(e_place);
        Text t_place = getCommentList().createTextNode(place);
        e_place.appendChild(t_place);
        // 			</place>
        // 		</style>
    }

    /**
     * translate a csv message into xml one<br>
     * message example:<br>
     * user123,STUDENT,subscribe,12.34,Hello,SHARE,live,ALL,,30,100
     *
     * @param sendMessage
     * @return sendData
     */
    static public String createSendData(String sendMessage) {
        String sendData = null;

        String[] array = sendMessage.split(",");
        // user tag
        String user_id = array[0];
        String role = array[1];
        String from = array[2];
        // message tag
        String time = array[3];
        String value = array[4];
        String type = array[5];
        String target = array[6];
        String scope = array[7];
        String whisper = array[8];
        // style tag
        String fontsize = array[9];
        String place = array[10];

        sendData = "<data>"
                + "<comment>"
                + "<user>"
                + "<user_id>" + user_id + "</user_id>"
                + "<role>" + role + "</role>"
                + "<from>" + from + "</from>"
                + "</user>"
                + "<message>"
                + "<time>" + time + "</time>"
                + "<value>" + value + "</value>"
                + "<type>" + type + "</type>"
                + "<target>" + target + "</target>"
                + "<scope>" + scope + "</scope>"
                + "<whisper>" + whisper + "</whisper>"
                + "</message>"
                + "<style>"
                + "<fontsize>" + fontsize + "</fontsize>"
                + "<place>" + place + "</place>"
                + "</style>"
                + "</comment>"
                + "</data>";

        return sendData;
    }

    /**
     * write a comment list to a file<br>
     * this method is called when the last user who has been reading the comment list ends the viewing
     *
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public void writeCommentListToFile() throws TransformerConfigurationException, TransformerException {
        File outfile = new File(commentListDir + "/" + getFileName());
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer = tfactory.newTransformer();
        transformer.transform(new DOMSource(getCommentList()), new StreamResult(outfile));
        logger.info("Wrote comments in memory to the file : {}", commentListDir + "/" + getFileName());
    }
}
