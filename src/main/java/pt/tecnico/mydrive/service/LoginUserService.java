package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.exception.*;

public class LoginUserService extends MyDriveService {
	private String _username;
	private String _password;
	private long _token;
	
	public LoginUserService(String username, String password){
		_username = username;
		_password = password;
	}
	
	@Override
	public final void dispatch() throws WrongPasswordException {
	    FileSystem fs = MyDriveService.getFileSystem();
	    
	    _token = fs.login(_username, _password);
	}
	
	public long result(){
		return _token;
	}
}