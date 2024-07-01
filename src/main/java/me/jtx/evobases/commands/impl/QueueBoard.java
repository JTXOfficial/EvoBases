package me.jtx.evobases.commands.impl;

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QueueBoard extends Command {

    private final EvoBases bot;
    private final ConcurrentMap<String, Integer> pageStates = new ConcurrentHashMap<>();

    public QueueBoard(EvoBases bot) {
        super("queueboard", Permission.ADMINISTRATOR, null, "display queue board list", null);

        this.bot = bot;
        //this.bot.getCommandManager().register(this);
    }

    /*@Override
    public void execute(CommandContext e) {
        List<JsonObject> incompleteOrders = bot.getOrderDetail().getIncompleteOrders();

        if (incompleteOrders.isEmpty()) {
            e.getSlashEvent().reply("The queue is currently empty.").setEphemeral(true).queue();
            return;
        }

        handleFullQueue(e, incompleteOrders, 1);
    }

    public void handleFullQueue(CommandContext e, List<JsonObject> incompleteOrders, int pageNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) incompleteOrders.size() / itemsPerPage);

        int startIndex = (pageNumber - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, incompleteOrders.size());

        for (int i = startIndex; i < endIndex; i++) {
            JsonObject order = incompleteOrders.get(i);
            String userId = order.get("userId").getAsString();
            int queueNum = order.get("queueNum").getAsInt();
            User user = e.getJDA().getUserById(userId);
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
                            net.dv8tion.jda.api.interactions.components.buttons.Button.primary("prev", "Previous Page").withDisabled(pageNumber == 1),
                            net.dv8tion.jda.api.interactions.components.buttons.Button.primary("next", "Next Page").withDisabled(pageNumber == totalPages)
                    ).queue();
        } else if (e.getButtonEvent() != null) {
            e.getButtonEvent().editMessageEmbeds(eb.build())
                    .setActionRow(
                            net.dv8tion.jda.api.interactions.components.buttons.Button.primary("prev", "Previous Page").withDisabled(pageNumber == 1),
                            Button.primary("next", "Next Page").withDisabled(pageNumber == totalPages)
                    ).queue();
        }

        pageStates.put(e.getUser().getId(), pageNumber);
    }

    public int getPageState(String userId) {
        return pageStates.getOrDefault(userId, 1);
    }

    public void updateQueueBoard() {
        List<JsonObject> incompleteOrders = bot.getOrderDetail().getIncompleteOrders();

        TextChannel queueBoardChannel = bot.getJDA().getTextChannelById(bot.getQueueChannelId()); // Assuming you have the channel ID
        if (queueBoardChannel != null) {
            queueBoardChannel.retrieveMessageById(bot.getQueueMessageId()).queue(message -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Current Queue")
                        .setColor(Color.WHITE)
                        .setDescription(buildQueueDescription(incompleteOrders))
                        .setFooter(bot.getEmbedDetails().footer);

                message.editMessageEmbeds(eb.build()).queue();
            });
        }
    }

    private String buildQueueDescription(List<JsonObject> incompleteOrders) {
        StringBuilder stringBuilder = new StringBuilder();

        for (JsonObject order : incompleteOrders) {
            String userId = order.get("userId").getAsString();
            int queueNum = order.get("queueNum").getAsInt();
            User user = bot.getJDA().getUserById(userId);
            if (user != null) {
                stringBuilder.append("**#").append(queueNum).append("** ")
                        .append("<@").append(userId).append("> [").append(user.getName()).append("]\n");
            }
        }

        return stringBuilder.toString();
    }*/
}
