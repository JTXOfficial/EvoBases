package me.jtx.evobases.commands.impl;// QueueBoard.java

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.utils.Global;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.List;

public class QueueBoard extends Command {

    private final EvoBases bot;

    public QueueBoard(EvoBases bot) {
        super("queueboard", Permission.ADMINISTRATOR, null, "display queue board list", null);

        this.bot = bot;
       // this.bot.getCommandManager().register(this);
    }

    @Override
    public void execute(CommandContext e) {
        e.getSlashEvent().deferReply().setEphemeral(true).queue();

        TextChannel channel = e.getTextChannel();

        channel.sendMessageEmbeds(createQueueEmbed(e)).queue(message -> {
            bot.setQueueMessageId(message.getId());
        });
    }

    private MessageEmbed createQueueEmbed(CommandContext e) {
        List<JsonObject> incompleteOrders = bot.getOrderDetail().getIncompleteOrders();

        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder stringBuilder = new StringBuilder();

        if (incompleteOrders.isEmpty()) {
            embed.setTitle("Current Queue")
                    .setColor(Color.WHITE)
                    .setDescription("The queue is currently empty.")
                    .setFooter(Global.footer);
            return embed.build();
        }

        for (JsonObject order : incompleteOrders) {
            String userId = order.get("userId").getAsString();
            int queueNum = order.get("queueNum").getAsInt();
            User user = e.getUser().getJDA().getUserById(userId);

            stringBuilder.append("**#").append(queueNum).append("** ").append("<@")
                    .append(userId).append("> [").append(user.getName()).append("]\n");
        }

        embed.setTitle("Current Queue")
                .setColor(Color.WHITE)
                .setDescription(stringBuilder.toString())
                .setFooter(Global.footer);

        return embed.build();
    }
}
