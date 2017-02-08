package pt.tecnico.mydrive.service;

import org.jdom2.Document;

import pt.tecnico.mydrive.exception.ImportDocumentException;

public class ImportMyDriveService extends MyDriveService {
    private final Document doc;

    public ImportMyDriveService(Document doc) {
        this.doc = doc;
    }

    @Override
    protected void dispatch() throws ImportDocumentException {
        getFileSystem().xmlImport(doc);
    }
}
