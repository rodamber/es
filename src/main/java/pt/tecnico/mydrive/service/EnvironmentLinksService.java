package pt.tecnico.mydrive.service;

public class EnvironmentLinksService extends MyDriveService {
    long token;
    String path;
    String operation;
    String content;
    String ret;

    public EnvironmentLinksService(long token, String path, String operation, String content) {
        this.token = token;
        this.path = path;
        this.operation = operation;
        this.content = content;
    	  this.ret = null;
    }

    public final void dispatch() {
	      // TODO: mockup example
    }

    public final String result() {
        return ret;
    }
}
