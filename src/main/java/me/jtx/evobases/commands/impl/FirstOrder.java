package me.jtx.evobases.commands.impl;

import com.google.gson.JsonObject;
import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;

public class FirstOrder extends Command {

    private final EvoBases bot;

    public FirstOrder(EvoBases bot) {
        super("firstorder", Permission.UNKNOWN, null, "gets first order link", null);

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

        JsonObject firstOrder = getFirstOrder();
        if (firstOrder == null) {
            e.getSlashEvent().reply("The queue is empty.").setEphemeral(true).queue();
            return;
        }

        System.out.println(firstOrder);


        String messageId = firstOrder.get("messageId").getAsString();
        String messageLink = "https://discord.com/channels/" + e.getGuild().getId() + "/" + bot.getOrderChannelId() + "/" + messageId;

        e.getSlashEvent().reply(messageLink).queue();
    }

    private JsonObject getFirstOrder() {
        List<JsonObject> queue = bot.getOrderDetail().getIncompleteOrders(); // Assuming this is how you get the queue
        if (queue.isEmpty()) {
            return null;
        }
        return queue.get(0);
    }
}
