package me.jtx.evobases.commands.impl;

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import me.jtx.evobases.utils.Msg;
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

import java.awt.*;
import java.io.File;
import java.util.List;

public class MakingStatus extends Command {

    private final EvoBases bot;

    public MakingStatus(EvoBases bot) {
        super("status", Permission.ADMINISTRATOR, null, "display's embed of base making status", null);

        this.bot = bot;
        this.bot.getCommandManager().register(this);

    }

    @Override
    public void execute(CommandContext e) {
        int incompleteOrderCount = bot.getOrderDetail().getIncompleteOrders().size();

        String status = "";

        String messageLink = "https://discord.com/channels/1254738644711903303/1254790165290029076";

        EmbedBuilder eb = new EmbedBuilder();

        if (incompleteOrderCount <= 30) {
            status = ":green_circle:- There is a short queue for bases right now! get your order in a sit tight!\n\nPlease be patient and keep a eye on" + messageLink+ " for your base!";
            eb.setColor(Color.GREEN);
        } else if (incompleteOrderCount <= 100) {
            status = ":orange_circle:- There is a average queue for your base if you have just ordered!\n\nPlease be patient and keep a eye on" + messageLink+ " for your base!";
            eb.setColor(Color.ORANGE);
        } else {
            status = ":red_circle:- There is a long wait for bases currently if you have just ordered!\n\nPlease be patient and keep a eye on" + messageLink+ " for your base!";
            eb.setColor(Color.red);
        }

        eb.setDescription(status);


        String channelId = "1255080970323755079";
        TextChannel textChannel = e.getJDA().getTextChannelById(channelId);
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(eb.build()).queue();
        } else {
            System.err.println("Channel not found!");
        }

        e.getSlashEvent().reply("Good job you can run a command").setEphemeral(true).queue();
    }

}