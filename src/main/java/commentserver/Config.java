package commentserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This singleton class reads <i>config.properties</i> and 
 * contains the user-defined constant variables.
 * 
 * @author Hiroki Sawano
 * @see Logger
 * @see Properties
 * @see InputStream
 * @since 1.0
 */
public class Config {

    private static Config instance = null;
    
    private int port;
    private int maxNumUser;
    private String commentListDir;
    
    private static Logger logger = LogManager.getLogger();

    /**
     * Generates only one instance.
     * 
     * @return Config object
     */
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Initializes instance fields according to <i>config.properties</i>.
     */
    public void init(){
        Properties properties = new Properties();
        //String file = "src/main/resources/config.properties";
        InputStream inputStream;
        try {
            //inputStream = new FileInputStream(file));
            inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            
            properties.load(inputStream);
            inputStream.close();

            port = Integer.parseInt(properties.getProperty("port_num"));
            maxNumUser = Integer.parseInt(properties.getProperty("max_num_user"));
            commentListDir = properties.getProperty("comment_list_dir");
            
            logger.info("port_num : {} maxNumUser : {} commentListDir : {}", port, maxNumUser, commentListDir);
        } catch (FileNotFoundException ex) {
            logger.error("Could't find the config file");
        } catch (IOException ex) {
            logger.error("Could't read the config file");
        }
    }

    /**
     * @return a port number this comment server uses.
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the maximum number of users the server can accept at the same time.
     */
    public int getMaxNumUser() {
        return maxNumUser;
    }

    /**
     * @return a place where comment lists are saved.
     */
    public String getCommentListDir() {
        return commentListDir;
    }
}
