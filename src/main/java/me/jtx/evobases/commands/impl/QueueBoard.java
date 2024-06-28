package me.jtx.evobases.commands.impl;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.Permission;

public class QueueBoard extends Command {

    private final EvoBases bot;

    public QueueBoard(EvoBases bot) {
        super("queueboard", Permission.ADMINISTRATOR, null, "display queue board list", null);
        this.bot = bot;
        this.bot.getCommandManager().register(this);
    }

    @Override
    public void execute(CommandContext e) {
    }


}
