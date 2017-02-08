package pt.tecnico.mydrive.presentation;
import pt.tecnico.mydrive.service.LoginUserService;

public class LoginUser extends MdCommand {
    
    public LoginUser(MyDrive sh) {
        super(sh, "login", "login username [password] : login user in filesystem"); 
    }
    
    @Override
    public void dispatch(String[] args) {
        
    	if (args.length < 1)
    	    throw new RuntimeException("USAGE: "+name()+" <username> [<password>]");
    	
    	final MyDrive drive = (MyDrive) shell();
    	
    	final String password;
        if(args.length > 1){
            password = args[1]; 
        }
        else{
            password = null;
        }
        
	    LoginUserService lus = new LoginUserService(args[0], password);
	    lus.execute();
	    long token = lus.result();
	    drive.setToken(args[0], token);
	    drive.setUsername(args[0]);
	    drive.setToken(token);
	    drive.println("" + token);
    	
    }
}