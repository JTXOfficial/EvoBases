package me.jtx.evobases.commands.impl;

import me.jtx.evobases.EvoBases;
import me.jtx.evobases.commands.Command;
import me.jtx.evobases.commands.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;

public class Review extends Command {

    private final EvoBases bot;

    public Review(EvoBases bot) {
        super("review", Permission.UNKNOWN, null, "post a review of your order", null);

        this.bot = bot;
        this.bot.getCommandManager().register(this);
    }

    @Override
    public void execute(CommandContext e) {

        TextInput stars = TextInput.create("ratingNumber", "Rating Number", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("Rate from 1 to 5!")
                .build();

        TextInput review = TextInput.create("review", "Feedback", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setPlaceholder("Write your feedback here!")
                .build();


        Modal modal = Modal.create("reviewModal", "Order Review")
                .addComponents(ActionRow.of(stars), ActionRow.of(review))
                .build();

        e.getSlashEvent().replyModal(modal).queue();






    }
}
