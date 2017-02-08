package pt.tecnico.mydrive.presentation;

import java.io.IOException;
import java.io.File;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.ListIterator;

import pt.tecnico.mydrive.service.*;

public class MyDrive extends Shell {

    private long token = -1; // Invalid token.
    private long guestToken = -1; // Invalid token.

    /* package */ long getToken() { return this.token; }
    /* package */ void setToken(final long token) { this.token = token; }

    private String username = "";

    void setUsername(final String username) { this.username = username; }
    String getUsername() { return this.username; }


    private Map<String, ArrayList<Long>> loggedUsers = new HashMap<String, ArrayList<Long>>();

    void setToken(final String username, final long token) {  //adds username - token association
        ArrayList newArray = new ArrayList();
        newArray.add(token);

        if (loggedUsers.putIfAbsent(username, newArray) != null) //null -> new username key
            loggedUsers.get(username).add(token);  //not null -> existing username key
    }

    long getToken(final String username) {   //gets a token associated with username
        ArrayList<Long> tokens = loggedUsers.get(username);

        if (tokens == null) return -1; //username without login

        if (username.equals(getUsername()) ) {
            int i = 0;
            while(tokens.get(i++) != getToken()) {}

            return tokens.get(i%tokens.size());
        }

        return tokens.get(0);
    }

    void removeToken(final long token) {  //removes a token AND a username if it's his last token
        for (Map.Entry<String, ArrayList<Long>> e : loggedUsers.entrySet()) {
            ListIterator<Long> i = e.getValue().listIterator();
            while (i.hasNext()) {

                if (i.next() == token) {
                    i.remove(); //deletes token

                    if (e.getValue().size() == 0) //if username has no tokens, is removed
                        loggedUsers.remove(e.getKey());

                    return;
                }
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        MyDrive sh = new MyDrive();

        for (String s: args) {
            sh.xmlScan(new File(s));
        }

        sh.execute();
    }

    public MyDrive() {
        super("MyDrive");

        /* Add commands here */
        new Cwd(this);
        new Execute(this);
        new Environment(this);
        new Key(this);
        new List(this);
        LoginUser logUs = new LoginUser(this);
        new Write(this);
        new Read(this);
        new Create(this);

        /* Login Guest user */
        String[] logArgs = {"nobody"};
        logUs.execute(logArgs);
        this.guestToken = this.getToken();

        /* Start at the file system root directory */
        final ChangeDirectoryService cwds = new ChangeDirectoryService(getToken(), "/");
        cwds.execute();
    }

    @Override
    public void quit() {
        ArrayList<Long> tokens = loggedUsers.get("nobody");

        for (long token: tokens)
            new LogoutUserService(token).execute();

        super.quit();
    }

    public void xmlScan(final File file) {
        final SAXBuilder builder = new SAXBuilder();
        try {
            final Document doc = (Document) builder.build(file);
            new ImportMyDriveService(doc).execute();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

}
