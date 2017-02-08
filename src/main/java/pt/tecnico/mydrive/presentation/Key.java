package pt.tecnico.mydrive.presentation;

public class Key extends MdCommand {

    public Key(MyDrive md) {
        super(md, "token", "token [username] : prints the active user's username and token; " +
                                            "if username is given, changes the active user");
    }

    @Override
    public void dispatch(String[] args) throws RuntimeException {
    	if (args.length > 1)
    	    throw new RuntimeException("USAGE: " + name() + " [<username>]");

        final MyDrive md = (MyDrive) shell();

        if (args.length == 1) { //if username is given
            long token = md.getToken(args[0]);

            if (token != -1) { //-1 -> invalid token
                md.setToken(token);
                md.setUsername(args[0]);
            }
            else
                md.println("User " + args[0] + " is not logged in");
        }

        if (md.getToken() == -1)
            md.println("No user in session");
        else
            md.println("username: " + md.getUsername() + "    token: " + md.getToken());
    }

}
