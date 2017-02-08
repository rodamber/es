package pt.tecnico.mydrive.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.service.*;
import pt.tecnico.mydrive.service.dto.*;
import pt.tecnico.mydrive.exception.*;

@RunWith(JMockit.class)
public class IntegrationTest extends AbstractServiceTest {

    private static final String importFile = "users.xml";

    private static final String homePath = "/home";
    private                 int homeSize = 9; //root + guest + 7 users

    private long ruToken;
    private static final String ruUsername = "root";
    private static final String ruPassword = "***";
    private static final String ruHomePath = "/home/root";
    private static final String ruApp1Name = "ruApp1";
    private static final String ruApp1Res  = "Executing " + ruApp1Name + "...";

    private long mjaToken;
    private static final String mjaUsername = "mja";
    private static final String mjaPassword = "Peyrelongue";

    private long jtbToken;
    private static final String jtbUsername         = "jtb";
    private static final String jtbPassword         = "Fernandes";
    private static final String jtbHomePath         = "/home/jtb";
    private                 int jtbHomeSize         = 4; //plain + dir + link + app
    private static final String jtbDirDocumentsName = "documents";
    private static final String jtbDirDocumentsPath = "/home/jtb/documents";
    private                 int jtbDirDocumentsSize = 0;
    private                 int jtbEnvSetSize       = 0;
    //----- INITIAL jtb STATE END
    private static final String jtbEnv1Name          = "$env1";
    private static final String jtbEnv1Content       = jtbDirDocumentsPath;
    private static final String jtbEnv2Name          = "$env2";
    private static final String jtbEnv2Content       = "./../././" + jtbDirDocumentsName;
    private static final String jtbDirectory1Name    = "directory1";
    private static final String jtbDirectory1Path    = jtbDirDocumentsPath + "/" + jtbDirectory1Name;
    private                 int jtbDirectory1Size    = 0;
    private static final String jtbPlainFile1Name    = "plainfile1";
    private static final String jtbPlainFile1Path    = jtbDirDocumentsPath + "/" + jtbPlainFile1Name;
    private static final String jtbPlainFile1Content = "this is " + jtbPlainFile1Name + "'s placeholder text";
    private static final String jtbPlainFile1Content2= "this is alternative content2";
    private static final String jtbApp1Name          = "app1";
    private static final String jtbApp1Path          = jtbDirDocumentsPath + "/" + jtbApp1Name;
    private static final String jtbApp1Content       = "pt.tecnico.mydrive.presentation.Hello";
    private static final String jtbApp2Name          = "app2";
    private static final String jtbApp2Content       = "pt.tecnico.mydrive.presentation.Hello.sum";
    private static final String[] app2Arguments      = {"41", "1"};
    private static final String jtbLink1Name         = "link1";
    private static final String jtbLink1Path         = jtbDirDocumentsPath + "/" + jtbLink1Name;
    private static final String jtbLink1Content      = jtbApp1Path;
    private static final String jtbLink1Content2     = "pt.tecnico.mydrive.presentation.Hello.bye";
    private static final String jtbLink2Name         = "link2";
    private static final String jtbLink2Content      = jtbPlainFile1Name;
    private static final String jtbLink3Name         = "link3";
    private static final String jtbLink3Content      = jtbEnv1Name + "/" + jtbPlainFile1Name;
    private static final String jtbLink4Name         = "link4";
    private static final String jtbLink4Content      = jtbDirDocumentsPath + "/" + jtbEnv2Name + "/" + jtbApp1Name;

    //----- DELETING THESE ONES
    private static final String jtbDelDirectory1Name    = "delDirectory1";
    private static final String jtbDelDirectory1Path    = jtbDirectory1Path + "/" + jtbDelDirectory1Name;
    private                 int jtbDelDirectory1Size    = 0;
    private static final String jtbDelDirectory2Name    = "delDirectory2";
    private static final String jtbDelDirectory2Path    = jtbDelDirectory1Path + "/" + jtbDelDirectory2Name;
    private                 int jtbDelDirectory2Size    = 0;
    private static final String jtbDelPlainFile1Name    = "delPlainfile1";
    private static final String jtbDelPlainFile1Content = "this is " + jtbDelPlainFile1Name + "'s placeholder text";
    private static final String jtbDelLink1Name         = "delLink1";
    private static final String jtbDelLink1Content      = jtbDirectory1Path;
    private static final String jtbDelApp1Name          = "delApp1";
    private static final String jtbDelApp1Content       = "pt.tecnico.mydrive.presentation.Hello.bye";

    protected void populate() {
        FileSystem fs = FileSystem.getInstance();
        User ru = fs.getRootUser();
        App ruA = new App(fs, ru.getHomeDirectory(), ruApp1Name, ru, "pt.tecnico.mydrive.presentation.Hello.bye");
        new FileExtension(ru, "cpp", ruA);
    }

    @Test
    public void success() throws Exception {

        ClassLoader loader = getClass().getClassLoader();
    	File file = new File(loader.getResource(importFile).getFile());
    	Document doc = (Document)new SAXBuilder().build(file);
    	new ImportMyDriveService(doc).execute();

        //testing LoginUserService
        LoginUserService lus = new LoginUserService(ruUsername, ruPassword);
        lus.execute();
        ruToken = lus.result();
        assertTrue(ruToken >= 0);

        lus = new LoginUserService(mjaUsername, mjaPassword);
        lus.execute();
        mjaToken = lus.result();
        assertTrue(mjaToken >= 0);
        assertFalse(mjaToken == ruToken);

        lus = new LoginUserService(jtbUsername, jtbPassword);
        lus.execute();
        jtbToken = lus.result();
        assertTrue(jtbToken >= 0);
        assertFalse( (jtbToken == mjaToken) || (jtbToken == ruToken) );

        //testing ChangeDirectoryService
        ChangeDirectoryService cds = new ChangeDirectoryService(jtbToken, jtbDirDocumentsName);
        cds.execute();
        assertEquals(cds.result(), jtbDirDocumentsPath);

        cds = new ChangeDirectoryService(jtbToken, ".");
        cds.execute();
        assertEquals(cds.result(), jtbDirDocumentsPath);

        cds = new ChangeDirectoryService(jtbToken, ".."); // /home/jtb
        cds.execute();
        assertEquals(cds.result(), jtbHomePath);

        cds = new ChangeDirectoryService(jtbToken, ".");
        cds.execute();
        assertEquals(cds.result(), jtbHomePath);

        cds = new ChangeDirectoryService(jtbToken, jtbDirDocumentsPath); // /home/jtb/documents
        cds.execute();
        assertEquals(cds.result(), jtbDirDocumentsPath);

        cds = new ChangeDirectoryService(jtbToken, ".");
        cds.execute();
        assertEquals(cds.result(), jtbDirDocumentsPath);

        cds = new ChangeDirectoryService(ruToken, ".."); // /home
        cds.execute();
        assertEquals(cds.result(), homePath);

        //testing ListDirectoryService
        ListDirectoryService lds = new ListDirectoryService(jtbToken, ".");
        lds.execute();
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //0

        lds = new ListDirectoryService(jtbToken, ".."); // /home/jtb
        lds.execute();
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbHomeSize,
                   lds.result().size() == jtbHomeSize); //4


        lds = new ListDirectoryService(ruToken, "."); // /home
        lds.execute();
        assertTrue("Is: " + lds.result().size() + " should be: " +  homeSize,
                   lds.result().size() == homeSize); //9

        lds = new ListDirectoryService(jtbToken, jtbDirDocumentsPath);
        lds.execute();
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                  lds.result().size() == jtbDirDocumentsSize); //0

        //testing CreateFileService (cwd: /home/jtb/documents)
        new CreateFileService(jtbToken, jtbDirectory1Name, "Directory").execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //1

        new CreateFileService(jtbToken, jtbPlainFile1Name, "PlainFile", jtbPlainFile1Content).execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //2

        new CreateFileService(jtbToken, jtbApp1Name, "App", jtbApp1Content).execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //3

        new CreateFileService(jtbToken, jtbApp2Name, "App", jtbApp2Content).execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //4

        new CreateFileService(jtbToken, jtbLink1Name, "Link", jtbLink1Content).execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //5

        new CreateFileService(jtbToken, jtbLink2Name, "Link", jtbLink2Content).execute();
        jtbDirDocumentsSize++;
        lds.execute(); //listDirectoryService in /home/jtb/documents
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirDocumentsSize,
                   lds.result().size() == jtbDirDocumentsSize); //6

        //testing EnvironmentVariableService
        AddEnvironmentVariableService aevs
           = new AddEnvironmentVariableService(jtbToken, jtbEnv1Name, jtbEnv1Content);
        aevs.execute();
        jtbEnvSetSize++;
        assertTrue("Is: " + aevs.result().size() + " should be: " +  jtbEnvSetSize,
                   aevs.result().size() == jtbEnvSetSize); //1

        aevs = new AddEnvironmentVariableService(jtbToken, jtbEnv2Name, jtbEnv2Content);
        aevs.execute();
        jtbEnvSetSize++;
        assertTrue("Is: " + aevs.result().size() + " should be: " +  jtbEnvSetSize,
                  aevs.result().size() == jtbEnvSetSize); //2

        //testing CreateFileService FILES TO BE DELETED
        cds = new ChangeDirectoryService(jtbToken, jtbDirectory1Path);
        cds.execute();
        assertEquals(cds.result(), jtbDirectory1Path);
        new CreateFileService(jtbToken, jtbDelDirectory1Name, "Directory").execute();
        jtbDirectory1Size++;
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirectory1Size,
                   lds.result().size() == jtbDirDocumentsSize); //1

        cds = new ChangeDirectoryService(jtbToken, jtbDelDirectory1Path);
        cds.execute();
        assertEquals(cds.result(), jtbDelDirectory1Path);
        new CreateFileService(jtbToken, jtbDelDirectory2Name, "Directory").execute();
        jtbDelDirectory1Size++;
        lds = new ListDirectoryService(jtbToken, jtbDelDirectory1Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1/delDirectory1
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDelDirectory1Size,
                   lds.result().size() == jtbDelDirectory1Size); //1

        new CreateFileService(jtbToken, jtbDelPlainFile1Name, "PlainFile", jtbDelPlainFile1Content).execute();
        jtbDelDirectory1Size++;
        lds = new ListDirectoryService(jtbToken, jtbDelDirectory1Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1/delDirectory1
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDelDirectory1Size,
                   lds.result().size() == jtbDelDirectory1Size); //2

        cds = new ChangeDirectoryService(jtbToken, jtbDelDirectory2Path);
        cds.execute();
        assertEquals(cds.result(), jtbDelDirectory2Path);
        new CreateFileService(jtbToken, jtbDelLink1Name, "Link", jtbDelLink1Content).execute();
        jtbDelDirectory2Size++;
        lds = new ListDirectoryService(jtbToken, jtbDelDirectory2Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1/delDirectory1/delDirectory2
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDelDirectory2Size,
                   lds.result().size() == jtbDelDirectory2Size); //1

        new CreateFileService(jtbToken, jtbDelApp1Name, "App", jtbDelApp1Content).execute();
        jtbDelDirectory2Size++;
        lds = new ListDirectoryService(jtbToken, jtbDelDirectory2Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1/delDirectory1/delDirectory2
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDelDirectory2Size,
                   lds.result().size() == jtbDelDirectory2Size); //2

        //testing CreateFileService DELETE TESTS
        new DeleteFileService(jtbToken, jtbDelApp1Name).execute();
        jtbDelDirectory2Size--;
        lds = new ListDirectoryService(jtbToken, jtbDelDirectory2Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1/delDirectory1/delDirectory2
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDelDirectory2Size,
                   lds.result().size() == jtbDelDirectory2Size); //1

        cds = new ChangeDirectoryService(jtbToken, jtbDirectory1Path);
        cds.execute();
        assertEquals(cds.result(), jtbDirectory1Path);
        new DeleteFileService(jtbToken, jtbDelDirectory1Name).execute();
        jtbDelDirectory1Size = 0;
        jtbDelDirectory2Size = 0;
        jtbDirectory1Size--;
        lds = new ListDirectoryService(jtbToken, jtbDirectory1Path);
        lds.execute(); //listDirectoryService in /home/jtb/documents/directory1
        assertTrue("Is: " + lds.result().size() + " should be: " +  jtbDirectory1Size,
                   lds.result().size() == jtbDirectory1Size); //0

        //testing executeFileService
        cds = new ChangeDirectoryService(jtbToken, jtbDirDocumentsPath);
        cds.execute();
        assertEquals(cds.result(), jtbDirDocumentsPath);
        new ExecuteFileService(jtbToken, jtbApp1Name, null);
        new ExecuteFileService(jtbToken, jtbApp2Name, app2Arguments);
        new ExecuteFileService(jtbToken, jtbLink1Name, null);

        //testing readFileService
        ReadFileService rfs = new ReadFileService(jtbToken, jtbApp1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbApp1Content);

        rfs = new ReadFileService(jtbToken, jtbPlainFile1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbPlainFile1Content);

        rfs = new ReadFileService(jtbToken, jtbLink1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbApp1Content);

        rfs = new ReadFileService(jtbToken, jtbLink2Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbPlainFile1Content);

        //testing writeFileService
        new WriteFileService(jtbToken, jtbPlainFile1Name, jtbPlainFile1Content2).execute();
        rfs = new ReadFileService(jtbToken, jtbPlainFile1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbPlainFile1Content2);

        new WriteFileService(jtbToken, jtbLink1Name, jtbLink1Content2).execute();
        rfs = new ReadFileService(jtbToken, jtbLink1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbLink1Content2);
        rfs = new ReadFileService(jtbToken, jtbApp1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbLink1Content2);

        new WriteFileService(jtbToken, jtbPlainFile1Name, jtbPlainFile1Content).execute();
        rfs = new ReadFileService(jtbToken, jtbPlainFile1Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbPlainFile1Content);

        //testing environment variables stuff (mockups)
        //this is needed because in our system you can't create a Link to file that does not exist
        cds = new ChangeDirectoryService(jtbToken, jtbDirDocumentsPath);
        cds.execute();

        new MockUp<CreateFileService>() {
            @Mock
            void dispatch() { }
        };
        new CreateFileService(jtbToken, jtbLink3Name, "Link", jtbLink3Content).execute();
        lds = new ListDirectoryService(jtbToken, jtbDirDocumentsPath);
        lds.execute(); //listDirectoryService in /home/jtb/documents

        new MockUp<CreateFileService>() {
            @Mock
            void dispatch() { }
        };
        new CreateFileService(jtbToken, jtbLink4Name, "Link", jtbLink4Content).execute();
        lds = new ListDirectoryService(jtbToken, jtbDirDocumentsPath);
        lds.execute(); //listDirectoryService in /home/jtb/documents

        new MockUp<ReadFileService>() {
            @Mock
            void dispatch() { }
            @Mock
            String result() { return jtbPlainFile1Content; }
        };
        rfs = new ReadFileService(jtbToken, jtbLink3Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbPlainFile1Content);

        new MockUp<ReadFileService>() {
            @Mock
            void dispatch() { }
            @Mock
            String result() { return jtbApp1Content; }
        };
        rfs = new ReadFileService(jtbToken, jtbLink4Name);
        rfs.execute();
        assertEquals(rfs.result(), jtbApp1Content);

        //testing ExecuteAssociationService (mockups)
        cds = new ChangeDirectoryService(ruToken, ruHomePath); // /home/root
        cds.execute();
        assertEquals(cds.result(), ruHomePath);

        new MockUp<ExecuteAssociationService>() {
            @Mock
            String result(){ return ruApp1Res; }
        };
        ExecuteAssociationService eas = new ExecuteAssociationService(ruToken, ruApp1Name);
        eas.execute();
        assertEquals(eas.result(), ruApp1Res);

        //testing LogoutUserService
        new LogoutUserService(jtbToken).execute();
    }

}
