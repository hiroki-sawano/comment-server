package commentserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Config {

    private static Config instance = null;
    
    private int port;
    private int maxNumUser;
    private String commentListDir;
    
    private static Logger logger = LogManager.getLogger();

    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public void init(){
        Properties properties = new Properties();
        String file = "src/main/resources/config.properties";
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxNumUser() {
        return maxNumUser;
    }

    public void setMaxNumUser(int maxNumUser) {
        this.maxNumUser = maxNumUser;
    }

    public String getCommentListDir() {
        return commentListDir;
    }

    public void setCommentListDir(String commentListDir) {
        this.commentListDir = commentListDir;
    }
}
