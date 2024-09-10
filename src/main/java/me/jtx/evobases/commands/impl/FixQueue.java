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

public class FixQueue extends Command {

    private final EvoBases bot;

    public FixQueue(EvoBases bot) {
        super("fixqueue", Permission.UNKNOWN, null, "fix queue", null);

        this.bot = bot;
        this.bot.getCommandManager().register(this);

    }

    @Override
    public void execute(CommandContext e) {
        boolean baseDesignRole = e.getGuild().getMember(e.getUser()).getRoles().stream()
                .anyMatch(role -> role.getId().equals(bot.getBaseDesignerRoleId()));

        boolean mod = e.getGuild().getMember(e.getUser()).getRoles().stream()
                .anyMatch(role -> role.getId().equals(bot.getModerationRoleId()));

        if (!baseDesignRole && !mod) {
            EmbedBuilder noPermEB = new EmbedBuilder();
            noPermEB.setTitle("Error")
                    .setDescription("You do not have permission!")
                    .setColor(Color.RED)
                    .setFooter(bot.getEmbedDetails().footer);

            e.getSlashEvent().replyEmbeds(noPermEB.build()).setEphemeral(true).queue();
            return;
        }

        bot.getOrderDetail().autoAdjustQueue();

        e.getSlashEvent().reply("Queue has been reordered").setEphemeral(true).queue();

    }

}