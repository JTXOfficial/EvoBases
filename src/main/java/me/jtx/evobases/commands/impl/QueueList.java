package me.jtx.evobases.commands.impl;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.utils.Msg;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QueueList extends Command {

    private final EvoBases bot;
    private final ConcurrentMap<String, Integer> pageStates = new ConcurrentHashMap<>();


    public QueueList(EvoBases bot) {
        super("queue", Permission.UNKNOWN, null, "view queue list", null);

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

        User mentionedUser = e.getSlashEvent().getOption("usertag") != null ? e.getSlashEvent().getOption("usertag").getAsUser() : null;
        if (mentionedUser != null) {
            handleUserQueue(e, incompleteOrders, mentionedUser);
        } else {
            handleFullQueue(e, incompleteOrders, 1);
        }
    }

    private void handleUserQueue(CommandContext e, List<JsonObject> incompleteOrders, User mentionedUser) {
        StringBuilder stringBuilder = new StringBuilder();

        for (JsonObject order : incompleteOrders) {
            String userId = order.get("userId").getAsString();
            if (userId.equals(mentionedUser.getId())) {
                int queueNum = order.get("queueNum").getAsInt();
                stringBuilder.append("**#").append(queueNum).append("** ")
                        .append("<@").append(userId).append("> [").append(mentionedUser.getName()).append("]\n");
            }
        }

        if (stringBuilder.isEmpty()) {
            e.getSlashEvent().reply("The mentioned user has no orders in the queue.").setEphemeral(true).queue();
        } else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(mentionedUser.getEffectiveName() + " Current Queue")
                    .setColor(Color.WHITE)
                    .setDescription(stringBuilder.toString())
                    .setFooter(bot.getEmbedDetails().footer);
            e.getSlashEvent().replyEmbeds(eb.build()).queue();
        }
    }

    public void handleFullQueue(CommandContext e, List<JsonObject> incompleteOrders, int pageNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) incompleteOrders.size() / itemsPerPage);

        int startIndex = (pageNumber - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, incompleteOrders.size());

        if (startIndex < 0 || endIndex > incompleteOrders.size() || startIndex >= endIndex) {
            e.reply("Please run /queue");
        }

        for (int i = startIndex; i < endIndex; i++) {
            JsonObject order = incompleteOrders.get(i);
            String userId = order.get("userId").getAsString();
            int queueNum = order.get("queueNum").getAsInt();
            User user = e.getJDA().retrieveUserById(userId).complete();
            if (user != null) {
                stringBuilder.append("**#").append(queueNum).append("** ")
                        .append("<@").append(userId).append("> [").append(user.getName()).append("]\n");
            }
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Current Queue - Page " + pageNumber + "/" + totalPages)
                .setColor(Color.WHITE)
                .setDescription(stringBuilder.toString())
                .setFooter(bot.getEmbedDetails().footer);

        if (e.isSlash()) {
            e.getSlashEvent().replyEmbeds(eb.build())
                    .addActionRow(
                            Button.primary("prev", "Previous Page").withDisabled(pageNumber == 1),
                            Button.primary("next", "Next Page").withDisabled(pageNumber == totalPages)
                    ).queue();
        } else if (e.getButtonEvent() != null) {
            e.getButtonEvent().editMessageEmbeds(eb.build())
                    .setActionRow(
                            Button.primary("prev", "Previous Page").withDisabled(pageNumber == 1),
                            Button.primary("next", "Next Page").withDisabled(pageNumber == totalPages)
                    ).queue();
        }

        pageStates.put(e.getUser().getId(), pageNumber);
    }

    public int getPageState(String userId) {
        return pageStates.getOrDefault(userId, 1);
    }
}
