package me.jtx.evobases.commands.impl;

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.utils.Global;
import me.jtx.evobases.utils.OrderDetail;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Completed extends Command {

    private final EvoBases bot;

    public Completed(EvoBases bot) {
        super("completed", Permission.ADMINISTRATOR, null, "confirm order completed", null);

        this.getSlashOptions().add(new OptionData(OptionType.INTEGER, "orderid", "The order id that you completed", true));
        this.getSlashOptions().add(new OptionData(OptionType.ATTACHMENT, "image", "base image", true));
        this.getSlashOptions().add(new OptionData(OptionType.STRING, "baselink", "base link", true));
        this.bot = bot;
        this.bot.getCommandManager().register(this);

    }

    @Override
    public void execute(CommandContext e) {
        int orderId = e.getSlashEvent().getOption("orderid").getAsInt();
        Message.Attachment image = e.getSlashEvent().getOption("image").getAsAttachment();
        String baseLink = e.getSlashEvent().getOption("baselink").getAsString();

        String userId = bot.getOrderDetail().getUserIdByOrderId(orderId);

        TextChannel channel = e.getSlashEvent().getJDA().getTextChannelById(bot.getOrderChannelId());

        if (bot.getOrderDetail().orderIdExists(orderId)) {
            bot.getOrderDetail().setCompleted(orderId);
            e.getSlashEvent().reply("Order #" + orderId + " completed successfully.").setEphemeral(true).queue();

            String originalMessageId = bot.getOrderDetail().getMessageIdByOrderId(orderId);

            if (originalMessageId != null) {
                channel.retrieveMessageById(originalMessageId).queue(originalMessage -> {
                    EmbedBuilder updatedEmbed = new EmbedBuilder(originalMessage.getEmbeds().get(0));
                    updatedEmbed.clearFields();

                    originalMessage.getEmbeds().get(0).getFields().forEach(field -> {
                        if (field.getName().equalsIgnoreCase("Completed")) {
                            updatedEmbed.addField("Completed", "true", true);
                        } else {
                            updatedEmbed.addField(field);
                        }
                    });

                    originalMessage.editMessageEmbeds(updatedEmbed.setColor(Color.GREEN).build()).queue();
                });
            }

            e.getSlashEvent().getJDA().getTextChannelById(bot.getBaseShowcaseChannelId())
                    .sendMessage("Base for <@" + userId + ">")
                    .addFiles(FileUpload.fromData(image.getProxy().download().join(), image.getFileName()))
                    .addActionRow(Button.secondary("link:", "Link").withEmoji(Emoji.fromUnicode("U+1F517")).withUrl(baseLink))
                    .queue(message -> {
                        String messageLink = "https://discord.com/channels/" + channel.getGuild().getId() + "/" + channel.getId() + "/" + message.getId();

                        e.getSlashEvent().getJDA().getUserById(userId).openPrivateChannel().flatMap(privateChannel ->
                                privateChannel.sendMessage("Your order #" + orderId + " has been completed. Check it out here: " + messageLink)
                        ).queue();
                    });

            /**
             * TODO fix board
             */
            /*TextChannel queueChannel = e.getSlashEvent().getJDA().getTextChannelById(bot.getQueueChannelId());
            queueChannel.retrieveMessageById(bot.getQueueMessageId()).queue(message -> {
                message.editMessageEmbeds(createQueueEmbed(e)).queue();
            });*/


        } else {
            e.getSlashEvent().reply("Order not found.").setEphemeral(true).queue();
        }
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