package pt.tecnico.mydrive.domain;

public class EnvVar extends EnvVar_Base {

    public EnvVar() {
        super();
    }

    public EnvVar(Login login, String name, String value) {
        if (login == null) {
            throw new IllegalArgumentException("login must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        setName(name);
        setValue(value);
        setLogin(login);
    }

    @Override
    public void setLogin(Login login) {
        if (login == null) {
            super.setLogin(login);
        } else {
            login.addEnvVar(this);
        }
    }

    public void remove() {
        setLogin(null);
        deleteDomainObject();
    }

}
