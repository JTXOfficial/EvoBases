package me.jtx.evobases.commands;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import me.jtx.evobases.EvoBases;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

public class CommandManager {
    private final EvoBases bot;

    private final HashSet<Command> commands;

    public CommandManager(EvoBases bot) {
        this.bot = bot;
        this.commands = new HashSet<>();
    }

    public void initialize() {
        Class<?> type = EvoBases.class;
        ScanResult result = new ClassGraph().acceptPackages("me.jtx.evobases.commands.impl").scan();
        for (ClassInfo cls : result.getAllClasses()) {
            Class<?> loadClass = cls.loadClass();
            try {
                Constructor<?> cons = loadClass.getConstructor(type);
                cons.newInstance(bot);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }


    public Command getCommand(String name) {
        for (Command command : bot.getCommandManager().getCommands()) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }
            if (command.getAliases() != null) {
                for (String alias : command.getAliases()) {
                    if (alias.equalsIgnoreCase(name)) {
                        return command;

                    }
                }
            }
        }
        return null;
    }

    public void register(Command command) {
        this.commands.add(command);
    }

    public EvoBases getBot() {
        return bot;
    }

    public HashSet<Command> getCommands() {
        return commands;
    }
}
