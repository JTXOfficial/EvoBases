package me.jtx.evobases.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashSet;

public class Command {
    private String name;
    private Permission requiredPermission;
    private String[] aliases;
    private String description;
    private HashSet<OptionData> slashOptions;
    private final int cooldown;

    public Command(String name, Permission requiredPermission, String[] aliases, String description, HashSet<OptionData> options) {
        this.name = name;
        this.requiredPermission = requiredPermission;
        this.aliases = aliases;
        this.description = description;
        this.slashOptions = options == null ? new HashSet<>() : options;
        this.cooldown = 0;
    }

    public HashSet<CommandData> toCommand() {
        HashSet<CommandData> hash = new HashSet<>();
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                hash.add(slashOptions.isEmpty() ? Commands.slash(alias, description) : Commands.slash(alias, description).addOptions(slashOptions));
            }
        }
        hash.add(slashOptions.isEmpty() ? Commands.slash(name, description) : Commands.slash(name, description).addOptions(slashOptions));
        return hash;
    }


    public void execute(CommandContext ctx) {
    }

    public HashSet<OptionData> getSlashOptions() {
        return slashOptions;
    }




    public Permission getRequiredPermission() {
        return requiredPermission;
    }


    public String getName() {
        return name;
    }



    public String[] getAliases() {
        return aliases;
    }

}
