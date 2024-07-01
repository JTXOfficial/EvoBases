package me.jtx.evobases.commands.impl;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class OrderMenu extends Command {

    private final EvoBases bot;

    public OrderMenu(EvoBases bot) {
        super("ticketmenu", Permission.UNKNOWN, null, "display ticket menu", null);

        this.bot = bot;
        this.bot.getCommandManager().register(this);

    }

    @Override
    public void execute(CommandContext e) {
        String todayDate = bot.getGlobal().todayDate();
        int currentCount = bot.getDailyOrderLimit().getCurrentOrderCount(todayDate);
        int maxLimit = bot.getDailyOrderMaxLimit();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(bot.getMenuTitle())
                .setDescription(String.format(bot.getMenuDescription(), maxLimit, "\n"))
                .addField("Current Count", currentCount + "/" + maxLimit, false)
                .setColor(Color.WHITE)
                .setFooter(bot.getEmbedDetails().footer);

        e.getSlashEvent().deferReply().setEphemeral(true).queue();

        e.getTextChannel().sendMessageEmbeds(eb.build()).addActionRow(
                Button.success("startOrderFormModal", bot.getMenuStartOrderButtonMessage())).queue();
    }


}