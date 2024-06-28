package me.jtx.evobases.commands.impl;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class QueueList extends Command {

    private final EvoBases bot;

    public QueueList(EvoBases bot) {
        super("queue", Permission.ADMINISTRATOR, null, "view queue list", null);

        this.getSlashOptions().add(new OptionData(OptionType.MENTIONABLE, "usertag", "View the user's queue place", false));
        this.bot = bot;
        this.bot.getCommandManager().register(this);
    }

    @Override
    public void execute(CommandContext e) {
        List<JsonObject> incompleteOrders = bot.getOrderDetail().getIncompleteOrders();

        if (incompleteOrders.isEmpty()) {
            e.getSlashEvent().reply("The queue is currently empty.").setEphemeral(true).queue();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        User mentionedUser = e.getSlashEvent().getOption("usertag") != null ? e.getSlashEvent().getOption("usertag").getAsUser() : null;


        if (mentionedUser != null) {
            for (JsonObject order : incompleteOrders) {
                String userId = order.get("userId").getAsString();
                if (userId.equals(mentionedUser.getId())) {
                    int queueNum = order.get("queueNum").getAsInt();
                    stringBuilder.append("**#").append(queueNum).append("** ").append("<@").append(userId).append("> [").append(mentionedUser.getName()).append("]\n");
                }
            }

            if (stringBuilder.isEmpty()) {
                e.getSlashEvent().reply("The mentioned user has no orders in the queue.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(mentionedUser.getEffectiveName() + " Current Queue")
                    .setColor(Color.WHITE)
                    .setDescription(stringBuilder.toString());

            e.getSlashEvent().replyEmbeds(eb.build()).queue();
        } else {
            for (JsonObject order : incompleteOrders) {
                String userId = order.get("userId").getAsString();
                int queueNum = order.get("queueNum").getAsInt();
                User user = e.getUser().getJDA().getUserById(userId);
                if (user != null) {
                    stringBuilder.append("**#").append(queueNum).append("** ").append("<@").append(userId).append("> [").append(user.getName()).append("]\n");
                }
            }
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Current Queue")
                .setColor(Color.WHITE)
                .setDescription(stringBuilder.toString());

        e.getSlashEvent().replyEmbeds(eb.build()).queue();
    }
}

