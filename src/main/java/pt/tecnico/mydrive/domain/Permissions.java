package pt.tecnico.mydrive.domain;

import java.lang.String;
import pt.tecnico.mydrive.exception.*;

public class Permissions extends Permissions_Base {

    public Permissions() { super(); }

    public Permissions(String permissions) {
        setPermissions(permissions);
    }

    public Permissions(Permissions p) {
        this(p.toString());
    }

    private void setPermissions(String permissions) {
        super.setOwnerReadPermission(permissions.charAt(0) == 'r');
        super.setOwnerWritePermission(permissions.charAt(1) == 'w');
        super.setOwnerExecutePermission(permissions.charAt(2) == 'x');
        super.setOwnerDeletePermission(permissions.charAt(3) == 'd');
        super.setOthersReadPermission(permissions.charAt(4) == 'r');
        super.setOthersWritePermission(permissions.charAt(5) == 'w');
        super.setOthersExecutePermission(permissions.charAt(6) == 'x');
        super.setOthersDeletePermission(permissions.charAt(7) == 'd');
    }

    public void setPermissions(User user, String permissions) {
    		if (canChangePermission(user))
          	setPermissions(permissions);
    }

    @Override
    public void setOwnerReadPermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwnerWritePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwnerExecutePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwnerDeletePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOthersReadPermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOthersWritePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOthersExecutePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOthersDeletePermission(boolean bool) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void setOwnerReadPermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOwnerReadPermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOwnerWritePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOwnerWritePermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOwnerExecutePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOwnerExecutePermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOwnerDeletePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOwnerDeletePermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOthersReadPermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOthersReadPermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOthersWritePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOthersWritePermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOthersExecutePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOthersExecutePermission(bool);
        else
            throw new AccessDeniedException();
    }

    public void setOthersDeletePermission(User user, boolean bool) throws AccessDeniedException {
    		if (canChangePermission(user))
          	super.setOthersDeletePermission(bool);
        else
            throw new AccessDeniedException();
    }

    @Override
    public String toString() {
        String permissions = "";

        if (getOwnerReadPermission())      permissions += "r"; else  permissions += "-";
        if (getOwnerWritePermission())     permissions += "w"; else  permissions += "-";
        if (getOwnerExecutePermission())   permissions += "x"; else  permissions += "-";
        if (getOwnerDeletePermission())    permissions += "d"; else  permissions += "-";
        if (getOthersReadPermission())     permissions += "r"; else  permissions += "-";
        if (getOthersWritePermission())    permissions += "w"; else  permissions += "-";
        if (getOthersExecutePermission())  permissions += "x"; else  permissions += "-";
        if (getOthersDeletePermission())   permissions += "d"; else  permissions += "-";

        return permissions;
    }

    private boolean canChangePermission(RootUser root) {
        return true;
    }

    private boolean canChangePermission(User changer) {
    		if (getUser() != null) // Then umask
            return getUser() == changer;
        return getFile().getOwner() == changer;
  	}

}
