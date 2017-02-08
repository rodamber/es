package pt.tecnico.mydrive.system;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.mydrive.service.AbstractServiceTest ;
import pt.tecnico.mydrive.presentation.*;

public class SystemTest extends AbstractServiceTest {

    private MyDrive sh;

    protected void populate() {
        sh = new MyDrive();
    }

    @Test
    public void cwdCommandsSuccess() {
        new Cwd(sh).dispatch(new String[] { } );
        new Cwd(sh).dispatch(new String[] { "home" } );
        new Cwd(sh).dispatch(new String[] { ".." } );
    }
    
    @Test
    public void envCommandsSuccess() {
        new Environment(sh).dispatch(new String[] { } );
        new Environment(sh).dispatch(new String[] { "var", "value"} );
        new Environment(sh).dispatch(new String[] { "var"} );
        new Environment(sh).dispatch(new String[] { } ); // check if it prints with no problems
    }

    @Test
    public void executeAppSuccess() {
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
        new Create(sh).dispatch(new String[] { "myApp", "App","pt.tecnico.mydrive.presentation.Hello" } );
        new Execute(sh).dispatch(new String[] { "myApp"} );
        new Create(sh).dispatch(new String[] { "otherApp", "App","pt.tecnico.mydrive.presentation.Hello.greet" } );
        new Execute(sh).dispatch(new String[] { "otherApp", "ola"} );
        new Execute(sh).dispatch(new String[] { "otherApp", "ola","app"} );
    }
    
    @Test
    public void keyCommandsSuccess() {
        new Key(sh).dispatch(new String[] { } );
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
        new Key(sh).dispatch(new String[] { } );
        new Key(sh).dispatch(new String[] { "root" } );
        new Key(sh).dispatch(new String[] { "nobody" } );
        new Key(sh).dispatch(new String[] { "root" } );
    }
        
    @Test
    public void listCommandsSuccess() {
        new List(sh).dispatch(new String[] { } );
    }
    
    @Test
    public void loginCommandsSuccess() {
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
    }
    
    @Test
    public void writeCommandsSuccess() {
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
        new Create(sh).dispatch(new String[] { "notas", "PlainFile","texto_importante" } );
        new Write(sh).dispatch(new String[] { "notas", "apagado" } );
    }
    
    @Test
    public void readCommandsSuccess() {
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
        new Create(sh).dispatch(new String[] { "notas", "PlainFile","texto_importante" } );
        new Read(sh).dispatch(new String[] { "notas" } );
    }
    
    @Test
    public void createCommandsSuccess() {
        new LoginUser(sh).dispatch(new String[] { "root", "***"});
        new Create(sh).dispatch(new String[] { "notas", "PlainFile" } );
        new Create(sh).dispatch(new String[] { "outrasNotas", "PlainFile","texto_importante" } );
        new Create(sh).dispatch(new String[] { "myApp", "App","pt.tecnico.mydrive.presentation.Hello" } );
    }

}