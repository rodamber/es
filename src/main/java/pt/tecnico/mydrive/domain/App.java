package pt.tecnico.mydrive.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Element;
import org.joda.time.DateTime;

import pt.tecnico.mydrive.exception.ImportDocumentException;
import pt.tecnico.mydrive.exception.AccessDeniedException;
import pt.tecnico.mydrive.exception.InvalidFileNameException;
import pt.tecnico.mydrive.exception.InvalidAppContentException;
import pt.tecnico.mydrive.exception.AbsolutePathSizeLimitExceeded;

import pt.tecnico.mydrive.utils.*;

public class App extends App_Base {

    public App(FileSystem fs, Directory parent, String name,
               User owner,    String content)
        throws AccessDeniedException, InvalidFileNameException,
               InvalidAppContentException, AbsolutePathSizeLimitExceeded
    {
        if (!isValidContent(content))
            throw new InvalidAppContentException(content);
        init(fs, parent, name, owner, content);
    }

    private boolean isValidContent(String content) {
        if (content.equals(""))
            return true;
        return content.matches("([A-Za-z0-9]+(\\.[A-Za-z0-9]+)*)");
    }

    public App(FileSystem fs, String absolutePath)
        throws InvalidFileNameException, AccessDeniedException, AbsolutePathSizeLimitExceeded
    {
        init(fs, absolutePath);
    }

    public App(FileSystem fs, Element element)
        throws ImportDocumentException, AccessDeniedException,
               InvalidFileNameException, AbsolutePathSizeLimitExceeded
    {
        init(fs, element);
        write(Utils.elementDefaultValue(element, "method", ""), fs.getRootUser());
    }
    
    @Override
    public void execute(User user, String[] args) throws AccessDeniedException {
        if (!user.hasExecutePermission(this))
            throw new AccessDeniedException();
        
        String name = read(user);
        
        try {
            if (name != null && !name.isEmpty())
                /*shell().*/run(name, args);
            else throw new Exception("Nothing to run!");
            
        } catch (Exception e) { throw new RuntimeException(""+e); }
    }
    
    public void run(String name, String[] args) 
            throws ClassNotFoundException, SecurityException, NoSuchMethodException, 
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        
        Class<?> cls;
        Method meth;
        try { // name is a class: call main()
            cls = Class.forName(name);
            meth = cls.getMethod("main", String[].class);
        } catch (ClassNotFoundException cnfe) { // name is a method
            int pos;
            if ((pos = name.lastIndexOf('.')) < 0) throw cnfe;
            cls = Class.forName(name.substring(0, pos));
            meth = cls.getMethod(name.substring(pos+1), String[].class);
        }
        meth.invoke(null, (Object)args); // static method (ignore return)
        
    }

    public Element xmlExport() {
        Element element = super.xmlExport();
        element.setName("app");
        element.getChild("contents").setName("method");
        return element;
    }

}
