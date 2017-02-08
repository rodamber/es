package pt.tecnico.mydrive.presentation;

import java.util.Set;
import java.util.function.Predicate;
import java.util.Optional;
import java.util.NoSuchElementException;

import pt.tecnico.mydrive.service.AddEnvironmentVariableService;
import pt.tecnico.mydrive.service.dto.EnvVarDto;
import pt.tecnico.mydrive.exception.EnvVarUnknownException;

public class Environment extends MdCommand {

    public Environment(MyDrive md) {
        super(md, "env",
              "env [name [value]] : creates/changes environment variable with 'name' and" +
              "associates the 'value'; if value omited, prints the value already associated;" +
              "if no arguments, prints all the variables and their values, separated with '=';");
    }

    @Override
    public void dispatch(String[] args) {
        if (args.length > 2) {
            throw new RuntimeException("USAGE: " + name() + " [<name> [<value>]]");
        }

        final MyDrive drive = (MyDrive) shell();

        if (args.length == 2) {
            new AddEnvironmentVariableService(drive.getToken(), args[0], args[1]).execute();
        } else {
            final AddEnvironmentVariableService addVar =
                new AddEnvironmentVariableService(drive.getToken(), "1", "2");
            final Set<EnvVarDto> dtos = addVar.result();

            if (args.length == 1) {
                final Predicate<EnvVarDto> p = dto -> dto.getName().equals(args[0]);
                final Optional<EnvVarDto> var = dtos.stream().filter(p).findFirst();

                try {
                    System.out.println(var.get());
                } catch (NoSuchElementException e) {
                    throw new EnvVarUnknownException(args[0]);
                }
            } else {
                dtos.stream().forEach(System.out::println);
            }
        }
    }
}
