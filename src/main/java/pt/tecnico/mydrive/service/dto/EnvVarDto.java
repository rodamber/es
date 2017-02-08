package pt.tecnico.mydrive.service.dto;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class EnvVarDto implements Comparable<EnvVarDto> {

    private String name;
    private String value;

    public EnvVarDto(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public EnvVarDto(EnvVar ev) {
        this(ev.getName(), ev.getValue());
    }

    public final String getName() {
        return this.name;
    }

    public final String getValue() {
        return this.value;
    }

    @Override
    public int compareTo(EnvVarDto other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }
}
