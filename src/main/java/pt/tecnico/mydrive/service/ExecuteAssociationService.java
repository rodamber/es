package pt.tecnico.mydrive.service;

public class ExecuteAssociationService extends MyDriveService {

    String file;
    long token;
    String ret;
    
    public ExecuteAssociationService(long token, String file) {
    	this.file = file;
    	this.token = token;
    	this.ret = null;
    }

    public final void dispatch() {
	// TODO: mockup example
    }
    
    public final String result() {
        return ret;
    }
}
